package myspace_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le code OTP est obligatoire")
    private String code;
}