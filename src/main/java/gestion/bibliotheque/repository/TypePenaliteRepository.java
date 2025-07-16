package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.TypeAdherent;
import gestion.bibliotheque.model.TypePenalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypePenaliteRepository extends JpaRepository<TypePenalite, Long> {
    TypePenalite findByNomTypePenaliteAndTypeAdherent(String nomTypePenalite, TypeAdherent typeAdherent);
    TypePenalite findByNomTypePenalite(String nomTypePenalite);
}
