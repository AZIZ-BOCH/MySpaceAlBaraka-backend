package myspace_backend.repository;

import myspace_backend.entity.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompteRepository extends JpaRepository<Compte, Long> {

    Optional<Compte> findByRib(String rib);

    boolean existsByRib(String rib);

    boolean existsByRibAndClient_Cin(String rib, String cin);

    boolean existsByRibAndClient_Passeport(String rib, String passeport);

    List<Compte> findByClient_Id(Long clientId);

    Optional<Compte> findByRibAndClient_Id(String rib, Long clientId);
}