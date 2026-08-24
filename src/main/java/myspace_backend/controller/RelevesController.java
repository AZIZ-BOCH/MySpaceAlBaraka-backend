package myspace_backend.controller;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.response.CompteResponse;
import myspace_backend.dto.response.SoldeMensuelResponse;
import myspace_backend.dto.response.TransactionResponse;
import myspace_backend.service.RelevesService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RelevesController {

    private final RelevesService relevesService;

    @GetMapping("/api/comptes")
    public ResponseEntity<List<CompteResponse>> listerComptes(Authentication authentication) {
        String email = authentication.getName();
        List<CompteResponse> comptes = relevesService.listerComptes(email);
        return ResponseEntity.ok(comptes);
    }

    @GetMapping("/api/transactions")
    public ResponseEntity<List<TransactionResponse>> consulterReleve(
            Authentication authentication,
            @RequestParam String rib,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mois) {

        String email = authentication.getName();
        List<TransactionResponse> transactions = relevesService.consulterReleve(email, rib, mois);
        return ResponseEntity.ok(transactions);
    }

    // 👈 NOUVEAU : Évolution du solde pour le graphique du dashboard
    @GetMapping("/api/comptes/evolution-solde")
    public ResponseEntity<List<SoldeMensuelResponse>> obtenirEvolutionSolde(
            Authentication authentication,
            @RequestParam String rib) {

        String email = authentication.getName();
        List<SoldeMensuelResponse> evolution = relevesService.obtenirEvolutionSolde(email, rib);
        return ResponseEntity.ok(evolution);
    }

    @GetMapping("/api/transactions/pdf")
    public ResponseEntity<InputStreamResource> telechargerPdfReleve(
            Authentication authentication,
            @RequestParam String rib,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mois) {

        String email = authentication.getName();
        ByteArrayInputStream pdfStream = relevesService.genererPdfReleve(email, rib, mois);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=releve_" + mois.toString() + ".pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}