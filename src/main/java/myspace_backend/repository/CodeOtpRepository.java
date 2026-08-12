package myspace_backend.repository;

import myspace_backend.entity.CodeOtp;
import myspace_backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeOtpRepository extends JpaRepository<CodeOtp, Long> {

    // Pour le LOGIN (recherche par utilisateur)
    Optional<CodeOtp> findTopByUtilisateurAndUtiliseFalseOrderByDateExpirationDesc(Utilisateur utilisateur);

    // Pour l'INSCRIPTION (recherche par client)
    Optional<CodeOtp> findFirstByClient_IdAndCodeAndUtiliseFalseOrderByIdDesc(Long clientId, String code);

    // Invalider les anciens codes d'un utilisateur
    @Modifying
    @Query("UPDATE CodeOtp c SET c.utilise = true WHERE c.utilisateur.id = :utilisateurId AND c.utilise = false")
    void invaliderAnciensOtpsUtilisateur(Long utilisateurId);

    // Invalider les anciens codes d'un client
    @Modifying
    @Query("UPDATE CodeOtp c SET c.utilise = true WHERE c.client.id = :clientId AND c.utilise = false")
    void invaliderAnciensOtpsClient(Long clientId);
}