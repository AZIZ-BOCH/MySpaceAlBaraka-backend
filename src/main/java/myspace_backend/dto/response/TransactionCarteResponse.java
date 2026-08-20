package myspace_backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionCarteResponse {
    private Long id;
    private String description;
    private BigDecimal montant;
    private LocalDateTime dateTransaction;
    private String statut;
    private String type;
}