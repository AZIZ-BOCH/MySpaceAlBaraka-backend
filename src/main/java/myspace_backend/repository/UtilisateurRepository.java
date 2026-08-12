package myspace_backend.repository;

import myspace_backend.entity.Role;
import myspace_backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByIdentifiantOrEmail(String identifiant, String email);

    boolean existsByIdentifiant(String identifiant);

    boolean existsByEmail(String email);

    boolean existsByClient_Id(Long clientId);

    long countByRole(Role role);
}