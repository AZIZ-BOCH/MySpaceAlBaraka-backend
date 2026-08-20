package myspace_backend.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VirementRequestDTO {
    private String ribSource;
    private String ribDestination;
    private BigDecimal montant;
    private String motif;
    private String typeVirement; // "ENTRE_MES_COMPTES" or "VERS_TIERS"
    private String codeOtp;       // Required only for "VERS_TIERS"
    private boolean enregistrerBeneficiaire; // 👈 NOUVEAU : choix de l'utilisateur
}