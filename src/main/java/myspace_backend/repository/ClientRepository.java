package myspace_backend.repository;

import myspace_backend.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByCin(String cin);

    Optional<Client> findByPasseport(String passeport);

    boolean existsByCin(String cin);

    boolean existsByPasseport(String passeport);
}