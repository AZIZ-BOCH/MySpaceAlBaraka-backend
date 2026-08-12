package myspace_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import myspace_backend.entity.CanalEnvoi;

@Data
public class EnvoyerCodeRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;

    @NotNull(message = "Le canal est obligatoire")
    private CanalEnvoi canal;
}