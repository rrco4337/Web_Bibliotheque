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
//     @PostMapping("/reserver")
//     public String reserverExemplaire(
//         @RequestParam("idExemplaire") Long idExemplaire,
//         HttpSession session,
//         Model model
// ) {
//     Long adherentId = (Long) session.getAttribute("adherentId");
//     if (adherentId == null) {
//         System.err.println("Erreur: Aucun ID adherent dans la session - redirection vers la connexion");
//         return "redirect:/adherents/connexion";
//     }

//     Exemplaire exemplaire = exemplaireRepository.findById(idExemplaire).orElse(null);
//     Adherent adherent = adherentRepository.findById(adherentId).orElse(null);

//     if (exemplaire == null) {
//         System.err.println("Erreur: Exemplaire introuvable pour l'ID " + idExemplaire);
//         model.addAttribute("message", "Erreur : Exemplaire introuvable.");
//         return "erreur";
//     }
    
//     if (adherent == null) {
//         System.err.println("Erreur: Adherent introuvable pour l'ID " + adherentId);
//         model.addAttribute("message", "Erreur : Adhérent introuvable.");
//         return "erreur";
//     }

//     if (adherent.getEstPenalise()) {
//         System.err.println("Erreur: Adherent " + adherentId + " est penalisé - reservation impossible");
//         model.addAttribute("message", "Vous ne pouvez pas réserver car vous avez une pénalité.");
//         return "erreur";
//     }

//     String statutPret = exemplaire.getStatutPret().getNomStatut();
//     System.out.println("Statut de l'exemplaire " + idExemplaire + ": " + statutPret);

//     if ("Confirmée".equalsIgnoreCase(statutPret) || "indisponible".equalsIgnoreCase(statutPret)) {
//         boolean dejaReserve = reservationRepository.existsByAdherentAndExemplaire(adherent, exemplaire);
//         if (dejaReserve) {
//             System.err.println("Erreur: Adherent " + adherentId + " a déjà réservé l'exemplaire " + idExemplaire);
//             model.addAttribute("message", "Vous avez déjà réservé cet exemplaire.");
//             return "erreur";
//         }

//         try {
//             Reservation reservation = new Reservation();
//             reservation.setAdherent(adherent);
//             reservation.setExemplaire(exemplaire);
//             // reservation.setDateReservation(LocalDate.now());  // instead of setDateCreated
//             reservation.setDateCreated(LocalDate.now());
// reservation.setDateReservation(LocalDate.now());
//             reservation.setIsValidate(false);

//             StatutReservation statutReservation = statutReservationRepository.findByNomStatut("En attente");
//             if (statutReservation == null) {
//                 System.err.println("Erreur critique: Statut 'en attente' introuvable dans la base de données");
//                 model.addAttribute("message", "Erreur système: Statut de réservation 'en attente' introuvable.");
//                 return "erreur";
//             }
//             reservation.setStatut(statutReservation);

//             reservationRepository.save(reservation);
//             System.out.println("Réservation créée avec succès pour l'exemplaire " + idExemplaire + " par l'adhérent " + adherentId);

//             model.addAttribute("message", "Réservation mise en file d'attente avec succès !");
//             return "confirmation_reservation";

//         } catch (Exception e) {
//             System.err.println("Erreur lors de la création de la réservation: " + e.getMessage());
//             e.printStackTrace();
//             model.addAttribute("message", "Une erreur technique est survenue lors de la réservation.");
//             return "erreur";
//         }

//     } else {
//         System.err.println("Erreur: Exemplaire " + idExemplaire + " non réservable - statut: " + statutPret);
//         model.addAttribute("message", "Cet exemplaire n'est pas réservable pour le moment.");
//         return "erreur";
//     }
// }
@PostMapping("/reserver")
public String reserverExemplaire(
    @RequestParam("idExemplaire") Long idExemplaire,
    HttpSession session,
    Model model
) {
    Long adherentId = (Long) session.getAttribute("adherentId");
    if (adherentId == null) {
        System.err.println("Erreur: Aucun ID adherent dans la session - redirection vers la connexion");
        return "redirect:/adherents/connexion";
    }

    Exemplaire exemplaire = exemplaireRepository.findById(idExemplaire).orElse(null);
    Adherent adherent = adherentRepository.findById(adherentId).orElse(null);

    if (exemplaire == null) {
        System.err.println("Erreur: Exemplaire introuvable pour l'ID " + idExemplaire);
        model.addAttribute("message", "Erreur : Exemplaire introuvable.");
        return "erreur";
    }
    
    if (adherent == null) {
        System.err.println("Erreur: Adherent introuvable pour l'ID " + adherentId);
        model.addAttribute("message", "Erreur : Adhérent introuvable.");
        return "erreur";
    }

    if (adherent.getEstPenalise()) {
        System.err.println("Erreur: Adherent " + adherentId + " est penalisé - reservation impossible");
        model.addAttribute("message", "Vous ne pouvez pas réserver car vous avez une pénalité.");
        return "erreur";
    }

    // Vérifier si l'exemplaire a déjà une réservation confirmée
    boolean dejaReserveConfirme = reservationRepository.existsByExemplaireAndStatutNomStatut(
        exemplaire, "Confirmée");
    if (dejaReserveConfirme) {
        System.err.println("Erreur: Exemplaire " + idExemplaire + " a déjà une réservation confirmée");
        model.addAttribute("message", "Cet exemplaire a déjà une réservation confirmée et ne peut pas être réservé.");
        return "erreur";
    }

    // Vérifier si l'adhérent a déjà réservé cet exemplaire (quel que soit le statut)
    boolean dejaReserveParAdherent = reservationRepository.existsByAdherentAndExemplaire(adherent, exemplaire);
    if (dejaReserveParAdherent) {
        System.err.println("Erreur: Adherent " + adherentId + " a déjà réservé l'exemplaire " + idExemplaire);
        model.addAttribute("message", "Vous avez déjà réservé cet exemplaire.");
        return "erreur";
    }

    try {
        Reservation reservation = new Reservation();
        reservation.setAdherent(adherent);
        reservation.setExemplaire(exemplaire);
        reservation.setDateCreated(LocalDate.now());
        reservation.setDateReservation(LocalDate.now());
        reservation.setIsValidate(false);

        StatutReservation statutReservation = statutReservationRepository.findByNomStatut("En attente");
        if (statutReservation == null) {
            System.err.println("Erreur critique: Statut 'en attente' introuvable dans la base de données");
            model.addAttribute("message", "Erreur système: Statut de réservation 'en attente' introuvable.");
            return "erreur";
        }
        reservation.setStatut(statutReservation);

        reservationRepository.save(reservation);
        System.out.println("Réservation créée avec succès pour l'exemplaire " + idExemplaire + " par l'adhérent " + adherentId);

        model.addAttribute("message", "Réservation mise en file d'attente avec succès !");
        return "confirmation_reservation";

    } catch (Exception e) {
        System.err.println("Erreur lors de la création de la réservation: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("message", "Une erreur technique est survenue lors de la réservation.");
        return "erreur";
    }
}
    @GetMapping("/liste")
public String listerReservations(Model model) {
    // Récupérer le statut "En attente" depuis la base de données
    StatutReservation statutEnAttente = statutReservationRepository.findByNomStatut("En attente");
    
    if (statutEnAttente == null) {
        model.addAttribute("message", "Le statut 'En attente' n'existe pas dans la base de données");
        return "erreur";
    }
    
    // Récupérer uniquement les réservations avec le statut "En attente"
    Iterable<Reservation> reservations = reservationRepository.findByStatut(statutEnAttente);
    model.addAttribute("reservations", reservations);
    return "liste_reservations";
}
    
    @GetMapping("/accepter/{id}")
    public String accepterReservation(@PathVariable Long id, Model model) {
        try {
            Reservation reservation = reservationRepository.findById(id).orElse(null);
            if (reservation == null) {
                model.addAttribute("message", "Réservation introuvable.");
                return "erreur";
            }

            StatutReservation statutConfirme = statutReservationRepository.findByNomStatut("Confirmée");
            if (statutConfirme == null) {
                model.addAttribute("message", "Erreur système: Statut de réservation 'Confirmée' introuvable.");
                return "erreur";
            }

            reservation.setStatut(statutConfirme);
            reservation.setIsValidate(true);
            reservationRepository.save(reservation);

            return "redirect:/reservations/liste";
        } catch (Exception e) {
            System.err.println("Erreur lors de l'acceptation de la réservation: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("message", "Une erreur technique est survenue lors de l'acceptation de la réservation.");
            return "erreur";
        }
    }

    @GetMapping("/refuser/{id}")
    public String refuserReservation(@PathVariable Long id, Model model) {
        try {
            Reservation reservation = reservationRepository.findById(id).orElse(null);
            if (reservation == null) {
                model.addAttribute("message", "Réservation introuvable.");
                return "erreur";
            }

            StatutReservation statutAnnulee = statutReservationRepository.findByNomStatut("Annulée");
            if (statutAnnulee == null) {
                model.addAttribute("message", "Erreur système: Statut de réservation 'Annulée' introuvable.");
                return "erreur";
            }

            reservation.setStatut(statutAnnulee);
            reservation.setIsValidate(false);
            reservationRepository.delete(reservation);

            return "redirect:/reservations/liste";
        } catch (Exception e) {
            System.err.println("Erreur lors du refus de la réservation: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("message", "Une erreur technique est survenue lors du refus de la réservation.");
            return "erreur";
        }
    }
}
