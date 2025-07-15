package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.Abonne;
import gestion.bibliotheque.model.Adherent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbonneRepository extends JpaRepository<Abonne, Long> {
    Abonne findByAdherent(Adherent adherent);
}