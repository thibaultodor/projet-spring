package fr.diginamic.projet_spring.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/** Département associé à ses villes ; sa région peut rester non renseignée. */
@Entity
@Table(name = "departement")
public class Departement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Le code du département est obligatoire")
    @Size(min = 2, max = 3, message = "Le code du département doit contenir 2 ou 3 caractères")
    @Column(nullable = false, length = 3, unique = true)
    private String code;

    @NotNull(message = "Le nom du département est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom du département doit contenir entre 2 et 100 caractères")
    @Column(name = "NOM", length = 100)
    private String nom;

    // Un département créé à partir de son seul code peut ne pas encore avoir de région.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGION")
    private Region region;

    // mappedBy indique que Ville.departement porte la clé étrangère.
    // Aucune suppression en cascade : le service protège les départements encore utilisés.
    @OneToMany(mappedBy = "departement")
    private List<Ville> villes = new ArrayList<>();

    public Departement() {
    }

    public Departement(String code, String nom, Region region) {
        this.code = code;
        this.nom = nom;
        this.region = region;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public List<Ville> getVilles() { return villes; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }
}
