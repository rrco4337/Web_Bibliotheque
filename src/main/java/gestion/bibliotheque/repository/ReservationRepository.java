package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.Adherent;
import gestion.bibliotheque.model.Exemplaire;
import gestion.bibliotheque.model.Reservation;
import gestion.bibliotheque.model.StatutReservation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByAdherentAndExemplaire(Adherent adherent, Exemplaire exemplaire);
    List<Reservation> findByStatut(StatutReservation statut);
    boolean existsByExemplaireAndStatutNomStatut(Exemplaire exemplaire, String nomStatut);
}