package myspace_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.BeneficiaireRequest;
import myspace_backend.dto.response.BeneficiaireResponse;
import myspace_backend.service.BeneficiaireService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaires")
@RequiredArgsConstructor
public class BeneficiaireController {

    private final BeneficiaireService beneficiaireService;

    @GetMapping
    public ResponseEntity<List<BeneficiaireResponse>> listerBeneficiaires(Authentication authentication) {
        return ResponseEntity.ok(beneficiaireService.listerBeneficiaires(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<BeneficiaireResponse> ajouterBeneficiaire(
            Authentication authentication,
            @Valid @RequestBody BeneficiaireRequest request) {
        return ResponseEntity.ok(beneficiaireService.ajouterBeneficiaire(authentication.getName(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerBeneficiaire(
            Authentication authentication,
            @PathVariable Long id) {
        beneficiaireService.supprimerBeneficiaire(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}