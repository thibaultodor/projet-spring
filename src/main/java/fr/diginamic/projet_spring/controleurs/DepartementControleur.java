package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.DepartementService;
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
@Tag(name = "Départements", description = "Gestion des départements et consultation de leurs villes")
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
    @Operation(summary = "Retourne la liste de tous les départements")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des départements au format JSON",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = DepartementDto.class))))
    })
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
    @Operation(summary = "Retourne un département à partir de son identifiant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Département au format JSON",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = DepartementDto.class))),
        @ApiResponse(responseCode = "404", description = "Département introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{id}")
    public DepartementDto getDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id) throws ExceptionMetier {
        return departementService.extractDepartement(id);
    }

    /**
     * Expose les villes d'un département.
     *
     * @param id département recherché
     * @return les villes qui lui sont rattachées
     * @throws ExceptionMetier si le département est absent
     */
    @Operation(summary = "Retourne les villes d’un département")
    @GetMapping("/{id}/villes")
    public List<VilleDto> getVillesDuDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id) throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id);
    }

    /**
     * Transmet au service un département reçu en JSON.
     *
     * @param departement DTO du département, avec une région facultative
     * @return la liste après insertion
     * @throws ExceptionMetier si un contrôle métier échoue
     */
    @Operation(summary = "Crée un département",
            description = "La région est facultative. Le serveur attribue un identifiant au département.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des départements après insertion",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = DepartementDto.class)))),
        @ApiResponse(responseCode = "400", description = "Données invalides ou code de département déjà utilisé",
                content = {
                        @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)),
                        @Content(mediaType = "application/json", schema = @Schema(type = "object",
                                additionalPropertiesSchema = String.class))
                }),
        @ApiResponse(responseCode = "404", description = "Région renseignée introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "409", description = "Contrainte de la base non respectée",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
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
    @Operation(summary = "Modifie un département existant",
            description = "Conserve la région actuelle si idRegion est omis. L’identifiant dans l’URL reste inchangé.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des départements après modification",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = DepartementDto.class)))),
        @ApiResponse(responseCode = "400", description = "Données invalides ou code de département déjà utilisé",
                content = {
                        @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)),
                        @Content(mediaType = "application/json", schema = @Schema(type = "object",
                                additionalPropertiesSchema = String.class))
                }),
        @ApiResponse(responseCode = "404", description = "Département ou région introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "409", description = "Contrainte de la base non respectée",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @PutMapping("/{id}")
    public List<DepartementDto> modifierDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id, @Valid @RequestBody DepartementDto departement)
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
    @Operation(summary = "Supprime un département sans villes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des départements après suppression",
                content = @Content(mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = DepartementDto.class)))),
        @ApiResponse(responseCode = "404", description = "Département introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "409", description = "Le département contient encore des villes ou une contrainte empêche sa suppression",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    @DeleteMapping("/{id}")
    public List<DepartementDto> supprimerDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id) throws ExceptionMetier {
        return departementService.supprimerDepartement(id);
    }

    /**
     * Liste les plus grandes villes d'un département.
     * @param id département recherché
     * @param n nombre maximal de villes, 10 par défaut
     * @return les DTO par population décroissante
     * @throws ExceptionMetier si le nombre est invalide ou le département absent
     */
    @Operation(summary = "Retourne les n villes les plus peuplées d’un département")
    @GetMapping("/{id}/villes/plus-grandes")
    public List<VilleDto> getPlusGrandesVilles(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id,
            @Parameter(description = "Nombre maximal de villes à retourner", example = "5", schema = @Schema(minimum = "1"))
            @RequestParam(defaultValue = "10") int n)
            throws ExceptionMetier {
        return villeService.extractPlusGrandesVilles(id, n);
    }

    /**
     * Liste les villes d'un département au-dessus d'un minimum exclu.
     * @param id département recherché
     * @param min population minimale exclue
     * @return les DTO par population décroissante
     * @throws ExceptionMetier si le département est absent
     */
    @Operation(summary = "Recherche les villes d’un département par population",
            description = "min est obligatoire ; max est facultatif. Les bornes sont exclues et le tri est décroissant.")
    @GetMapping(value = "/{id}/villes/population", params = "!max")
    public List<VilleDto> getVillesParPopulationMin(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id,
            @Parameter(description = "Population minimale, borne exclue", example = "20000")
            @RequestParam int min)
            throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id, min);
    }

    /**
     * Filtre les villes d'un département entre deux populations.
     * @param id département recherché
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les DTO correspondants
     * @throws ExceptionMetier si les bornes sont invalides ou le département absent
     */
    @Operation(summary = "Recherche les villes d’un département par population",
            description = "min est obligatoire ; max est facultatif. Les bornes sont exclues et le tri est décroissant.")
    @GetMapping(value = "/{id}/villes/population", params = "max")
    public List<VilleDto> getVillesParPopulation(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            @PathVariable int id,
            @Parameter(description = "Population minimale, borne exclue", example = "20000")
            @RequestParam int min,
            @Parameter(description = "Population maximale, borne exclue ; facultative pour cette URL", required = false, example = "100000")
            @RequestParam int max)
            throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id, min, max);
    }

}
