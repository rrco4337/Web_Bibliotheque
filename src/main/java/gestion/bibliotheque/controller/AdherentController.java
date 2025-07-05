package gestion.bibliotheque.controller;

import gestion.bibliotheque.model.TypeAdherent;
import gestion.bibliotheque.model.Adherent;

import gestion.bibliotheque.repository.AdherentRepository  ;
import gestion.bibliotheque.repository.PretRepository;
import gestion.bibliotheque.repository.TypeAdherentRepository  ;
import gestion.bibliotheque.service.TypeAdherentService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.beans.PropertyEditorSupport;

@Controller
@RequestMapping("/adherents")
public class AdherentController {

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private TypeAdherentRepository typeAdherentRepository;
    
    @Autowired
    private PretRepository pretRepository;

    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(Model model) {
        model.addAttribute("adherent", new Adherent());
        model.addAttribute("types", typeAdherentRepository.findAll());
        return "user";
    }

    @PostMapping("/ajouter")
    public String ajouterAdherent(@ModelAttribute Adherent adherent) {
        adherentRepository.save(adherent);
        return "redirect:listeAdherent"; // ou vers la page d'accueil
    }

    @GetMapping("/listeAdherent")
    public String listeAdherents(Model model) {
        model.addAttribute("adherents", adherentRepository.findAll());
        return "listeAdherent";
    }
    @GetMapping("/connexion")
    public String afficherConnexion() {
        return "connexionAdherent"; // nom de ta page de connexion (HTML)
    }
    @PostMapping("/connexion")
public String connexionAdherent(@RequestParam("prenom") String prenom,
                               @RequestParam("password") String password,
                               HttpSession session,
                               Model model) {
    Adherent adherent = adherentRepository.findByPrenomAndPassword(prenom, password);
    if (adherent != null) {
        session.setAttribute("adherentId", adherent.getId());
        return "redirect:/adherents/accueil";
    } else {
        model.addAttribute("erreur", "Prénom ou mot de passe incorrect");
        return "connexionAdherent";
    }
    
}
@GetMapping("/accueil")
public String accueilAdherent(Model model, HttpSession session) {
    Long adherentId = (Long) session.getAttribute("adherentId");
    if (adherentId != null) {
        Adherent adherent = adherentRepository.findById(adherentId).orElse(null);
        model.addAttribute("adherent", adherent);

        // Nombre de livres empruntés en cours
        int nbPrets = pretRepository.countByAdherentAndDateRetourReelleIsNull(adherent);
        model.addAttribute("nbPrets", nbPrets);

        // Quota max selon le type
        int quotaMax = adherent.getTypeAdherent().getQuotaMaxPret();
        model.addAttribute("quotaMax", quotaMax);

        // Quota restant
        int quotaRestant = quotaMax - nbPrets;
        model.addAttribute("quotaRestant", quotaRestant);

        // Statut pénalité (true/false)
        model.addAttribute("estPenalise", adherent.getEstPenalise());

        return "accueil";
    } else {
        return "redirect:/adherents/connexion";
    }
}

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(TypeAdherent.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                Long id = Long.parseLong(text);
                TypeAdherent type = typeAdherentRepository.findById(id).orElse(null);
                setValue(type);
            }
        });
    }
}
