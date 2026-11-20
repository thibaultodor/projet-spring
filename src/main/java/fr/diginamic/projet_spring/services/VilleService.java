package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.dao.DepartementDao;
import fr.diginamic.projet_spring.dao.VilleDao;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Ville;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Porte les contrôles métier des villes, sans accéder directement à EntityManager. */
@Service
@Transactional(readOnly = true)
public class VilleService {

    private final VilleDao villeDao;
    private final DepartementDao departementDao;
    private final Validator validator;

    public VilleService(VilleDao villeDao, DepartementDao departementDao, Validator validator) {
        this.villeDao = villeDao;
        this.departementDao = departementDao;
        this.validator = validator;
    }

    /**
     * Récupère toutes les villes enregistrées.
     *
     * @return les villes présentes en base.
     */
    public List<Ville> extractVilles() {
        return villeDao.extractVilles();
    }

    /**
     * Recherche une ville par son identifiant.
     *
     * @param idVille identifiant recherché
     * @return la ville existante
     * @throws ExceptionMetier si la ville est introuvable
     */
    public Ville extractVille(int idVille) throws ExceptionMetier {
        Ville ville = villeDao.extractVille(idVille);
        if (ville == null) {
            throw new ExceptionMetier("Ville introuvable", HttpStatus.NOT_FOUND);
        }
        return ville;
    }

    /**
     * Recherche les villes à partir du début de leur nom.
     *
     * @param prefixe début du nom recherché
     * @return les villes dont le nom commence par ce texte
     * @throws ExceptionMetier si le texte est vide ou si aucune ville ne correspond
     */
    public List<Ville> extractVilles(String prefixe) throws ExceptionMetier {
        if (prefixe == null || prefixe.isBlank()) {
            throw new ExceptionMetier("Le début du nom est obligatoire");
        }
        List<Ville> villes = villeDao.extractVilles(prefixe);
        if (villes.isEmpty()) {
            throw new ExceptionMetier("Aucune ville dont le nom commence par " + prefixe + " n’a été trouvée",
                    HttpStatus.NOT_FOUND);
        }
        return villes;
    }

    /**
     * Recherche les villes au-dessus d'une population donnée.
     *
     * @param min population minimale exclue
     * @return les villes dont la population dépasse min
     * @throws ExceptionMetier si aucune ville ne correspond
     */
    public List<Ville> extractVilles(int min) throws ExceptionMetier {
        List<Ville> villes = villeDao.extractVilles(min);
        if (villes.isEmpty()) {
            throw new ExceptionMetier("Aucune ville n’a une population supérieure à " + min,
                    HttpStatus.NOT_FOUND);
        }
        return villes;
    }

    /**
     * Recherche les villes strictement entre deux populations.
     *
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les villes dont la population est strictement comprise entre les bornes
     * @throws ExceptionMetier si l'intervalle est invalide ou sans résultat
     */
    public List<Ville> extractVilles(int min, int max) throws ExceptionMetier {
        if (min >= max) {
            throw new ExceptionMetier("La borne min doit être inférieure à la borne max");
        }
        List<Ville> villes = villeDao.extractVilles(min, max);
        if (villes.isEmpty()) {
            throw new ExceptionMetier("Aucune ville n’a une population comprise entre " + min + " et " + max,
                    HttpStatus.NOT_FOUND);
        }
        return villes;
    }

    /**
     * Récupère les villes rattachées à un département.
     *
     * @param idDepartement département recherché
     * @return ses villes, ou une liste vide s'il n'en contient aucune
     * @throws ExceptionMetier si le département n'existe pas
     */
    public List<Ville> extractVillesDuDepartement(int idDepartement) throws ExceptionMetier {
        retrouverDepartement(idDepartement);
        return villeDao.extractVillesDuDepartement(idDepartement);
    }

    /**
     * Enregistre une nouvelle ville.
     *
     * @param ville nouvelle ville
     * @return la liste après insertion
     * @throws ExceptionMetier si les données, le département ou le nom sont invalides
     */
    // rollbackFor annule aussi l'écriture si notre exception métier est levée.
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<Ville> insertVille(Ville ville) throws ExceptionMetier {
        verifierDonnees(ville);
        Departement departement = retrouverDepartement(ville.getDepartement().getId());
        verifierNomDisponible(ville.getNom(), departement.getId(), 0);
        // Ignore l'id envoyé par le client : la base attribue celui de la nouvelle ligne.
        ville.setId(0);
        ville.setDepartement(departement);
        villeDao.insertVille(ville);
        return villeDao.extractVilles();
    }

    /**
     * Met à jour une ville en conservant son identifiant.
     *
     * @param idVille identifiant à conserver
     * @param villeModifiee nouvelles données
     * @return la liste après modification
     * @throws ExceptionMetier si la ville, le département ou les données sont invalides
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<Ville> modifierVille(int idVille, Ville villeModifiee) throws ExceptionMetier {
        Ville existante = extractVille(idVille);
        verifierDonnees(villeModifiee);
        Departement departement = retrouverDepartement(villeModifiee.getDepartement().getId());
        verifierNomDisponible(villeModifiee.getNom(), departement.getId(), idVille);
        existante.setNom(villeModifiee.getNom());
        existante.setPopulation(villeModifiee.getPopulation());
        existante.setDepartement(departement);
        villeDao.modifierVille(existante);
        return villeDao.extractVilles();
    }

    /**
     * Supprime une ville existante.
     *
     * @param idVille identifiant à supprimer
     * @return les villes restantes
     * @throws ExceptionMetier si la ville n'existe pas
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<Ville> supprimerVille(int idVille) throws ExceptionMetier {
        Ville ville = extractVille(idVille);
        villeDao.supprimerVille(ville);
        return villeDao.extractVilles();
    }

    private void verifierDonnees(Ville ville) throws ExceptionMetier {
        if (ville == null) {
            throw new ExceptionMetier("La ville est obligatoire");
        }
        if (ville.getNom() != null) {
            ville.setNom(ville.getNom().trim());
        }
        // Réutilise Bean Validation pour protéger aussi les appels directs au service.
        Set<ConstraintViolation<Ville>> erreurs = validator.validate(ville);
        if (!erreurs.isEmpty()) {
            throw new ExceptionMetier(erreurs.stream().map(ConstraintViolation::getMessage)
                    .sorted().collect(Collectors.joining(" ; ")));
        }
    }

    private Departement retrouverDepartement(int id) throws ExceptionMetier {
        if (id <= 0) {
            throw new ExceptionMetier("Le département doit avoir un identifiant valide");
        }
        Departement departement = departementDao.extractDepartement(id);
        if (departement == null) {
            throw new ExceptionMetier("Département introuvable", HttpStatus.NOT_FOUND);
        }
        return departement;
    }

    private void verifierNomDisponible(String nom, int idDepartement, int idExclu) throws ExceptionMetier {
        // Deux départements peuvent avoir des villes homonymes, mais pas un même département.
        if (villeDao.existeParNomEtDepartement(nom, idDepartement, idExclu)) {
            throw new ExceptionMetier("La ville \"" + nom + "\" existe déjà dans ce département");
        }
    }
}
