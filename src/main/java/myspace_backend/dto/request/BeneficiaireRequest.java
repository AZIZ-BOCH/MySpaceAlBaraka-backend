package myspace_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BeneficiaireRequest {
    @NotBlank(message = "Le nom du bénéficiaire est obligatoire")
    private String nom;

    @NotBlank(message = "Le RIB est obligatoire")
    private String rib;

    @NotBlank(message = "Le code OTP est obligatoire")
    private String codeOtp;
}