package myspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // ex: "BLOCAGE_COMPTE", "DEBLOCAGE_COMPTE", "RESET_MOT_DE_PASSE"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    private String utilisateurEmail; // L'email de l'admin qui a fait l'action

    @Builder.Default
    private LocalDateTime dateAction = LocalDateTime.now();
}