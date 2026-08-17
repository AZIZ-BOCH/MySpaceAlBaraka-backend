package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    // Threshold for large transaction alert (e.g., 1000 TND)
    private static final BigDecimal SEUIL_GRAND_VIREMENT = new BigDecimal("1000");

    /**
     * Alert user when card status changes (Frozen / Unfrozen)
     */
    public void notifierStatutCarte(String emailClient, String numCarte, boolean estGelee) {
        emailService.envoyerAlerteStatutCarte(emailClient, numCarte, estGelee);
    }

    /**
     * Alert user when a transaction exceeds the threshold
     */
    public void notifierGrandVirement(String emailClient, BigDecimal montant, String destinataire) {
        if (montant != null && montant.compareTo(SEUIL_GRAND_VIREMENT) >= 0) {
            emailService.envoyerAlerteGrandVirement(emailClient, montant, destinataire);
        }
    }

    /**
     * Alert user when balance falls below minimum threshold
     */
    public void notifierSoldeBas(String emailClient, BigDecimal soldeActuel) {
        BigDecimal seuilSoldeBas = new BigDecimal("50"); // 50 TND limit
        if (soldeActuel != null && soldeActuel.compareTo(seuilSoldeBas) < 0) {
            emailService.envoyerAlerteSoldeBas(emailClient, soldeActuel);
        }
    }
}