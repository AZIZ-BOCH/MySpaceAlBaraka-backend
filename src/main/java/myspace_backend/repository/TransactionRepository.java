package myspace_backend.repository;

import myspace_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCompte_RibAndDateOperationBetween(String rib, LocalDate debut, LocalDate fin);

    // 👈 Pour récupérer la toute dernière transaction et en déduire le solde actuel
    Optional<Transaction> findTopByCompte_IdOrderByDateOperationDescIdDesc(Long compteId);

    // 👈 NOUVEAU : Pour récupérer le solde à une date donnée (fin de mois)
    Optional<Transaction> findTopByCompte_IdAndDateOperationLessThanEqualOrderByDateOperationDescIdDesc(
            Long compteId, LocalDate date);
}