package myspace_backend.service;

import lombok.RequiredArgsConstructor;
import myspace_backend.entity.AuditLog;
import myspace_backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void enregistrer(String action, String description, String userEmail) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .description(description)
                .utilisateurEmail(userEmail)
                .dateAction(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}