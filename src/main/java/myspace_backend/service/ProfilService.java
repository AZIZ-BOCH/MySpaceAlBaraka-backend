package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.response.ProfilResponse;
import myspace_backend.entity.Client;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.ClientNotFoundException;
import myspace_backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilService {

    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public ProfilResponse obtenirProfil(String email) {
        Client client = recupererClientParEmail(email);
        return new ProfilResponse(client.getNom(), client.getPrenom(), client.getEmail(), client.getTelephone());
    }

    private Client recupererClientParEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("Utilisateur introuvable"));
        return utilisateur.getClient();
    }
}