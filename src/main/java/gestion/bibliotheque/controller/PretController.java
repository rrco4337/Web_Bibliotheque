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

    // NOUVEAU : Injecter les repositories pour les pénalités
    @Autowired
    private PenaliteRepository penaliteRepository;

    @Autowired
    private TypePenaliteRepository typePenaliteRepository;


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
        
        // ================= DEBUT DE LA MODIFICATION (Vérification de pénalité) =================
        // Vérifier si l'adhérent a une pénalité active
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
        // ================= FIN DE LA MODIFICATION ============================================

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

        // ================= DEBUT DE LA MODIFICATION (Création de pénalité) =================
        // Vérifier si le retour est en retard
        if (pret.getDateRetourPrevue() != null && dateRetourReelle.isAfter(pret.getDateRetourPrevue())) {
            // Calculer le nombre de jours de retard
            long joursDeRetard = ChronoUnit.DAYS.between(pret.getDateRetourPrevue(), dateRetourReelle);
            
            System.out.println("Livre rendu avec " + joursDeRetard + " jour(s) de retard.");

            // Créer une nouvelle pénalité
            TypePenalite typeSuspension = typePenaliteRepository.findById(1L).orElse(null); // On suppose que l'ID 1 est la suspension
            if (typeSuspension != null) {
                Penalite nouvellePenalite = new Penalite();
                nouvellePenalite.setAdherent(pret.getAdherent());
                nouvellePenalite.setTypePenalite(typeSuspension);
                nouvellePenalite.setDateDebut(dateRetourReelle); // La pénalité commence le jour du retour
                nouvellePenalite.setDateFin(dateRetourReelle.plusDays(joursDeRetard)); // La pénalité dure autant de jours que le retard

                penaliteRepository.save(nouvellePenalite);
                System.out.println("Pénalité de suspension créée pour l'adhérent " + pret.getAdherent().getNom() + " jusqu'au " + nouvellePenalite.getDateFin());
            } else {
                 System.out.println("ERREUR : Le type de pénalité 'Suspension' (ID 1) est introuvable.");
            }
        }
        // ================= FIN DE LA MODIFICATION ============================================
        
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