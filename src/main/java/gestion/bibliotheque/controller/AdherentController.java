package gestion.bibliotheque.controller;

import gestion.bibliotheque.model.TypeAdherent;
import gestion.bibliotheque.model.Adherent;

import gestion.bibliotheque.repository.AdherentRepository  ;
import gestion.bibliotheque.repository.TypeAdherentRepository  ;
import gestion.bibliotheque.service.TypeAdherentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

@Controller
@RequestMapping("/adherents")
public class AdherentController {

    @Autowired
    private AdherentRepository adherentRepository;

    @Autowired
    private TypeAdherentRepository typeAdherentRepository;

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
