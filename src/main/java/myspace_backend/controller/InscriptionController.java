package myspace_backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import myspace_backend.dto.request.ConfirmerInscriptionRequest;
import myspace_backend.dto.request.EnvoyerCodeRequest;
import myspace_backend.dto.request.VerifierIdentiteRequest;
import myspace_backend.dto.response.InscriptionResponse;
import myspace_backend.dto.response.VerifierIdentiteResponse;
import myspace_backend.service.InscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscription")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @PostMapping("/verifier-identite")
    public ResponseEntity<VerifierIdentiteResponse> verifierIdentite(
            @Valid @RequestBody VerifierIdentiteRequest request) {

        VerifierIdentiteResponse response = inscriptionService.verifierIdentite(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/envoyer-code")
    public ResponseEntity<Void> envoyerCode(
            @Valid @RequestBody EnvoyerCodeRequest request) {

        inscriptionService.envoyerCodeVerification(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/confirmer")
    public ResponseEntity<InscriptionResponse> confirmer(
            @Valid @RequestBody ConfirmerInscriptionRequest request) {

        InscriptionResponse response = inscriptionService.confirmerInscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}