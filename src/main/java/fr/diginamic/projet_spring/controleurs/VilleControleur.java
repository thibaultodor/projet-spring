package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.VilleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Villes", description = "Gestion, pagination et recherche des villes")
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
    @Operation(summary = "Retourne une page de villes",
            description = "Les pages commencent à 0. La taille est comprise entre 1 et 100 ; elle vaut 20 par défaut.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Villes dans content et informations de pagination dans page"),
        @ApiResponse(responseCode = "400", description = "Numéro ou taille de page invalide",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @GetMapping
    public PagedModel<VilleDto> getVilles(
            @Parameter(description = "Numéro de page, à partir de 0", example = "0", schema = @Schema(minimum = "0"))
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre de villes par page", example = "20", schema = @Schema(minimum = "1", maximum = "100"))
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
    @Operation(summary = "Retourne une ville à partir de son identifiant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ville au format JSON",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = VilleDto.class))),
        @ApiResponse(responseCode = "404", description = "Ville introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{id}")
    public VilleDto getVille(
            @Parameter(description = "Identifiant en base de la ville", example = "1", required = true)
            @PathVariable int id) throws ExceptionMetier {
        return villeService.extractVille(id);
    }

    /**
     * Transmet au service une ville reçue en JSON.
     *
     * @param ville DTO JSON validé avant l'appel au service
     * @return la liste après insertion
     * @throws ExceptionMetier si un contrôle métier échoue
     */
    @Operation(summary = "Crée une ville avec son département",
            description = "Renseigner idDepartement ou codeDepartement. Un code inconnu crée un département sans région.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des villes après insertion",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = VilleDto.class)))),
        @ApiResponse(responseCode = "400", description = "Données invalides, références incohérentes ou ville déjà présente dans ce département",
                content = {
                        @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)),
                        @Content(mediaType = "application/json", schema = @Schema(type = "object",
                                additionalPropertiesSchema = String.class))
                }),
        @ApiResponse(responseCode = "404", description = "Département inconnu et aucun code fourni pour le créer",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "409", description = "Contrainte de la base non respectée",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
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
    @Operation(summary = "Modifie une ville existante",
            description = "Remplace les données de la ville, y compris son département. L’identifiant dans l’URL reste inchangé.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des villes après modification",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = VilleDto.class)))),
        @ApiResponse(responseCode = "400", description = "Données invalides, références incohérentes ou ville déjà présente dans ce département",
                content = {
                        @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)),
                        @Content(mediaType = "application/json", schema = @Schema(type = "object",
                                additionalPropertiesSchema = String.class))
                }),
        @ApiResponse(responseCode = "404", description = "Ville introuvable ou département inconnu sans code fourni",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "409", description = "Contrainte de la base non respectée",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @PutMapping("/{id}")
    public List<VilleDto> modifierVille(
            @Parameter(description = "Identifiant en base de la ville", example = "1", required = true)
            @PathVariable int id, @Valid @RequestBody VilleDto nouvellesDonnees)
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
    @Operation(summary = "Supprime une ville existante")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des villes après suppression",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = VilleDto.class)))),
        @ApiResponse(responseCode = "404", description = "Ville introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "409", description = "Contrainte de la base non respectée",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @DeleteMapping("/{id}")
    public List<VilleDto> supprimerVille(
            @Parameter(description = "Identifiant en base de la ville", example = "1", required = true)
            @PathVariable int id) throws ExceptionMetier {
        return villeService.supprimerVille(id);
    }

    /**
     * Expose la recherche par début du nom.
     *
     * @param nom début du nom reçu après ?nom=
     * @return les villes correspondantes
     * @throws ExceptionMetier si la recherche est invalide ou sans résultat
     */
    @Operation(summary = "Recherche les villes dont le nom commence par un texte")
    @GetMapping("/recherche/nom")
    public List<VilleDto> rechercherParNom(
            @Parameter(description = "Début du nom, sans distinguer les majuscules", example = "Mont")
            @RequestParam String nom) throws ExceptionMetier {
        return villeService.extractVilles(nom);
    }

    /**
     * Expose la recherche au-dessus d'une population donnée.
     *
     * @param min population minimale exclue
     * @return les villes dont la population dépasse min
     * @throws ExceptionMetier si aucune ville ne correspond
     */
    @Operation(summary = "Recherche les villes au-dessus d’une population, par ordre décroissant")
    @GetMapping("/recherche/population")
    public List<VilleDto> rechercherParPopulationMin(
            @Parameter(description = "Population minimale, borne exclue", example = "20000")
            @RequestParam int min) throws ExceptionMetier {
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
    @Operation(summary = "Recherche les villes entre deux populations exclues, par ordre décroissant")
    @GetMapping("/recherche/population/entre")
    public List<VilleDto> rechercherParPopulationEntre(
            @Parameter(description = "Population minimale, borne exclue", example = "20000")
            @RequestParam int min,
            @Parameter(description = "Population maximale, borne exclue", example = "100000")
            @RequestParam int max)
            throws ExceptionMetier {
        return villeService.extractVilles(min, max);
    }
}
