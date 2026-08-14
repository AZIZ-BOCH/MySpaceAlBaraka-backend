package myspace_backend.repository;

import myspace_backend.entity.Carte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarteRepository extends JpaRepository<Carte, Long> {
    Optional<Carte> findByCarteId(String carteId);
    List<Carte> findByClientEmail(String email);
}