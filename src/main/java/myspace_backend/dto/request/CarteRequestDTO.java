package myspace_backend.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarteRequestDTO {
    // Toggles / Controls
    private Boolean estGelee;
    private Boolean paiementsEnLigne;

    // Plafonds / Limits
    private BigDecimal plafondRetrait;
    private BigDecimal plafondPaiement;

    // Security / OTP
    private String codeOtp;
}