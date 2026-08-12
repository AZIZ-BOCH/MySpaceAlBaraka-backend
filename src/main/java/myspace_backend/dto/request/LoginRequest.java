package myspace_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'identifiant est obligatoire")
    private String identifiant; // 👈 Peut être l'identifiant (azizbouchriha) ou l'email

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;
}