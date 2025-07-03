package gestion.bibliotheque.controller;

import gestion.bibliotheque.model.Pret;
import gestion.bibliotheque.model.Livre;

import gestion.bibliotheque.model.Adherent;
import gestion.bibliotheque.model.Exemplaire;
import gestion.bibliotheque.model.TypePret;
import gestion.bibliotheque.model.StatutPret;
import gestion.bibliotheque.model.TypeAdherent;
import gestion.bibliotheque.repository.AdherentRepository;
import gestion.bibliotheque.repository.ExemplaireRepository;
import gestion.bibliotheque.repository.PretRepository;
import gestion.bibliotheque.repository.TypePretRepository;
import gestion.bibliotheque.repository.LivreRepository;

import gestion.bibliotheque.repository.StatutPretRepository;
import gestion.bibliotheque.service.AdherentRestrictionException;
import gestion.bibliotheque.service.AdherentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/prets")
public class PretController {

    @Autowired
    private PretRepository pretRepository;

    @Autowired
    private LivreRepository livreRepository;

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private ExemplaireRepository exemplaireRepository;

    @Autowired
    private TypePretRepository typePretRepository;

    @Autowired
    private StatutPretRepository statutPretRepository;

    @Autowired
    private AdherentService adherentService;

    @GetMapping("/ajouter")
    public String afficherFormulaireAjoutPret(Model model) {
        model.addAttribute("pret", new Pret());
        model.addAttribute("adherents", adherentRepository.findAll());
        model.addAttribute("exemplaires", exemplaireRepository.findAll());
        model.addAttribute("typesPret", typePretRepository.findAll());
        model.addAttribute("statuts", statutPretRepository.findAll());
        return "ajouter_pret";
    }

    // @PostMapping("/ajouter")
    // public String enregistrerPret(@ModelAttribute Pret pret) {
    //     pretRepository.save(pret);
    //     return "redirect:/prets/liste";
    // }

 
    @GetMapping("/NewPret")
    public String ajouterPret(@RequestParam("idExemplaire") Long idExemplaire, Model model) {
        // System.out.println("ID de l'exemplaire sélectionné : " + idExemplaire);

        model.addAttribute("idExemplaire", idExemplaire);

         model.addAttribute("pret", new Pret());
        model.addAttribute("adherents", adherentRepository.findAll());
        // model.addAttribute("exemplaires", exemplaireRepository.findAll());
        model.addAttribute("typesPret", typePretRepository.findAll());
        model.addAttribute("statuts", statutPretRepository.findAll());
        return "ajouter_pret";

        // return "formulaire-pret"; // par exemple
    }

    @GetMapping("/liste")
    public String listerPrets(Model model) {
        model.addAttribute("prets", pretRepository.findAll());
        return "liste_prets";
    }

@PostMapping("/ajouter")
public String enregistrerPret(@ModelAttribute Pret pret, @RequestParam("idExemplaire") Long idExemplaire) {

    Exemplaire exemplaire = exemplaireRepository.findById(idExemplaire).orElse(null);
    if (exemplaire == null) {
        System.out.println("Exemplaire non trouvé");
        return "redirect:/prets/liste"; // ou afficher une erreur
    }

    pret.setExemplaire(exemplaire);
    Adherent adherent = pret.getAdherent();

    if (adherent == null) {
        System.out.println("Adhérent non fourni dans le formulaire");
        return "redirect:/prets/liste"; // ou afficher une erreur
    }

    Livre livre = exemplaire.getLivre();
    if (livre == null) {
        System.out.println("Livre introuvable pour l'exemplaire");
        return "redirect:/prets/liste";
    }

    try {
        // Vérification de l'abonnement
        TypeAdherent typeAdherent = adherentService.verifRestriction(adherent.getId());

        // Vérification de l'âge
        int ageAdherent = adherent.getAge(); // méthode getAge() doit être définie dans Adherent
        int ageRestriction = livre.getAgeRestriction() != null ? livre.getAgeRestriction() : 0;
            System.out.println("---"+ageAdherent +"-"+ageRestriction+"---" );

        if (ageAdherent < ageRestriction) {
            System.out.println("Âge insuffisant pour emprunter ce livre." );
            return "redirect:/prets/liste";
        }

        // Calculer la date de retour prévue
        int dureePret = typeAdherent.getDureeMaxPret(); // assure-toi qu'elle existe
        if (pret.getDatePret() != null && pret.getTypePret() != null) {
            pret.setDateRetourPrevue(pret.getDatePret().plusDays(dureePret));
        }

        // Sauvegarder
        pretRepository.save(pret);
        return "redirect:/prets/liste";

    } catch (AdherentRestrictionException e) {
        System.out.println("Erreur : " + e.getMessage());
        return "redirect:/prets/liste";
    }
}


}


 