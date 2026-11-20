package fr.diginamic.projet_spring.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Région déjà présente en base, utilisée pour rattacher les départements. */
@Entity
@Table(name = "region")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 3, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String nom;

    public Region() {
    }

    public Region(int id, String code, String nom) {
        this.id = id;
        this.code = code;
        this.nom = nom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}
