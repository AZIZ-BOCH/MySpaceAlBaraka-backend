package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BeneficiaireResponse {
    private Long id;
    private String nom;
    private String rib;
}