package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.CarteRequestDTO;
import myspace_backend.dto.response.CarteResponse;
import myspace_backend.entity.Carte;
import myspace_backend.repository.CarteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarteService {

    private final CarteRepository carteRepository;
    private final AuthService authService;
    private final NotificationService notificationService; // 👈 Inject NotificationService

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

        if (req.getPlafondRetrait() != null) carte.setPlafondRetrait(req.getPlafondRetrait());
        if (req.getPlafondPaiement() != null) carte.setPlafondPaiement(req.getPlafondPaiement());

        return CarteResponse.fromEntity(carteRepository.save(carte));
    }

    @Transactional
    public CarteResponse modifierControles(String carteId, CarteRequestDTO req, String userEmail) {
        Carte carte = carteRepository.findByCarteId(carteId)
                .orElseThrow(() -> new RuntimeException("Carte non trouvée : " + carteId));

        if (req.getEstGelee() != null && req.getEstGelee() != carte.isEstGelee()) {
            carte.setEstGelee(req.getEstGelee());
            // 📩 Alert user via email when card is frozen or unfrozen
            notificationService.notifierStatutCarte(userEmail, carte.getCarteId(), req.getEstGelee());
        }

        if (req.getPaiementsEnLigne() != null) {
            carte.setPaiementsEnLigne(req.getPaiementsEnLigne());
        }

        return CarteResponse.fromEntity(carteRepository.save(carte));
    }
}