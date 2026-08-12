package myspace_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import myspace_backend.entity.TypeDocument;

@Data
public class VerifierIdentiteRequest {

    @NotBlank(message = "Le RIB est obligatoire")
    @Pattern(regexp = "^32\\d{18}$", message = "Le RIB doit commencer par 32 et contenir 20 chiffres")
    private String rib;

    @NotNull(message = "Le type de document est obligatoire")
    private TypeDocument typeDocument;

    @NotBlank(message = "Le numero de document est obligatoire")
    private String numeroDocument;
}