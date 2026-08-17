package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.entity.CodeOtp;
import myspace_backend.repository.CodeOtpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste le compteur de tentatives / verrouillage d'un CodeOtp dans une
 * transaction INDÉPENDANTE (REQUIRES_NEW).
 * <p>
 * Nécessaire car AuthService#validerCodeOtp enregistre la tentative échouée
 * puis lève une exception pour signaler l'échec au contrôleur. Si cette
 * sauvegarde se faisait dans la même transaction que l'appel entrant
 * (ex: CarteService.modifierPlafonds), le rollback automatique déclenché par
 * l'exception annulerait aussi l'incrémentation du compteur de tentatives,
 * rendant le verrouillage anti brute-force inopérant.
 */
@Service
@RequiredArgsConstructor
public class OtpTentativeService {

    private final CodeOtpRepository codeOtpRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerTentativeEchouee(Long otpId, int nouvellesTentatives, boolean verrouiller) {
        codeOtpRepository.findById(otpId).ifPresent(otp -> {
            otp.setAttempts(nouvellesTentatives);
            if (verrouiller) {
                otp.setVerrouille(true);
                otp.setUtilise(true);
            }
            codeOtpRepository.save(otp);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marquerExpire(Long otpId) {
        codeOtpRepository.findById(otpId).ifPresent(otp -> {
            otp.setUtilise(true);
            codeOtpRepository.save(otp);
        });
    }
}