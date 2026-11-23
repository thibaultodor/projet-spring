package fr.diginamic.projet_spring.dao;

import fr.diginamic.projet_spring.entites.Region;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/** Accès en lecture aux régions déjà enregistrées. */
@Repository
public class RegionDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Recherche une région par son identifiant.
     *
     * @param id identifiant recherché
     * @return la région, ou null si elle n'existe pas
     */
    public Region extractRegion(int id) {
        return entityManager.find(Region.class, id);
    }
}
