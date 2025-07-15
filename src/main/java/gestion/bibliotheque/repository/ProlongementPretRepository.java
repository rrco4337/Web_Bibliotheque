package gestion.bibliotheque.repository;

import gestion.bibliotheque.model.ProlongementPret;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ProlongementPretRepository extends CrudRepository<ProlongementPret, Long> {
    // Tu peux ajouter des méthodes personnalisées ici si besoin
    List<ProlongementPret> findByStatut_Id(Long statutId);
    int countByPret_IdAndStatut_Id(Long pretId, Long statutId);
}