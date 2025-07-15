package gestion.bibliotheque.controller;

import gestion.bibliotheque.model.*;
import gestion.bibliotheque.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/prolongements")
public class ProlongementController {

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private ProlongementPretRepository prolongementPretRepository;

    @Autowired
    private StatutPretRepository statutPretRepository;
    @PostMapping("/demander")
    public String demanderProlongement(@RequestParam("idPret") Long idPret, HttpSession session, Model model) {
    Long adherentId = (Long) session.getAttribute("adherentId");
    Pret pret = pretRepository.findById(idPret).orElse(null);

    if (pret == null || !pret.getAdherent().getId().equals(adherentId)) {
        model.addAttribute("message", "Prêt introuvable ou non autorisé.");
        System.err.println("Prêt introuvable ou non autorisé pour l'ID : " + idPret);
        return "erreur";
    }

    // Vérifier si déjà prolongé, ou autres règles métier ici

    ProlongementPret prolongement = new ProlongementPret();
    prolongement.setPret(pret);
    prolongement.setDateProlongement(LocalDate.now());
    
    //calculer la date de retour prévue s
    TypeAdherent typeAdherent = pret.getAdherent().getTypeAdherent();
    int dureeProlongement = typeAdherent.getDureeMaxPret();
    prolongement.setDateRetourPrevue(pret.getDateRetourPrevue().plusDays(dureeProlongement));
    // TypeAdherent typeAdherent = pret.getAdherent().getTypeAdherent();
    int quotaMax = typeAdherent.getQuotaMaxProlongement();
    long nbProlongements = prolongementPretRepository.countByPret_Adherent_IdAndStatut_Id(adherentId, 2L); // 2 = confirmé

if (nbProlongements >= quotaMax) {
    model.addAttribute("message", "Quota de prolongements atteint.");
    return "erreur";
}
  
    StatutPret statut = statutPretRepository.findById(1L).orElse(null);
    prolongement.setStatut(statut);

    prolongementPretRepository.save(prolongement);

    model.addAttribute("message", "Demande de prolongement envoyée !");
    return "demande_prolongement";
}
@GetMapping("/liste-attente")
public String listerProlongementsEnAttente(Model model) {
    // 1L = statut "en attente"
    List<ProlongementPret> prolongements = prolongementPretRepository.findByStatut_Id(1L);
    model.addAttribute("prolongements", prolongements);
    return "liste_prolongements";
}
@GetMapping("/refuser/{id}")
public String refuserProlongement(@PathVariable("id") Long id) {
    ProlongementPret prolongement = prolongementPretRepository.findById(id).orElse(null);
    if (prolongement != null) {
        StatutPret statutRefuse = statutPretRepository.findById(3L).orElse(null); // 3 = refusé
        if (statutRefuse != null) {
            prolongement.setStatut(statutRefuse);
            prolongementPretRepository.save(prolongement);
        }
    }
    return "redirect:/prolongements/liste-attente";
}@GetMapping("/confirmer/{id}")
public String confirmerProlongement(@PathVariable("id") Long id) {
    ProlongementPret prolongement = prolongementPretRepository.findById(id).orElse(null);
    if (prolongement != null) {
        Pret pretInitial = prolongement.getPret();
        Adherent adherent = pretInitial.getAdherent();
        TypeAdherent typeAdherent = adherent.getTypeAdherent();
        int quotaMax = typeAdherent.getQuotaMaxProlongement();
        int nbProlongements = prolongementPretRepository.countByPret_IdAndStatut_Id(pretInitial.getId(), 2L); // 2 = confirmé

        if (nbProlongements >= quotaMax) {
        
            return "redirect:/prolongements/liste-attente?error=quota";
        }

        int dureeProlongement = typeAdherent.getDureeMaxPret();
        LocalDate nouvelleDateRetour = pretInitial.getDateRetourPrevue().plusDays(dureeProlongement);

       
        pretInitial.setDateRetourPrevue(nouvelleDateRetour);
        pretRepository.save(pretInitial);

        StatutPret statutConfirme = statutPretRepository.findById(2L).orElse(null); // 2 = confirmé
        prolongement.setStatut(statutConfirme);
        prolongementPretRepository.save(prolongement);
    }
    return "redirect:/prolongements/liste-attente";
}
}