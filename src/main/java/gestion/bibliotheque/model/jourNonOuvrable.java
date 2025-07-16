package gestion.bibliotheque.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "jourNonOuvrable")
public class jourNonOuvrable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_jour_non_ouvrable", nullable = false)
    private LocalDate dateJourNonOuvrable;

    @Column(name = "description", length = 255)
    private String description;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateJourNonOuvrable() {
        return dateJourNonOuvrable;
    }

    public void setDateJourNonOuvrable(LocalDate dateJourNonOuvrable) {
        this.dateJourNonOuvrable = dateJourNonOuvrable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}