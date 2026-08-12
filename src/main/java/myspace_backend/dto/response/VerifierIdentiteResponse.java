package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifierIdentiteResponse {

    private Long clientId;

    private String emailMasque;

    private String telephoneMasque;
}