package myspace_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_otp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String code;

    @Enumerated(EnumType.STRING)
    private CanalEnvoi canal; // EMAIL ou SMS

    @Column(nullable = false)
    private LocalDateTime dateExpiration;

    @Builder.Default
    private boolean utilise = false;

    // Nombre de tentatives de saisie incorrectes pour ce code
    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

    // Verrouillé après trop de tentatives incorrectes (empêche toute nouvelle tentative sur ce code)
    @Builder.Default
    @Column(nullable = false)
    private boolean verrouille = false;

    // Lié à l'utilisateur (pour le login)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = true)
    private Utilisateur utilisateur;

    // Lié au client (pour l'inscription)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;
}