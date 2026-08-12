package myspace_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_operation", nullable = false)
    private LocalDate dateOperation;

    @Column(nullable = false)
    private String libelle;

    private BigDecimal debit;

    private BigDecimal credit;

    @Column(nullable = false)
    private BigDecimal solde;

    @ManyToOne
    @JoinColumn(name = "compte_id", nullable = false)
    private Compte compte;
}