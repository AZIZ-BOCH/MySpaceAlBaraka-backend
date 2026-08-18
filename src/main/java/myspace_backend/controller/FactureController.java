package myspace_backend.controller;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.FactureConsultationRequest;
import myspace_backend.dto.request.PaiementFactureRequest;
import myspace_backend.dto.response.FactureResponse;
import myspace_backend.service.FactureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @PostMapping("/consulter")
    public ResponseEntity<FactureResponse> consulterFacture(Authentication authentication, @RequestBody FactureConsultationRequest request) {
        return ResponseEntity.ok(factureService.consulterFacture(authentication.getName(), request));
    }

    @PostMapping("/demander-otp")
    public ResponseEntity<?> demanderOtpFacture(Authentication authentication) {
        try {
            factureService.demanderOtpFacture(authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Code OTP envoyé pour le règlement de la facture."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/payer")
    public ResponseEntity<FactureResponse> payerFacture(Authentication authentication, @RequestBody PaiementFactureRequest request) {
        return ResponseEntity.ok(factureService.payerFacture(authentication.getName(), request));
    }

    @GetMapping("/mes-factures")
    public ResponseEntity<List<FactureResponse>> getMesFactures(Authentication authentication) {
        return ResponseEntity.ok(factureService.getMesFactures(authentication.getName()));
    }
}