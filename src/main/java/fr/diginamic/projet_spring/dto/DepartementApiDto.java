package fr.diginamic.projet_spring.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Conserve seulement le code et le nom reçus de l'API géographique. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepartementApiDto {

    private String code;
    private String nom;

    public DepartementApiDto() {
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
