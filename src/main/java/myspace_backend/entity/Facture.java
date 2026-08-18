package myspace_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "facture")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrganismeType organisme;

    @Column(name = "reference_facture", nullable = false)
    private String referenceFacture;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal montant;

    @Column(nullable = false)
    private boolean payee = false;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(name = "recu_reference")
    private String recuReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}