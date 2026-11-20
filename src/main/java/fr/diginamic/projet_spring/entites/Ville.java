package fr.diginamic.projet_spring.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Ville persistée en base et rattachée à un département existant. */
@Entity
@Table(name = "ville")
public class Ville {

    // La base attribue l'id lors de l'insertion grâce à AUTO_INCREMENT.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull(message = "Le nom de la ville ne doit pas être nul")
    @Size(min = 2, max = 255, message = "Le nom de la ville doit contenir entre 2 et 255 caractères")
    @Column(nullable = false, length = 255)
    private String nom;

    @Min(value = 1, message = "La ville doit avoir au moins 1 habitant")
    @Column(nullable = false)
    private int population;

    // Cette relation reprend la clé étrangère ID_DEPARTEMENT de la table existante.
    @NotNull(message = "Le département de la ville est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_DEPARTEMENT", nullable = false)
    private Departement departement;

    // JPA utilise ce constructeur vide pour reconstruire la ville depuis la base.
    public Ville() {
    }

    public Ville(String nom, int population, Departement departement) {
        this.nom = nom;
        this.population = population;
        this.departement = departement;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public int getPopulation() { return population; }
    public void setPopulation(int population) { this.population = population; }
    public Departement getDepartement() { return departement; }
    public void setDepartement(Departement departement) { this.departement = departement; }
}
