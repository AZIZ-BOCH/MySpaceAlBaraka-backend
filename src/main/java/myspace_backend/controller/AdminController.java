package myspace_backend.controller;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.response.AdminStatsResponse;
import myspace_backend.dto.response.AdminUtilisateurResponse;
import myspace_backend.entity.AuditLog;
import myspace_backend.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/statistiques")
    public ResponseEntity<AdminStatsResponse> obtenirStatistiques() {
        AdminStatsResponse stats = adminService.obtenirStatistiques();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/utilisateurs")
    public ResponseEntity<List<AdminUtilisateurResponse>> listerUtilisateurs(
            @RequestParam(required = false, defaultValue = "") String q) {
        return ResponseEntity.ok(adminService.listerEtChercherUtilisateurs(q));
    }

    @PatchMapping("/utilisateurs/{id}/statut")
    public ResponseEntity<Void> changerStatut(
            @PathVariable Long id,
            @RequestParam boolean actif,
            Authentication authentication) {
        adminService.changerStatutUtilisateur(id, actif, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> listerAuditLogs() {
        return ResponseEntity.ok(adminService.listerAuditLogs());
    }
}