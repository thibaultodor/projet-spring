package fr.diginamic.projet_spring.dao;

import fr.diginamic.projet_spring.entites.Ville;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Locale;

/** Seule couche chargée d'exécuter les requêtes JPA sur les villes. */
@Repository
public class VilleDao {

    // Charge le département pour construire le DTO avant la fin de la transaction.
    private static final String SELECTION =
            "select v from Ville v join fetch v.departement d ";

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Récupère toutes les villes enregistrées.
     *
     * @return toutes les villes classées par identifiant.
     */
    public List<Ville> extractVilles() {
        return entityManager.createQuery(SELECTION + "order by v.id", Ville.class).getResultList();
    }

    /**
     * Recherche une ville par son identifiant.
     *
     * @param id identifiant recherché
     * @return la ville, ou null si elle n'existe pas
     */
    public Ville extractVille(int id) {
        List<Ville> resultats = entityManager.createQuery(SELECTION + "where v.id = :id", Ville.class)
                .setParameter("id", id).getResultList();
        return resultats.isEmpty() ? null : resultats.getFirst();
    }

    /**
     * Recherche les villes à partir du début de leur nom.
     *
     * @param prefixe début du nom recherché
     * @return les villes correspondantes, sans distinguer les majuscules
     */
    public List<Ville> extractVilles(String prefixe) {
        // Échappe les caractères spéciaux de LIKE pour rechercher le texte tel qu'il a été saisi.
        String motif = prefixe.toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        return entityManager.createQuery(
                        SELECTION + "where lower(v.nom) like :motif escape '!' order by v.id", Ville.class)
                .setParameter("motif", motif).getResultList();
    }

    /**
     * Recherche les villes au-dessus d'une population donnée.
     *
     * @param min borne basse exclue
     * @return les villes dont la population dépasse min
     */
    public List<Ville> extractVilles(int min) {
        return entityManager.createQuery(SELECTION + "where v.population > :min order by v.id", Ville.class)
                .setParameter("min", min).getResultList();
    }

    /**
     * Recherche les villes strictement entre deux populations.
     *
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les villes situées strictement entre les deux bornes
     */
    public List<Ville> extractVilles(int min, int max) {
        return entityManager.createQuery(
                        SELECTION + "where v.population > :min and v.population < :max order by v.id", Ville.class)
                .setParameter("min", min).setParameter("max", max).getResultList();
    }

    /**
     * Récupère les villes rattachées à un département.
     *
     * @param idDepartement département recherché
     * @return les villes de ce département
     */
    public List<Ville> extractVillesDuDepartement(int idDepartement) {
        return entityManager.createQuery(SELECTION + "where d.id = :id order by v.id", Ville.class)
                .setParameter("id", idDepartement).getResultList();
    }

    /**
     * Vérifie si une autre ville du département porte déjà ce nom.
     *
     * @param nom nom à contrôler
     * @param idDepartement département de la ville
     * @param idExclu ville à ignorer, ou 0 lors d'un ajout
     * @return true si une autre ville du département porte ce nom
     */
    public boolean existeParNomEtDepartement(String nom, int idDepartement, int idExclu) {
        return entityManager.createQuery("select count(v) from Ville v where lower(v.nom) = :nom "
                        + "and v.departement.id = :departement and v.id <> :id", Long.class)
                .setParameter("nom", nom.toLowerCase(Locale.ROOT))
                .setParameter("departement", idDepartement).setParameter("id", idExclu).getSingleResult() > 0;
    }

    /**
     * Compte les villes liées à un département.
     *
     * @param idDepartement département à contrôler avant sa suppression
     * @return le nombre de villes qui lui sont rattachées
     */
    public long compterParDepartement(int idDepartement) {
        return entityManager.createQuery("select count(v) from Ville v where v.departement.id = :id", Long.class)
                .setParameter("id", idDepartement).getSingleResult();
    }

    /**
     * Enregistre une nouvelle ville.
     *
     * @param ville nouvelle ville à insérer.
     */
    public void insertVille(Ville ville) {
        entityManager.persist(ville);
        // flush envoie l'insertion et permet de récupérer l'identifiant généré par la base.
        entityManager.flush();
    }

    /**
     * Met à jour une ville en conservant son identifiant.
     *
     * @param ville ville portant les nouvelles données.
     */
    public void modifierVille(Ville ville) {
        entityManager.merge(ville);
        entityManager.flush();
    }

    /**
     * Supprime une ville existante.
     *
     * @param ville ville existante à supprimer.
     */
    public void supprimerVille(Ville ville) {
        entityManager.remove(ville);
        entityManager.flush();
    }

    /**
     * Trie et limite les résultats directement en base.
     * @param idDepartement département concerné
     * @param nombre nombre maximal de villes
     * @return les villes par population décroissante, puis par id en cas d'égalité
     */
    public List<Ville> extractPlusGrandesVilles(int idDepartement, int nombre) {
        return entityManager.createQuery(SELECTION
                        + "where d.id = :id order by v.population desc, v.id asc", Ville.class)
                .setParameter("id", idDepartement).setMaxResults(nombre).getResultList();
    }

    /**
     * Filtre les populations dans un seul département.
     * @param idDepartement département concerné
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les villes correspondant aux trois critères
     */
    public List<Ville> extractVillesDuDepartement(int idDepartement, int min, int max) {
        return entityManager.createQuery(SELECTION
                        + "where d.id = :id and v.population > :min and v.population < :max order by v.id", Ville.class)
                .setParameter("id", idDepartement).setParameter("min", min).setParameter("max", max)
                .getResultList();
    }

}
