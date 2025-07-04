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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
    StatutPret statutIndisponible = statutPretRepository.findById(2L).orElse(null);
    if (statutIndisponible != null) {
        exemplaire.setStatutPret(statutIndisponible);
        exemplaireRepository.save(exemplaire);
    }
        // Sauvegarder
        pretRepository.save(pret);
        return "redirect:/prets/liste";

    } catch (AdherentRestrictionException e) {
        System.out.println("Erreur : " + e.getMessage());
        return "redirect:/prets/liste";
    }
}

@GetMapping("/retour/formulaire")
public String afficherFormulaireRetour(Model model) {
    // 1. Récupérer tous les prêts en cours (ceux qui n'ont pas de date de retour)
    List<Pret> pretsEnCours = pretRepository.findByDateRetourReelleIsNull();
    
    // 2. Ajouter cette liste au modèle pour l'envoyer à la vue
    model.addAttribute("pretsEnCours", pretsEnCours);
    
    // 3. Retourner le nom du fichier HTML qui contiendra le formulaire
    return "formulaire_retour_pret"; // Nommez ce fichier comme vous le souhaitez
}
// Dans PretController.java

@PostMapping("/retour")
public String retourPret(
        @RequestParam("idPret") Long idPret, // On reçoit directement l'ID du prêt
        @RequestParam("dateRetourReelle") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dateRetourReelle
) {
    // 1. Trouver le prêt directement par son ID
    Pret pret = pretRepository.findById(idPret).orElse(null);
    if (pret == null) {
        System.out.println("Prêt non trouvé pour l'ID : " + idPret);
        // Vous pouvez ajouter un message d'erreur pour l'utilisateur ici
        return "redirect:/prets/liste";
    }

    // Sécurité : Vérifier que le prêt n'a pas déjà été retourné
    if (pret.getDateRetourReelle() != null) {
        System.out.println("Ce prêt a déjà été retourné.");
        return "redirect:/prets/liste";
    }

    // 2. Mettre à jour la date de retour réelle sur l'objet Pret
    pret.setDateRetourReelle(dateRetourReelle);
    pretRepository.save(pret);

    // 3. Récupérer l'exemplaire associé au prêt
    Exemplaire exemplaire = pret.getExemplaire();

    // 4. Mettre à jour le statut de l'exemplaire à "disponible" (statut ID 1)
    StatutPret statutDisponible = statutPretRepository.findById(1L).orElse(null);
    if (statutDisponible != null) {
        exemplaire.setStatutPret(statutDisponible);
        exemplaireRepository.save(exemplaire);
    } else {
        // Gérer le cas où le statut "Disponible" n'existe pas dans la BDD
        System.out.println("ERREUR : Le statut 'Disponible' (ID 1) est introuvable.");
    }

    // Rediriger vers la liste des prêts (ou une page de confirmation)
    return "redirect:/prets/liste";
}
}


 