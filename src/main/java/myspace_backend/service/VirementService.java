package myspace_backend.service;

import myspace_backend.dto.request.VirementRequestDTO;
import myspace_backend.entity.Beneficiaire;
import myspace_backend.entity.Compte;
import myspace_backend.entity.Transaction;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.IdentifiantsInvalidesException;
import myspace_backend.repository.BeneficiaireRepository;
import myspace_backend.repository.CompteRepository;
import myspace_backend.repository.TransactionRepository;
import myspace_backend.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class VirementService {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final AuthService authService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public void demanderOtpVirement(String userEmail) {
        authService.genererEtEnvoyerOtpVirement(userEmail);
    }

    @Transactional
    public void effectuerVirement(VirementRequestDTO dto, String userEmail) {
        // 1. Fetch User
        Utilisateur utilisateur = utilisateurRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        // 2. Fetch Source Account
        Compte compteSource = compteRepository.findByRib(dto.getRibSource())
                .orElseThrow(() -> new RuntimeException("Compte source introuvable."));

        // Verify that the logged-in user owns this source account through Client entity
        if (utilisateur.getClient() == null || compteSource.getClient() == null
                || !compteSource.getClient().getId().equals(utilisateur.getClient().getId())) {
            throw new RuntimeException("Vous n'êtes pas autorisé à débiter ce compte.");
        }

        // 3. Fetch Destination Account
        Compte compteDest = compteRepository.findByRib(dto.getRibDestination())
                .orElseThrow(() -> new RuntimeException("Compte destinataire introuvable."));

        // Prevent transfer to the same account
        if (compteSource.getRib().equalsIgnoreCase(compteDest.getRib())) {
            throw new RuntimeException("Le compte source et le compte destination doivent être différents.");
        }

        // 4. Calculate total balance for source account from existing transactions
        BigDecimal soldeSource = calculerSoldeActuel(compteSource);
        if (soldeSource.compareTo(dto.getMontant()) < 0) {
            throw new RuntimeException("Solde insuffisant pour effectuer ce virement.");
        }

        // 5. Handle OTP validation if transfer is to a Third Party (VERS_TIERS)
        if ("VERS_TIERS".equalsIgnoreCase(dto.getTypeVirement())) {
            boolean estBeneficiaireEnregistre = beneficiaireRepository
                    .findByClientAndRib(utilisateur.getClient(), dto.getRibDestination())
                    .isPresent();

            // Require OTP only if the beneficiary is NOT already saved
            if (!estBeneficiaireEnregistre) {
                if (dto.getCodeOtp() == null || dto.getCodeOtp().isBlank()) {
                    throw new RuntimeException("Code OTP requis pour un virement vers un nouveau bénéficiaire.");
                }
                authService.validerOtpVirement(userEmail, dto.getCodeOtp());

                // Save new beneficiary only if the user explicitly chose to
                if (dto.isEnregistrerBeneficiaire()) {
                    String nomBeneficiaire = compteDest.getClient() != null
                            ? compteDest.getClient().getNom() + " " + compteDest.getClient().getPrenom()
                            : "Bénéficiaire " + dto.getRibDestination();

                    Beneficiaire nouveauBeneficiaire = Beneficiaire.builder()
                            .nom(nomBeneficiaire)
                            .rib(dto.getRibDestination())
                            .client(utilisateur.getClient())
                            .build();

                    beneficiaireRepository.save(nouveauBeneficiaire);
                }
            }
        }

        // 6. Calculate new balances
        BigDecimal soldeDest = calculerSoldeActuel(compteDest);
        BigDecimal nouveauSoldeSource = soldeSource.subtract(dto.getMontant());
        BigDecimal nouveauSoldeDest = soldeDest.add(dto.getMontant());

        // 7. Record DEBIT Transaction
        Transaction txnDebit = new Transaction();
        txnDebit.setDateOperation(LocalDate.now());
        txnDebit.setLibelle("Virement émis : " + (dto.getMotif() != null && !dto.getMotif().isBlank() ? dto.getMotif() : "Transfert") + " vers " + dto.getRibDestination());
        txnDebit.setDebit(dto.getMontant());
        txnDebit.setCredit(null);
        txnDebit.setSolde(nouveauSoldeSource);
        txnDebit.setCompte(compteSource);
        transactionRepository.save(txnDebit);

        // 8. Record CREDIT Transaction
        Transaction txnCredit = new Transaction();
        txnCredit.setDateOperation(LocalDate.now());
        String nomEmetteur = utilisateur.getClient() != null ? utilisateur.getClient().getNom() : utilisateur.getIdentifiant();
        txnCredit.setLibelle("Virement reçu : " + (dto.getMotif() != null && !dto.getMotif().isBlank() ? dto.getMotif() : "Transfert") + " de " + nomEmetteur);
        txnCredit.setDebit(null);
        txnCredit.setCredit(dto.getMontant());
        txnCredit.setSolde(nouveauSoldeDest);
        txnCredit.setCompte(compteDest);
        transactionRepository.save(txnCredit);

        // 📜 9. Record Audit Log for Admin View
        String actionType = "ENTRE_MES_COMPTES".equalsIgnoreCase(dto.getTypeVirement())
                ? "VIREMENT_INTERNE"
                : "VIREMENT_TIERS";

        String description = String.format(
                "Virement de %.3f TND effectué par %s (RIB Source: %s -> RIB Destination: %s)",
                dto.getMontant(),
                userEmail,
                dto.getRibSource(),
                dto.getRibDestination()
        );

        auditService.enregistrer(actionType, description, userEmail);

        // 📩 10. Trigger Email Notifications
        notificationService.notifierGrandVirement(userEmail, dto.getMontant(), dto.getRibDestination());
        notificationService.notifierSoldeBas(userEmail, nouveauSoldeSource);
    }

    private BigDecimal calculerSoldeActuel(Compte compte) {
        return transactionRepository.findTopByCompte_IdOrderByDateOperationDescIdDesc(compte.getId())
                .map(Transaction::getSolde)
                .orElse(BigDecimal.ZERO);
    }
}