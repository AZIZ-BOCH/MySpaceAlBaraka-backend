package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUtilisateurResponse {
    private Long id;
    private String identifiant;
    private String email;
    private String nom;
    private String prenom;
    private String cin;
    private boolean actif;
}