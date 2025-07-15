package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.Adherent;
import gestion.bibliotheque.model.Penalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PenaliteRepository extends JpaRepository<Penalite, Long> {
    List<Penalite> findByAdherentId(Long adherentId);
    @Query("SELECT p FROM Penalite p WHERE p.adherent = :adherent AND :currentDate BETWEEN p.dateDebut AND p.dateFin")
    List<Penalite> findActivePenalitesForAdherent(@Param("adherent") Adherent adherent, @Param("currentDate") LocalDate currentDate);
    List<Penalite> findByAdherent(Adherent adherent);
}
