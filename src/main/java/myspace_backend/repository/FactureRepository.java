package myspace_backend.repository;

import myspace_backend.entity.Facture;
import myspace_backend.entity.OrganismeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    Optional<Facture> findByOrganismeAndReferenceFacture(OrganismeType organisme, String referenceFacture);

    List<Facture> findByClient_Id(Long clientId);

    List<Facture> findByClient_IdAndPayeeFalse(Long clientId);
}