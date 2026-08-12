package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private LocalDate date;

    private String libelle;

    private BigDecimal debit;

    private BigDecimal credit;

    private BigDecimal solde;
}