package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.FactureConsultationRequest;
import myspace_backend.dto.request.PaiementFactureRequest;
import myspace_backend.dto.response.FactureResponse;
import myspace_backend.entity.Compte;
import myspace_backend.entity.Facture;
import myspace_backend.entity.Transaction;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.IdentifiantsInvalidesException;
import myspace_backend.repository.CompteRepository;
import myspace_backend.repository.FactureRepository;
import myspace_backend.repository.TransactionRepository;
import myspace_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuthService authService;
    private final AuditService auditService; // 👈 Inject AuditService

    @Transactional(readOnly = true)
    public FactureResponse consulterFacture(String email, FactureConsultationRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        return factureRepository.findByOrganismeAndReferenceFacture(request.getOrganisme(), request.getReferenceFacture())
                .map(f -> mapToResponse(f, f.isPayee() ? "Cette facture a déjà été réglée." : "Facture trouvée."))
                .orElseGet(() -> {
                    BigDecimal dynamicAmount = new BigDecimal((15 + new Random().nextInt(185)) + ".500");
                    Facture simulatedFacture = Facture.builder()
                            .organisme(request.getOrganisme())
                            .referenceFacture(request.getReferenceFacture())
                            .montant(dynamicAmount)
                            .payee(false)
                            .client(utilisateur.getClient())
                            .build();

                    Facture saved = factureRepository.save(simulatedFacture);
                    return mapToResponse(saved, "Facture récupérée depuis l'organisme.");
                });
    }

    @Transactional
    public void demanderOtpFacture(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        authService.genererEtEnvoyerOtpVirement(email);
    }

    @Transactional
    public FactureResponse payerFacture(String email, PaiementFactureRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        // Validate OTP
        authService.validerOtpVirement(email, request.getCodeOtp());

        Compte compteSource = compteRepository.findByRibAndClient_Id(request.getRibSource(), utilisateur.getClient().getId())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Compte débiteur invalide ou non autorisé."));

        Facture facture = factureRepository.findByOrganismeAndReferenceFacture(request.getOrganisme(), request.getReferenceFacture())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Facture introuvable."));

        if (facture.isPayee()) {
            throw new IdentifiantsInvalidesException("Cette facture est déjà réglée.");
        }

        // Calculate current balance using transactions
        BigDecimal soldeActuel = calculerSoldeActuel(compteSource);
        if (soldeActuel.compareTo(facture.getMontant()) < 0) {
            throw new IdentifiantsInvalidesException("Solde insuffisant sur le compte débiteur.");
        }

        BigDecimal nouveauSolde = soldeActuel.subtract(facture.getMontant());

        // Record Debit Transaction
        Transaction txnDebit = new Transaction();
        txnDebit.setDateOperation(LocalDate.now());
        txnDebit.setLibelle("Paiement facture " + facture.getOrganisme() + " (Réf: " + facture.getReferenceFacture() + ")");
        txnDebit.setDebit(facture.getMontant());
        txnDebit.setCredit(null);
        txnDebit.setSolde(nouveauSolde);
        txnDebit.setCompte(compteSource);
        transactionRepository.save(txnDebit);

        // Execute Payment
        facture.setPayee(true);
        facture.setDatePaiement(LocalDateTime.now());
        facture.setRecuReference("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        factureRepository.save(facture);

        // 📜 Record Audit Log for Admin View
        String description = String.format("Paiement de la facture %s (%s) d'un montant de %.3f TND effectué par %s",
                facture.getOrganisme(),
                facture.getReferenceFacture(),
                facture.getMontant(),
                email
        );
        auditService.enregistrer("PAIEMENT_FACTURE", description, email);

        return mapToResponse(facture, "Paiement de la facture effectué avec succès !");
    }

    @Transactional(readOnly = true)
    public List<FactureResponse> getMesFactures(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur introuvable."));

        return factureRepository.findByClient_Id(utilisateur.getClient().getId())
                .stream()
                .map(f -> mapToResponse(f, null))
                .collect(Collectors.toList());
    }

    private BigDecimal calculerSoldeActuel(Compte compte) {
        return transactionRepository.findTopByCompte_IdOrderByDateOperationDescIdDesc(compte.getId())
                .map(Transaction::getSolde)
                .orElse(BigDecimal.ZERO);
    }

    private FactureResponse mapToResponse(Facture facture, String message) {
        return FactureResponse.builder()
                .id(facture.getId())
                .organisme(facture.getOrganisme())
                .referenceFacture(facture.getReferenceFacture())
                .montant(facture.getMontant())
                .payee(facture.isPayee())
                .datePaiement(facture.getDatePaiement())
                .recuReference(facture.getRecuReference())
                .message(message)
                .build();
    }
}