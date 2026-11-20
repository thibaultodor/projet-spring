package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Permet de choisir une région existante lors de la création d'un département. */
@RestController
@RequestMapping("/regions")
public class RegionControleur {

    private final RegionService regionService;

    public RegionControleur(RegionService regionService) {
        this.regionService = regionService;
    }

    /**
     * Expose les régions disponibles pour rattacher un département.
     *
     * @return les régions déjà présentes en base.
     */
    @GetMapping
    public List<Region> getRegions() {
        return regionService.extractRegions();
    }

    /**
     * Expose une région à partir de son identifiant.
     *
     * @param id identifiant recherché
     * @return la région correspondante
     * @throws ExceptionMetier si la région est absente
     */
    @GetMapping("/{id}")
    public Region getRegion(@PathVariable int id) throws ExceptionMetier {
        return regionService.extractRegion(id);
    }
}
