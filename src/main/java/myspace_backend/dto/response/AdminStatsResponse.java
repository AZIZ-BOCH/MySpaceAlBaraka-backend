package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    private long nombreClients;

    private long nombreComptes;

    private long nombreUtilisateursInscrits;

    private long nombreTransactions;

    private long nombreClientsNonInscrits;
}