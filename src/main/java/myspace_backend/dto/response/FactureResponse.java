package myspace_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import myspace_backend.entity.OrganismeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FactureResponse {
    private Long id;
    private OrganismeType organisme;
    private String referenceFacture;
    private BigDecimal montant;
    private boolean payee;
    private LocalDateTime datePaiement;
    private String recuReference;
    private String message;
}