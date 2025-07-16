package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.jourNonOuvrable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface JourNonOuvrableRepository extends JpaRepository<jourNonOuvrable, Long> {
    List<jourNonOuvrable> findByDateJourNonOuvrable(LocalDate dateJourNonOuvrable);
    boolean existsByDateJourNonOuvrable(LocalDate dateJourNonOuvrable);
}