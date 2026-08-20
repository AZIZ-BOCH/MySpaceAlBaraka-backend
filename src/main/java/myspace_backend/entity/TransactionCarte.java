package myspace_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_carte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCarte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description; // Ex: "Paiement en ligne - Amazon", "Retrait DAB"

    @Column(nullable = false)
    private BigDecimal montant; // Ex: 45.500

    @Column(nullable = false)
    private LocalDateTime dateTransaction = LocalDateTime.now();

    @Column(nullable = false)
    private String statut; // Ex: "VALIDEE", "REFUSEE", "EN_ATTENTE"

    @Column(nullable = false)
    private String type; // Ex: "PAIEMENT", "RETRAIT"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carte_id", nullable = false)
    private Carte carte;
}