package myspace_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.ChangerMotDePasseRequest;
import myspace_backend.dto.response.ProfilResponse;
import myspace_backend.service.AuthService;
import myspace_backend.service.ProfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService profilService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<ProfilResponse> obtenirProfil(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(profilService.obtenirProfil(email));
    }

    @PostMapping("/changer-mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(
            Authentication authentication,
            @Valid @RequestBody ChangerMotDePasseRequest request) {

        // Ensure the email in request matches the authenticated user
        request.setEmail(authentication.getName());
        authService.changerMotDePasse(request);
        return ResponseEntity.noContent().build();
    }
}