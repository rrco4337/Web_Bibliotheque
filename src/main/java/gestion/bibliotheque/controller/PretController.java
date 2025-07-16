package gestion.bibliotheque.controller;

import gestion.bibliotheque.model.*; // Importe toutes les classes du package model
import gestion.bibliotheque.repository.*; // Importe tous les repositories
import gestion.bibliotheque.service.AdherentRestrictionException;
import gestion.bibliotheque.service.AdherentService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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


    @Autowired
    private PenaliteRepository penaliteRepository;

    @Autowired
    private TypePenaliteRepository typePenaliteRepository;

    @Autowired
    private AbonneRepository abonneRepository;
    @Autowired
    private JourNonOuvrableRepository jourNonOuvrableRepository;

    @GetMapping("/ajouter")
    public String afficherFormulaireAjoutPret(Model model) {
        model.addAttribute("pret", new Pret());
        model.addAttribute("adherents", adherentRepository.findAll());
        model.addAttribute("exemplaires", exemplaireRepository.findAll());
        model.addAttribute("typesPret", typePretRepository.findAll());
        model.addAttribute("statuts", statutPretRepository.findAll());
        return "ajouter_pret";
    }
 
    @GetMapping("/NewPret")
    public String ajouterPret(@RequestParam("idExemplaire") Long idExemplaire, Model model) {
        model.addAttribute("idExemplaire", idExemplaire);
        model.addAttribute("pret", new Pret());
        model.addAttribute("adherents", adherentRepository.findAll());
        model.addAttribute("typesPret", typePretRepository.findAll());
        model.addAttribute("statuts", statutPretRepository.findAll());
        return "ajouter_pret";
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
            return "redirect:/prets/liste";
        }

        pret.setExemplaire(exemplaire);
        Adherent adherent = pret.getAdherent();

        if (adherent == null) {
            System.out.println("Adhérent non fourni dans le formulaire");
            return "redirect:/prets/liste";
        }
        // Vérification de l'abonnement de l'adhérent
        Abonne abonne = abonneRepository.findByAdherent(adherent);
        LocalDate date = pret.getDatePret();
        if (abonne == null || abonne.getDateFin() == null || date == null || date.isAfter(abonne.getDateFin())) {
            System.out.println("Prêt refusé : L'adhérent " + adherent.getNom() + " n'est pas abonné ou l'abonnement est expiré.");
            return "redirect:/prets/liste";
        }
        List<Pret> pretsEnCours = pretRepository.findByAdherentAndExemplaireAndDateRetourReelleIsNull(adherent, exemplaire);
if (!pretsEnCours.isEmpty()) {
    System.out.println("Prêt refusé : L'adhérent " + adherent.getNom() 
        + " a déjà un prêt en cours pour cet exemplaire (ID: " + exemplaire.getId() + ")");
    return "redirect:/prets/liste";
}

// Vérifier si l'exemplaire est déjà emprunté (peu importe par qui)
List<Pret> pretsActifsPourExemplaire = pretRepository.findByExemplaireAndDateRetourReelleIsNull(exemplaire);
if (!pretsActifsPourExemplaire.isEmpty()) {
    System.out.println("Prêt refusé : L'exemplaire (ID: " + exemplaire.getId() 
        + ") est déjà emprunté (prêt ID: " + pretsActifsPourExemplaire.get(0).getId() + ")");
    return "redirect:/prets/liste";
}
      
        List<Penalite> penalites = penaliteRepository.findByAdherent(adherent);
        LocalDate datePret = pret.getDatePret();
        if (datePret == null) {
            System.out.println("Date de prêt non fournie.");
            return "redirect:/prets/liste";
        }
        for (Penalite penalite : penalites) {
            if (!datePret.isAfter(penalite.getDateFin())) { // datePret <= dateFin
                System.out.println("Prêt refusé : L'adhérent " + adherent.getNom() + " est suspendu jusqu'au " + penalite.getDateFin());
                return "redirect:/prets/liste";
            }
        }


        Livre livre = exemplaire.getLivre();
        if (livre == null) {
            System.out.println("Livre introuvable pour l'exemplaire");
            return "redirect:/prets/liste";
        }

        try {
            TypeAdherent typeAdherent = adherentService.verifRestriction(adherent.getId());
            int ageAdherent = adherent.getAge();
            int ageRestriction = livre.getAgeRestriction() != null ? livre.getAgeRestriction() : 0;
            
            if (ageAdherent < ageRestriction) {
                System.out.println("Âge insuffisant pour emprunter ce livre.");
                return "redirect:/prets/liste";
            }
             // Vérification du quota max de prêts
            int quotaMaxPret = typeAdherent.getQuotaMaxPret();
            int nbPretsEnCours = pretRepository.countByAdherentAndDateRetourReelleIsNull(adherent);
            if (nbPretsEnCours >= quotaMaxPret) {
                System.out.println("Quota de prêts atteint pour l'adhérent " + adherent.getNom());
                return "redirect:/prets/liste";
            }
            int dureePret = typeAdherent.getDureeMaxPret();
            if (pret.getDatePret() != null && pret.getTypePret() != null) {
                pret.setDateRetourPrevue(pret.getDatePret().plusDays(dureePret));
            }

            StatutPret statutIndisponible = statutPretRepository.findById(2L).orElse(null);
            if (statutIndisponible != null) {
                exemplaire.setStatutPret(statutIndisponible);
                exemplaireRepository.save(exemplaire);
            }
            pretRepository.save(pret);
            return "redirect:/prets/liste";

        } catch (AdherentRestrictionException e) {
            System.out.println("Erreur : " + e.getMessage());
            return "redirect:/prets/liste";
        }
    }

    @GetMapping("/retour/formulaire")
    public String afficherFormulaireRetour(Model model) {
        List<Pret> pretsEnCours = pretRepository.findByDateRetourReelleIsNull();
        model.addAttribute("pretsEnCours", pretsEnCours);
        return "formulaire_retour_pret";
    }

    @PostMapping("/retour")
    public String retourPret(
            @RequestParam("idPret") Long idPret,
            @RequestParam("dateRetourReelle") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate dateRetourReelle
    ) {
        Pret pret = pretRepository.findById(idPret).orElse(null);
        if (pret == null) {
            System.out.println("Prêt non trouvé pour l'ID : " + idPret);
            return "redirect:/prets/liste";
        }

        if (pret.getDateRetourReelle() != null) {
            System.out.println("Ce prêt a déjà été retourné.");
            return "redirect:/prets/liste";
        }

        pret.setDateRetourReelle(dateRetourReelle);

        List<jourNonOuvrable> joursNonOuvrables = jourNonOuvrableRepository.findByDateJourNonOuvrable(dateRetourReelle);
        if (!joursNonOuvrables.isEmpty()) {
            System.out.println("Erreur : Le jour du retour (" + dateRetourReelle + ") est un jour non ouvrable. Veuillez retourner le livre un jour après.");
            return "redirect:/prets/liste";
        }
if (pret.getDateRetourPrevue() != null && dateRetourReelle.isAfter(pret.getDateRetourPrevue())) {
    long joursDeRetard = ChronoUnit.DAYS.between(pret.getDateRetourPrevue(), dateRetourReelle);

    System.out.println("Livre rendu avec " + joursDeRetard + " jour(s) de retard.");

    TypeAdherent typeAdherent = pret.getAdherent().getTypeAdherent();
    Integer dureePenalite = typeAdherent.getDureePenalite();

    if (dureePenalite != null && dureePenalite > 0) {
        Penalite nouvellePenalite = new Penalite();
        nouvellePenalite.setAdherent(pret.getAdherent());
        nouvellePenalite.setDateDebut(dateRetourReelle);
        nouvellePenalite.setDateFin(dateRetourReelle.plusDays(dureePenalite));

        // Ajout d'un TypePenalite par défaut (ex: "Retard")
        TypePenalite typePenaliteDefaut = typePenaliteRepository.findByNomTypePenalite("Livre endommagé");
        nouvellePenalite.setTypePenalite(typePenaliteDefaut);
        

        penaliteRepository.save(nouvellePenalite);

        Adherent adherentPenalise = pret.getAdherent();
        adherentPenalise.setEstPenalise(true);
        adherentRepository.save(adherentPenalise);

        System.out.println("Pénalité créée pour l'adhérent " + pret.getAdherent().getNom() + " jusqu'au " + nouvellePenalite.getDateFin());
    } else {
        System.out.println("Durée de pénalité non définie pour ce type d'adhérent.");
    }


}
      
        
        pretRepository.save(pret);

        Exemplaire exemplaire = pret.getExemplaire();
        StatutPret statutDisponible = statutPretRepository.findById(1L).orElse(null);
        if (statutDisponible != null) {
            exemplaire.setStatutPret(statutDisponible);
            exemplaireRepository.save(exemplaire);
        } else {
            System.out.println("ERREUR : Le statut 'Disponible' (ID 1) est introuvable.");
        }

        return "redirect:/prets/liste";
    }
}