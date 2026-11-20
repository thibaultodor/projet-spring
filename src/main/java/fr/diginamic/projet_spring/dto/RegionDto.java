package fr.diginamic.projet_spring.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

/** Informations publiques d'une région, indépendantes de sa représentation JPA. */
public class RegionDto {

    private int id;

    private String code;

    private String nom;

    public RegionDto() {
    }

    // Réservé au mapping interne ; JSON utilise le constructeur vide et les setters.
    @JsonCreator(mode = JsonCreator.Mode.DISABLED)
    public RegionDto(int id, String code, String nom) {
        this.id = id;
        this.code = code;
        this.nom = nom;
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

}
