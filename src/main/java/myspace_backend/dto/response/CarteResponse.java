package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import myspace_backend.entity.Carte;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarteResponse {
    private String carteId;
    private String numeroMasque;
    private String titulaire;
    private String dateExpiration;
    private String cvv;
    private boolean estGelee;
    private boolean paiementsEnLigne;
    private BigDecimal plafondRetrait;
    private BigDecimal plafondPaiement;

    public static CarteResponse fromEntity(Carte carte) {
        return new CarteResponse(
                carte.getCarteId(),
                carte.getNumeroMasque(),
                carte.getTitulaire(),
                carte.getDateExpiration(),
                carte.getCvv(),
                carte.isEstGelee(),
                carte.isPaiementsEnLigne(),
                carte.getPlafondRetrait(),
                carte.getPlafondPaiement()
        );
    }
}