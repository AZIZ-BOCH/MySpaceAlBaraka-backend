package myspace_backend.controller;

import myspace_backend.dto.request.VirementRequestDTO;
import myspace_backend.service.VirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/virements")
@RequiredArgsConstructor
public class VirementController {

    private final VirementService virementService;

    // Demander un code OTP (uniquement pour virement VERS_TIERS)
    @PostMapping("/demander-otp")
    public ResponseEntity<?> demanderOtp(Authentication auth) {
        try {
            String email = auth.getName();
            virementService.demanderOtpVirement(email);
            return ResponseEntity.ok(Map.of("message", "Code OTP envoyé à votre adresse email."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Exécuter le virement
    @PostMapping("/executer")
    public ResponseEntity<?> executerVirement(@RequestBody VirementRequestDTO dto, Authentication auth) {
        try {
            String email = auth.getName();
            virementService.effectuerVirement(dto, email);
            return ResponseEntity.ok(Map.of("message", "Virement effectué avec succès."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}