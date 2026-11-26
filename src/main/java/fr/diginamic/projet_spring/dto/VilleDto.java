package fr.diginamic.projet_spring.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Données échangées avec le client, sans exposer les relations JPA de Ville. */
@Schema(description = "Ville sans relation imbriquée. Renseigner idDepartement ou codeDepartement ; un code inconnu crée le département.")
public class VilleDto {

    @Schema(description = "Identifiant attribué par la base", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private int id;

    @NotNull(message = "Le nom de la ville ne doit pas être nul")
    @Size(min = 2, max = 255, message = "Le nom de la ville doit contenir entre 2 et 255 caractères")
    @Schema(description = "Nom de la ville", example = "Ville exemple")
    private String nom;

    @Min(value = 1, message = "La ville doit avoir au moins 1 habitant")
    @Schema(description = "Nombre d’habitants", example = "12000")
    private int population;

    @Min(value = 1, message = "L’identifiant du département doit être positif")
    @Schema(description = "Identifiant d’un département connu, facultatif si le code est fourni", example = "74")
    private Integer idDepartement;

    @Size(max = 3, message = "Le code du département doit contenir au plus 3 caractères")
    @Schema(description = "Code sur 2 ou 3 caractères ; peut remplacer idDepartement", example = "34")
    private String codeDepartement;

    // Ces informations viennent du serveur. Le client ne peut pas les modifier.
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Dernier utilisateur ayant ajouté ou modifié la donnée", accessMode = Schema.AccessMode.READ_ONLY)
    private String userMaj;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Date et heure de la dernière écriture", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dateMaj;

    public VilleDto() {
    }

    // Réservé au mapping interne ; JSON utilise le constructeur vide et les setters.
    @JsonCreator(mode = JsonCreator.Mode.DISABLED)
    public VilleDto(int id, String nom, int population, Integer idDepartement, String codeDepartement) {
        this.id = id;
        this.nom = nom;
        this.population = population;
        this.idDepartement = idDepartement;
        this.codeDepartement = codeDepartement;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public Integer getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(Integer idDepartement) {
        this.idDepartement = idDepartement;
    }

    public String getCodeDepartement() {
        return codeDepartement;
    }

    public void setCodeDepartement(String codeDepartement) {
        this.codeDepartement = codeDepartement;
    }

    public String getUserMaj() { return userMaj; }
    public void setUserMaj(String userMaj) { this.userMaj = userMaj; }
    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }
}
