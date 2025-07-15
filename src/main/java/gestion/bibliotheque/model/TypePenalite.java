package gestion.bibliotheque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Type_Penalite")
public class TypePenalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_penalite")
    private Long id;

    @Column(name = "nom_type_penalite", length = 100)
    private String nomTypePenalite;

    @Column(columnDefinition = "TEXT")
    private String description;

@Column(name = "durre_penalite")
private Integer dureePenalite; // durée en jours

public Integer getDureePenalite() {
    return dureePenalite;
}

public void setDureePenalite(Integer dureePenalite) {
    this.dureePenalite = dureePenalite;
}
    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomTypePenalite() {
        return nomTypePenalite;
    }

    public void setNomTypePenalite(String nomTypePenalite) {
        this.nomTypePenalite = nomTypePenalite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
