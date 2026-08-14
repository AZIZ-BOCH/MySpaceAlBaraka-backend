package myspace_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "carte")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String carteId;

    @Column(nullable = false)
    private String numeroMasque; // e.g. "•••• •••• •••• 4289"

    @Column(nullable = false)
    private String titulaire;

    @Column(nullable = false)
    private String dateExpiration;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private boolean estGelee = false;

    @Column(nullable = false)
    private boolean paiementsEnLigne = true;

    @Column(nullable = false)
    private boolean paiementsInternationaux = false;

    @Column(nullable = false)
    private BigDecimal plafondRetrait = new BigDecimal("1000.00");

    @Column(nullable = false)
    private BigDecimal plafondPaiement = new BigDecimal("3000.00");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}