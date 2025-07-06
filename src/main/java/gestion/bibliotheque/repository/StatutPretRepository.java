package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.StatutPret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatutPretRepository extends JpaRepository<StatutPret, Long> {

     StatutPret findByNomStatut(String nomStatut);
}
