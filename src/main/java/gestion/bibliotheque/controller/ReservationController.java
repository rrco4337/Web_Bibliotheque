package gestion.bibliotheque.controller;

import gestion.bibliotheque.model.*;
import gestion.bibliotheque.repository.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ExemplaireRepository exemplaireRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private StatutReservationRepository statutReservationRepository;

    // Afficher la page de réservation pour un exemplaire disponible
    @GetMapping("/nouvelle")
    public String afficherFormulaireReservation(@RequestParam("idExemplaire") Long idExemplaire, Model model) {
        Exemplaire exemplaire = exemplaireRepository.findById(idExemplaire).orElse(null);
        if (exemplaire == null) {
            model.addAttribute("message", "Exemplaire introuvable.");
            return "erreur";
        }
        model.addAttribute("exemplaire", exemplaire);
        return "formulaire_reservation";
    }
@PostMapping("/reserver")
    public String reserverExemplaire(
            @RequestParam("idExemplaire") Long idExemplaire,
            HttpSession session, // Injectez la session HTTP
            Model model
    ) {
        // 1. Récupérer l'ID de l'adhérent depuis la session
        Long adherentId = (Long) session.getAttribute("adherentId");

        // 2. Vérifier si l'adhérent est bien connecté
        if (adherentId == null) {
            // Si l'utilisateur n'est pas connecté, le rediriger vers la page de connexion
            return "redirect:/adherents/connexion";
        }

        Exemplaire exemplaire = exemplaireRepository.findById(idExemplaire).orElse(null);
        Adherent adherent = adherentRepository.findById(adherentId).orElse(null);

        if (exemplaire == null || adherent == null) {
            model.addAttribute("message", "Erreur : Exemplaire ou adhérent introuvable.");
            return "erreur"; // ou une page d'erreur appropriée
        }

        // Vérifiez si l'adhérent n'est pas pénalisé et a encore du quota
        if (adherent.getEstPenalise()) {
            model.addAttribute("message", "Vous ne pouvez pas réserver car vous avez une pénalité.");
            return "erreur";
        }

        // ... autres vérifications (quota, etc.)

        if (exemplaire.getStatutPret() != null && "disponible".equalsIgnoreCase(exemplaire.getStatutPret().getNomStatut())) {
            Reservation reservation = new Reservation();
            reservation.setAdherent(adherent);
            reservation.setExemplaire(exemplaire);
            reservation.setDateCreated(LocalDate.now());
            reservation.setDateReservation(LocalDate.now());
            reservation.setIsValidate(false);

            StatutReservation statut = statutReservationRepository.findByNomStatut("en_attente");
            reservation.setStatut(statut);

            reservationRepository.save(reservation);

            model.addAttribute("message", "Réservation effectuée avec succès !");
            return "confirmation_reservation";
        } else {
            model.addAttribute("message", "Cet exemplaire n'est plus disponible pour la réservation.");
            return "erreur";
        }
    }
    @GetMapping("/liste")
    public String listerReservations(Model model) {
        Iterable<Reservation> reservations = reservationRepository.findAll();
        model.addAttribute("reservations", reservations);
        return "liste_reservations";
}

}
