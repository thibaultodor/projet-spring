package fr.diginamic.projet_spring.entites;

// Représente les données d'une ville échangées au format JSON.
public class Ville {

    // Le contrôleur attribue cet identifiant lors de l'ajout.
    private int id;
    private String nom;
    private int population;

    // Spring utilise ce constructeur vide pour lire une ville envoyée en JSON.
    public Ville() {
    }

    // Appelle le constructeur complet avec un id provisoire de 0.
    public Ville(String nom, int population) {
        this(0, nom, population);
    }

    public Ville(int id, String nom, int population) {
        this.id = id;
        this.nom = nom;
        this.population = population;
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
}
