package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginInitResponse {
    private String message;
    private String email;
    private boolean premiereConnexion;
}