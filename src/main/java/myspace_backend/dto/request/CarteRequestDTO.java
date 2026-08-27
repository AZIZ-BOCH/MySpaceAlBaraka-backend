package myspace_backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarteRequestDTO {
    // Toggles / Controls
    private Boolean estGelee;
    private Boolean paiementsEnLigne;

    // Plafonds / Limits
    @NotNull(message = "Le plafond de retrait est obligatoire")
    @DecimalMin(value = "50.00", message = "Le plafond de retrait minimum est de 50.00 TND")
    private BigDecimal plafondRetrait;

    @NotNull(message = "Le plafond de paiement est obligatoire")
    @DecimalMin(value = "50.00", message = "Le plafond de paiement minimum est de 50.00 TND")
    private BigDecimal plafondPaiement;

    // Security / OTP
    private String codeOtp;
}