package fr.diginamic.projet_spring.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Département envoyé en JSON, sans sa collection de villes pour éviter une boucle. */
@Schema(description = "Département avec une région facultative, sans collection de villes.")
public class DepartementDto {

    @Schema(description = "Identifiant attribué par la base, différent du code départemental", accessMode = Schema.AccessMode.READ_ONLY, example = "74")
    private int id;

    @NotNull(message = "Le code du département est obligatoire")
    @Size(min = 2, max = 3, message = "Le code du département doit contenir 2 ou 3 caractères")
    @Schema(description = "Code officiel sur 2 ou 3 caractères", example = "34")
    private String code;

    @NotNull(message = "Le nom du département est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom du département doit contenir entre 2 et 100 caractères")
    @Schema(description = "Nom du département", example = "Hérault")
    private String nom;

    @Min(value = 1, message = "L’identifiant de la région doit être positif")
    @Schema(description = "Région facultative ; une valeur omise en PUT conserve la région actuelle", example = "33")
    private Integer idRegion;

    public DepartementDto() {
    }

    // Réservé au mapping interne ; JSON utilise le constructeur vide et les setters.
    @JsonCreator(mode = JsonCreator.Mode.DISABLED)
    public DepartementDto(int id, String code, String nom, Integer idRegion) {
        this.id = id;
        this.code = code;
        this.nom = nom;
        this.idRegion = idRegion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getIdRegion() {
        return idRegion;
    }

    public void setIdRegion(Integer idRegion) {
        this.idRegion = idRegion;
    }

}
