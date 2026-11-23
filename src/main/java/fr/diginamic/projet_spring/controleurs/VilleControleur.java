package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.VilleService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expose uniquement des VilleDto : les entités JPA restent dans les services et repositories. */
@RestController
@RequestMapping("/villes")
public class VilleControleur {

    private final VilleService villeService;

    public VilleControleur(VilleService villeService) {
        this.villeService = villeService;
    }

    /**
     * Expose une page de villes avec les informations de navigation.
     * @param page numéro de page, 0 par défaut
     * @param size nombre de villes par page, 20 par défaut et 100 au maximum
     * @return content contient les villes ; page contient les totaux et le numéro demandé
     * @throws ExceptionMetier si la pagination est invalide
     */
    @GetMapping
    public PagedModel<VilleDto> getVilles(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) throws ExceptionMetier {
        // PagedModel fournit une réponse JSON simple : content et page.
        return new PagedModel<>(villeService.extractVillesPaginees(page, size));
    }

    /**
     * Expose une ville à partir de son identifiant.
     *
     * @param id identifiant lu dans l'URL
     * @return la ville correspondante
     * @throws ExceptionMetier si elle est introuvable
     */
    @GetMapping("/{id}")
    public VilleDto getVille(@PathVariable int id) throws ExceptionMetier {
        return villeService.extractVille(id);
    }

    /**
     * Transmet au service une ville reçue en JSON.
     *
     * @param ville DTO JSON validé avant l'appel au service
     * @return la liste après insertion
     * @throws ExceptionMetier si un contrôle métier échoue
     */
    @PostMapping
    public List<VilleDto> ajouterVille(@Valid @RequestBody VilleDto ville) throws ExceptionMetier {
        return villeService.insertVille(ville);
    }

    /**
     * Met à jour une ville en conservant son identifiant.
     *
     * @param id identifiant à conserver
     * @param nouvellesDonnees données JSON de remplacement
     * @return la liste après modification
     * @throws ExceptionMetier si la ville ou ses nouvelles données sont invalides
     */
    @PutMapping("/{id}")
    public List<VilleDto> modifierVille(@PathVariable int id, @Valid @RequestBody VilleDto nouvellesDonnees)
            throws ExceptionMetier {
        return villeService.modifierVille(id, nouvellesDonnees);
    }

    /**
     * Supprime une ville existante.
     *
     * @param id identifiant à supprimer
     * @return la liste après suppression
     * @throws ExceptionMetier si la ville n'existe pas
     */
    @DeleteMapping("/{id}")
    public List<VilleDto> supprimerVille(@PathVariable int id) throws ExceptionMetier {
        return villeService.supprimerVille(id);
    }

    /**
     * Expose la recherche par début du nom.
     *
     * @param nom début du nom reçu après ?nom=
     * @return les villes correspondantes
     * @throws ExceptionMetier si la recherche est invalide ou sans résultat
     */
    @GetMapping("/recherche/nom")
    public List<VilleDto> rechercherParNom(@RequestParam String nom) throws ExceptionMetier {
        return villeService.extractVilles(nom);
    }

    /**
     * Expose la recherche au-dessus d'une population donnée.
     *
     * @param min population minimale exclue
     * @return les villes dont la population dépasse min
     * @throws ExceptionMetier si aucune ville ne correspond
     */
    @GetMapping("/recherche/population")
    public List<VilleDto> rechercherParPopulationMin(@RequestParam int min) throws ExceptionMetier {
        return villeService.extractVilles(min);
    }

    /**
     * Expose la recherche entre deux populations.
     *
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les villes situées entre les bornes
     * @throws ExceptionMetier si l'intervalle est invalide ou sans résultat
     */
    @GetMapping("/recherche/population/entre")
    public List<VilleDto> rechercherParPopulationEntre(@RequestParam int min, @RequestParam int max)
            throws ExceptionMetier {
        return villeService.extractVilles(min, max);
    }
}
