package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.CarteRequestDTO;
import myspace_backend.dto.response.CarteResponse;
import myspace_backend.dto.response.TransactionCarteResponse;
import myspace_backend.entity.Carte;
import myspace_backend.repository.CarteRepository;
import myspace_backend.repository.TransactionCarteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarteService {

    private final CarteRepository carteRepository;
    private final TransactionCarteRepository transactionCarteRepository;
    private final AuthService authService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public List<CarteResponse> obtenirCartesDuClient(String email) {
        return carteRepository.findByClientEmail(email)
                .stream()
                .map(CarteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public void genererEtEnvoyerOtp(String userEmail) {
        authService.genererEtEnvoyerOtpVirement(userEmail);
    }

    @Transactional
    public CarteResponse obtenirDetailsCarteAvecOtp(String carteId, String codeOtp, String userEmail) {
        authService.validerOtpVirement(userEmail, codeOtp);

        Carte carte = carteRepository.findByCarteId(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));

        return CarteResponse.fromEntity(carte);
    }

    @Transactional
    public CarteResponse modifierPlafonds(String carteId, CarteRequestDTO req, String userEmail) {
        if (req.getCodeOtp() != null && !req.getCodeOtp().isBlank()) {
            authService.validerOtpVirement(userEmail, req.getCodeOtp());
        } else {
            throw new RuntimeException("Code OTP requis pour modifier les plafonds.");
        }

        Carte carte = carteRepository.findByCarteId(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));

        if (carte.isEstGelee()) {
            throw new RuntimeException("Impossible de modifier les plafonds d'une carte gelée.");
        }

        // --- Validation Métier Backend (BigDecimal) ---
        BigDecimal minSeuil = new BigDecimal("50.00");

        if (req.getPlafondRetrait() == null || req.getPlafondPaiement() == null) {
            throw new IllegalArgumentException("Les plafonds de retrait et de paiement ne peuvent pas être vides.");
        }

        if (req.getPlafondRetrait().compareTo(minSeuil) < 0 || req.getPlafondPaiement().compareTo(minSeuil) < 0) {
            throw new IllegalArgumentException("Le plafond minimal autorisé est de 50.00 TND.");
        }

        carte.setPlafondRetrait(req.getPlafondRetrait());
        carte.setPlafondPaiement(req.getPlafondPaiement());

        return CarteResponse.fromEntity(carteRepository.save(carte));
    }

    @Transactional
    public CarteResponse modifierControles(String carteId, CarteRequestDTO req, String userEmail) {
        Carte carte = carteRepository.findByCarteId(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));

        if (req.getEstGelee() != null && req.getEstGelee() != carte.isEstGelee()) {
            carte.setEstGelee(req.getEstGelee());
            notificationService.notifierStatutCarte(userEmail, carte.getCarteId(), req.getEstGelee());

            String actionType = req.getEstGelee() ? "GEL_CARTE" : "DEGEL_CARTE";
            String description = String.format("La carte %s a été %s par %s",
                    carte.getCarteId(),
                    req.getEstGelee() ? "gelée" : "dégelée",
                    userEmail);
            auditService.enregistrer(actionType, description, userEmail);
        }

        if (req.getPaiementsEnLigne() != null) {
            carte.setPaiementsEnLigne(req.getPaiementsEnLigne());
        }

        return CarteResponse.fromEntity(carteRepository.save(carte));
    }

    public List<TransactionCarteResponse> obtenirDernieresTransactions(String carteId) {
        Carte carte = carteRepository.findByCarteId(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));

        return transactionCarteRepository.findTop10ByCarteIdOrderByDateTransactionDesc(carte.getId())
                .stream()
                .map(t -> TransactionCarteResponse.builder()
                        .id(t.getId())
                        .description(t.getDescription())
                        .montant(t.getMontant())
                        .dateTransaction(t.getDateTransaction())
                        .statut(t.getStatut())
                        .type(t.getType())
                        .build())
                .collect(Collectors.toList());
    }
}