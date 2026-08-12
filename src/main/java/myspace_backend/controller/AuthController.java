package myspace_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.ChangerMotDePasseRequest;
import myspace_backend.dto.request.LoginRequest;
import myspace_backend.dto.request.VerifyOtpRequest;
import myspace_backend.dto.response.LoginInitResponse;
import myspace_backend.dto.response.LoginResponse;
import myspace_backend.service.AuthService;
import myspace_backend.dto.request.ForgotPasswordInitRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Étape 1 : Vérifie Email + Mot de passe
     * Génère un code OTP et l'envoie par email au client.
     */
    @PostMapping("/login/init")
    public ResponseEntity<LoginInitResponse> loginInit(@Valid @RequestBody LoginRequest request) {
        LoginInitResponse response = authService.initialiserConnexion(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Étape 2 (2FA) : Vérifie le code OTP reçu par email
     * Si valide, retourne le token/session et les informations de connexion.
     */
    @PostMapping("/login/verify-otp")
    public ResponseEntity<LoginResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        LoginResponse response = authService.verifierOtpEtConnecter(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Changement de mot de passe obligatoire lors de la 1ère connexion
     */
    @PostMapping("/changer-mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(@Valid @RequestBody ChangerMotDePasseRequest request) {
        authService.changerMotDePasse(request);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/forgot-password/init")
    public ResponseEntity<Void> forgotPasswordInit(@Valid @RequestBody ForgotPasswordInitRequest request) {
        authService.initialiserMotDePasseOublie(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<Void> forgotPasswordVerifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.validerMotDePasseOublieEtEnvoyerMdp(request);
        return ResponseEntity.ok().build();
    }
}