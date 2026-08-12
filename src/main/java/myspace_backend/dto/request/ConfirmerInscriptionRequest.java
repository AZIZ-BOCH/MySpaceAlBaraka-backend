package myspace_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmerInscriptionRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;

    @NotBlank(message = "Le code est obligatoire")
    private String code;
}