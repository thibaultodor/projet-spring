package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Ville;
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
    public List<Departement> getDepartements() {
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
    public Departement getDepartement(@PathVariable int id) throws ExceptionMetier {
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
    public List<Ville> getVillesDuDepartement(@PathVariable int id) throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id);
    }

    /**
     * Transmet au service un département reçu en JSON.
     *
     * @param departement données JSON du département et id de sa région
     * @return la liste après insertion
     * @throws ExceptionMetier si un contrôle métier échoue
     */
    @PostMapping
    public List<Departement> ajouterDepartement(@Valid @RequestBody Departement departement)
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
    public List<Departement> modifierDepartement(@PathVariable int id, @Valid @RequestBody Departement departement)
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
    public List<Departement> supprimerDepartement(@PathVariable int id) throws ExceptionMetier {
        return departementService.supprimerDepartement(id);
    }
}
