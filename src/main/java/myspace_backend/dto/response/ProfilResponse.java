package myspace_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilResponse {

    private String nom;

    private String prenom;

    private String email;

    private String telephone;
}