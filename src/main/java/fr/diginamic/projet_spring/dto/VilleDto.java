package fr.diginamic.projet_spring.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Données échangées avec le client, sans exposer les relations JPA de Ville. */
public class VilleDto {

    private int id;

    @NotNull(message = "Le nom de la ville ne doit pas être nul")
    @Size(min = 2, max = 255, message = "Le nom de la ville doit contenir entre 2 et 255 caractères")
    private String nom;

    @Min(value = 1, message = "La ville doit avoir au moins 1 habitant")
    private int population;

    @Min(value = 1, message = "L’identifiant du département doit être positif")
    private Integer idDepartement;

    @Size(max = 3, message = "Le code du département doit contenir au plus 3 caractères")
    private String codeDepartement;

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

}
