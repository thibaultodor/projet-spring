package fr.diginamic.projet_spring.dao;

import fr.diginamic.projet_spring.entites.Departement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Regroupe les requêtes JPA et les écritures des départements. */
@Repository
public class DepartementDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Récupère les départements avec leur région.
     *
     * @return les départements avec leur région déjà chargée.
     */
    public List<Departement> extractDepartements() {
        // join fetch charge la relation dans la même requête pour éviter une requête par département.
        return entityManager.createQuery(
                "select d from Departement d left join fetch d.region order by d.id", Departement.class).getResultList();
    }

    /**
     * Recherche un département par son identifiant.
     *
     * @param id identifiant recherché
     * @return le département, ou null s'il n'existe pas
     */
    public Departement extractDepartement(int id) {
        return entityManager.find(Departement.class, id);
    }

    /**
     * Vérifie si un autre département utilise déjà ce code.
     *
     * @param code code à contrôler
     * @param idExclu département à ignorer lors d'une modification, ou 0 lors d'un ajout
     * @return true si un autre département utilise ce code
     */
    public boolean existeParCode(String code, int idExclu) {
        return entityManager.createQuery(
                        "select count(d) from Departement d where upper(d.code) = :code and d.id <> :id", Long.class)
                .setParameter("code", code).setParameter("id", idExclu).getSingleResult() > 0;
    }

    /**
     * Enregistre un nouveau département.
     *
     * @param departement nouveau département à insérer.
     */
    public void insertDepartement(Departement departement) {
        entityManager.persist(departement);
        entityManager.flush();
    }

    /**
     * Met à jour un département existant.
     *
     * @param departement département contenant les nouvelles données.
     */
    public void modifierDepartement(Departement departement) {
        entityManager.merge(departement);
        entityManager.flush();
    }

    /**
     * Supprime un département après les contrôles nécessaires.
     *
     * @param departement département existant à supprimer.
     */
    public void supprimerDepartement(Departement departement) {
        entityManager.remove(departement);
        entityManager.flush();
    }

    /**
     * Recherche un code sans distinguer les majuscules.
     * @param code code normalisé à rechercher
     * @return le département, ou null s'il n'existe pas
     */
    public Departement extractParCode(String code) {
        List<Departement> resultats = entityManager.createQuery(
                        "select d from Departement d left join fetch d.region where upper(d.code) = :code",
                        Departement.class)
                .setParameter("code", code).getResultList();
        return resultats.isEmpty() ? null : resultats.getFirst();
    }

}
