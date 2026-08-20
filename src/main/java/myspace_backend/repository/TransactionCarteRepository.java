package myspace_backend.repository;

import myspace_backend.entity.TransactionCarte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionCarteRepository extends JpaRepository<TransactionCarte, Long> {

    // Pour récupérer les dernières transactions d'une carte spécifique (ex: les 10 plus récentes)
    List<TransactionCarte> findTop10ByCarteIdOrderByDateTransactionDesc(Long carteId);
}