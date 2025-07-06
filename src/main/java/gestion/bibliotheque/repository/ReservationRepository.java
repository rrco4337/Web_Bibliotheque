package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // Tu peux ajouter d'autres méthodes personnalisées ici si besoin
}