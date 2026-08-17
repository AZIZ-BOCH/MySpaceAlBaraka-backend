package myspace_backend.repository;

import myspace_backend.entity.Beneficiaire;
import myspace_backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaireRepository extends JpaRepository<Beneficiaire, Long> {
    List<Beneficiaire> findByClient(Client client);
    Optional<Beneficiaire> findByClientAndRib(Client client, String rib);
}