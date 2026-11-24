package fr.diginamic.projet_spring.controleurs.interfaces;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

/** Documentation des routes des départements. */
@Tag(name = "Départements", description = "Gestion des départements et consultation de leurs villes")
public interface IDepartementControleur {

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
    List<DepartementDto> getDepartements();

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
    DepartementDto getDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id) throws ExceptionMetier;

    /**
     * Expose les villes d'un département.
     *
     * @param id département recherché
     * @return les villes qui lui sont rattachées
     * @throws ExceptionMetier si le département est absent
     */
    @Operation(summary = "Retourne les villes d’un département")
    List<VilleDto> getVillesDuDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id) throws ExceptionMetier;

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
    List<DepartementDto> ajouterDepartement(DepartementDto departement)
            throws ExceptionMetier;

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
    List<DepartementDto> modifierDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id,   DepartementDto departement)
            throws ExceptionMetier;

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
    List<DepartementDto> supprimerDepartement(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id) throws ExceptionMetier;

    /**
     * Liste les plus grandes villes d'un département.
     * @param id département recherché
     * @param n nombre maximal de villes, 10 par défaut
     * @return les DTO par population décroissante
     * @throws ExceptionMetier si le nombre est invalide ou le département absent
     */
    @Operation(summary = "Retourne les n villes les plus peuplées d’un département")
    List<VilleDto> getPlusGrandesVilles(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id,
            @Parameter(description = "Nombre maximal de villes à retourner", example = "5", schema = @Schema(minimum = "1"))
            int n)
            throws ExceptionMetier;

    /**
     * Liste les villes d'un département au-dessus d'un minimum exclu.
     * @param id département recherché
     * @param min population minimale exclue
     * @return les DTO par population décroissante
     * @throws ExceptionMetier si le département est absent
     */
    @Operation(summary = "Recherche les villes d’un département par population",
            description = "min est obligatoire ; max est facultatif. Les bornes sont exclues et le tri est décroissant.")
    List<VilleDto> getVillesParPopulationMin(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id,
            @Parameter(description = "Population minimale, borne exclue", example = "20000")
            int min)
            throws ExceptionMetier;

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
    List<VilleDto> getVillesParPopulation(
            @Parameter(description = "Identifiant en base du département, différent de son code", example = "74", required = true)
            int id,
            @Parameter(description = "Population minimale, borne exclue", example = "20000")
            int min,
            @Parameter(description = "Population maximale, borne exclue ; facultative pour cette URL", required = false, example = "100000")
            int max)
            throws ExceptionMetier;

    /**
     * Télécharge un PDF à partir du code départemental, et non de son identifiant en base.
     * @param code code du département
     * @return la fiche PDF avec les villes et leurs populations
     * @throws ExceptionMetier si le département est introuvable ou si le PDF ne peut pas être créé
     */
    @Operation(summary = "Exporte un rapport illustré du département au format PDF",
            description = "Synthèse, graphiques, repères dans la base, signets et annuaire complet des villes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Fichier PDF à télécharger",
                content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(responseCode = "404", description = "Département introuvable",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "La génération du PDF a échoué",
                content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    })
    ResponseEntity<byte[]> exporterPdf(
            @Parameter(description = "Code du département, différent de son identifiant en base", example = "34")
            String code) throws ExceptionMetier;
}
