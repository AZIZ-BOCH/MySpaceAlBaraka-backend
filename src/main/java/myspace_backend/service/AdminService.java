package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.dto.response.AdminStatsResponse;
import myspace_backend.dto.response.AdminUtilisateurResponse;
import myspace_backend.entity.AuditLog;
import myspace_backend.entity.Role;
import myspace_backend.entity.Utilisateur;
import myspace_backend.exception.IdentifiantsInvalidesException;
import myspace_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogRepository auditLogRepository;

    // 📊 Statistiques
    @Transactional(readOnly = true)
    public AdminStatsResponse obtenirStatistiques() {
        long nombreClients = clientRepository.count();
        long nombreComptes = compteRepository.count();
        long nombreUtilisateursInscrits = utilisateurRepository.countByRole(Role.CLIENT);
        long nombreTransactions = transactionRepository.count();
        long nombreClientsNonInscrits = nombreClients - nombreUtilisateursInscrits;

        return new AdminStatsResponse(
                nombreClients,
                nombreComptes,
                nombreUtilisateursInscrits,
                nombreTransactions,
                nombreClientsNonInscrits
        );
    }

    // 🔍 Lister et rechercher des utilisateurs clients
    @Transactional(readOnly = true)
    public List<AdminUtilisateurResponse> listerEtChercherUtilisateurs(String recherche) {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();

        return utilisateurs.stream()
                .filter(u -> u.getRole() == Role.CLIENT && u.getClient() != null)
                .filter(u -> {
                    if (recherche == null || recherche.isBlank()) return true;
                    String q = recherche.toLowerCase();
                    return u.getIdentifiant().toLowerCase().contains(q) ||
                            u.getEmail().toLowerCase().contains(q) ||
                            u.getClient().getNom().toLowerCase().contains(q) ||
                            u.getClient().getPrenom().toLowerCase().contains(q) ||
                            u.getClient().getCin().toLowerCase().contains(q);
                })
                .map(u -> new AdminUtilisateurResponse(
                        u.getId(),
                        u.getIdentifiant(),
                        u.getEmail(),
                        u.getClient().getNom(),
                        u.getClient().getPrenom(),
                        u.getClient().getCin(),
                        u.isActif()
                ))
                .collect(Collectors.toList());
    }

    // 🚫 Bloquer / Débloquer
    @Transactional
    public void changerStatutUtilisateur(Long id, boolean actif, String adminEmail) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new IdentifiantsInvalidesException("Utilisateur non trouvé"));

        utilisateur.setActif(actif);
        utilisateurRepository.save(utilisateur);

        String actionType = actif ? "DEBLOCAGE_COMPTE" : "BLOCAGE_COMPTE";
        String description = String.format("L'admin %s a %s le compte de %s (ID: %d)",
                adminEmail, actif ? "débloqué" : "bloqué", utilisateur.getEmail(), utilisateur.getId());

        auditLogRepository.save(AuditLog.builder()
                .action(actionType)
                .description(description)
                .utilisateurEmail(adminEmail)
                .build());
    }

    // 📜 Lister l'historique d'audit
    @Transactional(readOnly = true)
    public List<AuditLog> listerAuditLogs() {
        return auditLogRepository.findAllByOrderByIdDesc();
    }
}