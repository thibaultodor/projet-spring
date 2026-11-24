package fr.diginamic.projet_spring.repositories.projections;

/** Projection : la base renvoie les totaux sans charger toutes les villes en mémoire. */
public interface StatistiquesDepartement {
    int getId();
    String getCode();
    String getNom();
    Integer getIdRegion();
    long getPopulation();
    long getNombreVilles();
}
