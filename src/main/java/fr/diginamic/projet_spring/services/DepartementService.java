package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.dao.DepartementDao;
import fr.diginamic.projet_spring.dao.RegionDao;
import fr.diginamic.projet_spring.dao.VilleDao;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/** Contrôle les départements et confie toutes les requêtes aux DAO. */
@Service
@Transactional(readOnly = true)
public class DepartementService {

    private final DepartementDao departementDao;
    private final RegionDao regionDao;
    private final VilleDao villeDao;
    private final Validator validator;

    public DepartementService(DepartementDao departementDao, RegionDao regionDao,
                              VilleDao villeDao, Validator validator) {
        this.departementDao = departementDao;
        this.regionDao = regionDao;
        this.villeDao = villeDao;
        this.validator = validator;
    }

    /**
     * Récupère les départements avec leur région.
     *
     * @return les départements présents en base.
     */
    public List<Departement> extractDepartements() {
        return departementDao.extractDepartements();
    }

    /**
     * Recherche un département par son identifiant.
     *
     * @param id identifiant recherché
     * @return le département existant
     * @throws ExceptionMetier si le département est introuvable
     */
    public Departement extractDepartement(int id) throws ExceptionMetier {
        Departement departement = departementDao.extractDepartement(id);
        if (departement == null) {
            throw new ExceptionMetier("Département introuvable", HttpStatus.NOT_FOUND);
        }
        return departement;
    }

    /**
     * Enregistre un nouveau département.
     *
     * @param departement nouvelles données
     * @return la liste après insertion
     * @throws ExceptionMetier si les données, le code ou la région ne conviennent pas
     */
    // rollbackFor annule aussi l'écriture si notre exception métier est levée.
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<Departement> insertDepartement(Departement departement) throws ExceptionMetier {
        verifierDonnees(departement);
        Region region = retrouverRegion(departement.getRegion());
        verifierCodeDisponible(departement.getCode(), 0);
        departement.setId(0);
        departement.setRegion(region);
        departementDao.insertDepartement(departement);
        return departementDao.extractDepartements();
    }

    /**
     * Met à jour un département existant.
     *
     * @param id identifiant à conserver
     * @param nouvellesDonnees valeurs de remplacement
     * @return la liste après modification
     * @throws ExceptionMetier si le département, la région ou les données sont invalides
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<Departement> modifierDepartement(int id, Departement nouvellesDonnees) throws ExceptionMetier {
        Departement existant = extractDepartement(id);
        verifierDonnees(nouvellesDonnees);
        Region region = retrouverRegion(nouvellesDonnees.getRegion());
        verifierCodeDisponible(nouvellesDonnees.getCode(), id);
        existant.setCode(nouvellesDonnees.getCode());
        existant.setNom(nouvellesDonnees.getNom());
        existant.setRegion(region);
        departementDao.modifierDepartement(existant);
        return departementDao.extractDepartements();
    }

    /**
     * Supprime un département après les contrôles nécessaires.
     *
     * @param id identifiant à supprimer
     * @return les départements restants
     * @throws ExceptionMetier si le département est absent ou contient encore des villes
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<Departement> supprimerDepartement(int id) throws ExceptionMetier {
        Departement departement = extractDepartement(id);
        // Évite de supprimer un parent encore utilisé par des villes.
        if (villeDao.compterParDepartement(id) > 0) {
            throw new ExceptionMetier("Impossible de supprimer un département qui contient des villes",
                    HttpStatus.CONFLICT);
        }
        departementDao.supprimerDepartement(departement);
        return departementDao.extractDepartements();
    }

    private void verifierDonnees(Departement departement) throws ExceptionMetier {
        if (departement == null) {
            throw new ExceptionMetier("Le département est obligatoire");
        }
        if (departement.getCode() != null) {
            departement.setCode(departement.getCode().trim().toUpperCase(Locale.ROOT));
        }
        if (departement.getNom() != null) {
            departement.setNom(departement.getNom().trim());
        }
        // Réutilise les contraintes de l'entité, même si le service est appelé sans contrôleur.
        Set<ConstraintViolation<Departement>> erreurs = validator.validate(departement);
        if (!erreurs.isEmpty()) {
            throw new ExceptionMetier(erreurs.stream().map(ConstraintViolation::getMessage)
                    .sorted().collect(Collectors.joining(" ; ")));
        }
    }

    private Region retrouverRegion(Region reference) throws ExceptionMetier {
        if (reference.getId() <= 0) {
            throw new ExceptionMetier("La région doit avoir un identifiant valide");
        }
        Region region = regionDao.extractRegion(reference.getId());
        if (region == null) {
            throw new ExceptionMetier("Région introuvable", HttpStatus.NOT_FOUND);
        }
        return region;
    }

    private void verifierCodeDisponible(String code, int idExclu) throws ExceptionMetier {
        if (departementDao.existeParCode(code, idExclu)) {
            throw new ExceptionMetier("Le code de département " + code + " existe déjà");
        }
    }
}
