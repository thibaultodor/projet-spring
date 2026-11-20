package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.DepartementService;
import fr.diginamic.projet_spring.services.VilleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Expose le CRUD des départements et la consultation de leurs villes. */
@RestController
@RequestMapping("/departements")
public class DepartementControleur {

    private final DepartementService departementService;
    private final VilleService villeService;

    public DepartementControleur(DepartementService departementService, VilleService villeService) {
        this.departementService = departementService;
        this.villeService = villeService;
    }

    /**
     * Expose la liste des départements.
     *
     * @return les départements disponibles.
     */
    @GetMapping
    public List<DepartementDto> getDepartements() {
        return departementService.extractDepartements();
    }

    /**
     * Expose un département à partir de son identifiant.
     *
     * @param id identifiant recherché
     * @return le département correspondant
     * @throws ExceptionMetier si le département est absent
     */
    @GetMapping("/{id}")
    public DepartementDto getDepartement(@PathVariable int id) throws ExceptionMetier {
        return departementService.extractDepartement(id);
    }

    /**
     * Expose les villes d'un département.
     *
     * @param id département recherché
     * @return les villes qui lui sont rattachées
     * @throws ExceptionMetier si le département est absent
     */
    @GetMapping("/{id}/villes")
    public List<VilleDto> getVillesDuDepartement(@PathVariable int id) throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id);
    }

    /**
     * Transmet au service un département reçu en JSON.
     *
     * @param departement DTO du département, avec une région facultative
     * @return la liste après insertion
     * @throws ExceptionMetier si un contrôle métier échoue
     */
    @PostMapping
    public List<DepartementDto> ajouterDepartement(@Valid @RequestBody DepartementDto departement)
            throws ExceptionMetier {
        return departementService.insertDepartement(departement);
    }

    /**
     * Met à jour un département existant.
     *
     * @param id identifiant à conserver
     * @param departement données de remplacement
     * @return la liste après modification
     * @throws ExceptionMetier si les données ou les références sont invalides
     */
    @PutMapping("/{id}")
    public List<DepartementDto> modifierDepartement(@PathVariable int id, @Valid @RequestBody DepartementDto departement)
            throws ExceptionMetier {
        return departementService.modifierDepartement(id, departement);
    }

    /**
     * Supprime un département après les contrôles nécessaires.
     *
     * @param id département à supprimer
     * @return la liste restante
     * @throws ExceptionMetier si le département est absent ou contient encore des villes
     */
    @DeleteMapping("/{id}")
    public List<DepartementDto> supprimerDepartement(@PathVariable int id) throws ExceptionMetier {
        return departementService.supprimerDepartement(id);
    }

    /**
     * Liste les plus grandes villes d'un département.
     * @param id département recherché
     * @param n nombre maximal de villes, 10 par défaut
     * @return les DTO par population décroissante
     * @throws ExceptionMetier si le nombre est invalide ou le département absent
     */
    @GetMapping("/{id}/villes/plus-grandes")
    public List<VilleDto> getPlusGrandesVilles(@PathVariable int id, @RequestParam(defaultValue = "10") int n)
            throws ExceptionMetier {
        return villeService.extractPlusGrandesVilles(id, n);
    }

    /**
     * Filtre les villes d'un département entre deux populations.
     * @param id département recherché
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les DTO correspondants
     * @throws ExceptionMetier si les bornes sont invalides ou le département absent
     */
    @GetMapping("/{id}/villes/population")
    public List<VilleDto> getVillesParPopulation(@PathVariable int id, @RequestParam int min, @RequestParam int max)
            throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id, min, max);
    }

}
