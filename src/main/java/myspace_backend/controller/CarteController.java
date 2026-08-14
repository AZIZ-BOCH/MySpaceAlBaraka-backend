package myspace_backend.controller;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.CarteRequestDTO;
import myspace_backend.dto.response.CarteResponse;
import myspace_backend.service.CarteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cartes")
@RequiredArgsConstructor
public class CarteController {

    private final CarteService carteService;

    // 1. Get User Cards
    @GetMapping("/mes-cartes")
    public ResponseEntity<List<CarteResponse>> obtenirMesCartes(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(carteService.obtenirCartesDuClient(email));
    }

    // 2. Request OTP Code
    @PostMapping("/demander-otp")
    public ResponseEntity<Void> demanderOtp(Authentication authentication) {
        String email = authentication.getName();
        carteService.genererEtEnvoyerOtp(email);
        return ResponseEntity.ok().build();
    }

    // 3. Reveal Sensitive Card Details
    @PostMapping("/{carteId}/details")
    public ResponseEntity<CarteResponse> obtenirDetailsCarteAvecOtp(
            @PathVariable String carteId,
            @RequestBody CarteRequestDTO request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(carteService.obtenirDetailsCarteAvecOtp(carteId, request.getCodeOtp(), email));
    }

    // 4. Update Card Controls (Freeze / Online Payment)
    @PatchMapping("/{carteId}/controles")
    public ResponseEntity<CarteResponse> modifierControles(
            @PathVariable String carteId,
            @RequestBody CarteRequestDTO request) {
        return ResponseEntity.ok(carteService.modifierControles(carteId, request));
    }

    // 5. Update Limits (Plafonds)
    @PutMapping("/{carteId}/plafonds")
    public ResponseEntity<CarteResponse> modifierPlafonds(
            @PathVariable String carteId,
            @RequestBody CarteRequestDTO request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(carteService.modifierPlafonds(carteId, request, email));
    }
}