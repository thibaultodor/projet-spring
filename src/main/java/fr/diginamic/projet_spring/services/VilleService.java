package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.dao.VilleDao;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Ville;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.mappers.DtoMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Applique les règles métier et convertit les entités en DTO pendant la transaction. */
@Service
@Transactional(readOnly = true)
public class VilleService {

    private final VilleDao villeDao;
    private final DepartementService departementService;
    private final Validator validator;
    private final DtoMapper dtoMapper;

    public VilleService(VilleDao villeDao, DepartementService departementService,
                         Validator validator, DtoMapper dtoMapper) {
        this.villeDao = villeDao;
        this.departementService = departementService;
        this.validator = validator;
        this.dtoMapper = dtoMapper;
    }

    /** @return les villes de l'API, sans relations JPA imbriquées. */
    public List<VilleDto> extractVilles() {
        return convertir(villeDao.extractVilles());
    }

    /**
     * Recherche une ville par son identifiant.
     * @param idVille identifiant recherché
     * @return le DTO de la ville
     * @throws ExceptionMetier si elle est absente
     */
    public VilleDto extractVille(int idVille) throws ExceptionMetier {
        return dtoMapper.versVilleDto(trouverEntite(idVille));
    }

    /**
     * Recherche les villes par début du nom.
     * @param prefixe texte recherché
     * @return les DTO correspondants
     * @throws ExceptionMetier si le texte est vide ou sans résultat
     */
    public List<VilleDto> extractVilles(String prefixe) throws ExceptionMetier {
        if (prefixe == null || prefixe.isBlank()) {
            throw new ExceptionMetier("Le début du nom est obligatoire");
        }
        List<Ville> villes = villeDao.extractVilles(prefixe);
        if (villes.isEmpty()) {
            throw new ExceptionMetier("Aucune ville dont le nom commence par " + prefixe + " n’a été trouvée",
                    HttpStatus.NOT_FOUND);
        }
        return convertir(villes);
    }

    /**
     * Recherche les populations au-dessus d'un minimum.
     * @param min borne exclue
     * @return les DTO correspondants
     * @throws ExceptionMetier si aucune ville ne correspond
     */
    public List<VilleDto> extractVilles(int min) throws ExceptionMetier {
        List<Ville> villes = villeDao.extractVilles(min);
        if (villes.isEmpty()) {
            throw new ExceptionMetier("Aucune ville n’a une population supérieure à " + min,
                    HttpStatus.NOT_FOUND);
        }
        return convertir(villes);
    }

    /**
     * Recherche les populations entre deux bornes exclues.
     * @param min borne basse
     * @param max borne haute
     * @return les DTO correspondants
     * @throws ExceptionMetier si l'intervalle est invalide ou vide
     */
    public List<VilleDto> extractVilles(int min, int max) throws ExceptionMetier {
        verifierBornes(min, max);
        List<Ville> villes = villeDao.extractVilles(min, max);
        if (villes.isEmpty()) {
            throw new ExceptionMetier("Aucune ville n’a une population comprise entre " + min + " et " + max,
                    HttpStatus.NOT_FOUND);
        }
        return convertir(villes);
    }

    /**
     * Liste les villes d'un département existant.
     * @param idDepartement département recherché
     * @return les DTO, éventuellement dans une liste vide
     * @throws ExceptionMetier si le département est absent
     */
    public List<VilleDto> extractVillesDuDepartement(int idDepartement) throws ExceptionMetier {
        departementService.extractDepartement(idDepartement);
        return convertir(villeDao.extractVillesDuDepartement(idDepartement));
    }

    /**
     * Limite les résultats en base et conserve un ordre déterministe en cas d'égalité.
     * @param idDepartement département recherché
     * @param nombre nombre maximal de villes, supérieur ou égal à 1
     * @return les DTO par population décroissante
     * @throws ExceptionMetier si le nombre est invalide ou le département absent
     */
    public List<VilleDto> extractPlusGrandesVilles(int idDepartement, int nombre) throws ExceptionMetier {
        if (nombre < 1) {
            throw new ExceptionMetier("Le nombre de villes doit être supérieur ou égal à 1");
        }
        departementService.extractDepartement(idDepartement);
        return convertir(villeDao.extractPlusGrandesVilles(idDepartement, nombre));
    }

    /**
     * Filtre les villes d'un seul département par population.
     * @param idDepartement département recherché
     * @param min borne basse exclue
     * @param max borne haute exclue
     * @return les DTO, éventuellement dans une liste vide
     * @throws ExceptionMetier si les bornes sont invalides ou le département absent
     */
    public List<VilleDto> extractVillesDuDepartement(int idDepartement, int min, int max) throws ExceptionMetier {
        verifierBornes(min, max);
        departementService.extractDepartement(idDepartement);
        return convertir(villeDao.extractVillesDuDepartement(idDepartement, min, max));
    }

    /**
     * Vérifie le DTO, résout son département puis crée une nouvelle entité.
     * @param donnees ville à créer
     * @return les villes après insertion
     * @throws ExceptionMetier si un contrôle échoue
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<VilleDto> insertVille(VilleDto donnees) throws ExceptionMetier {
        verifierDonnees(donnees);
        // La création éventuelle du département et celle de la ville partagent la transaction.
        Departement departement = departementService.resoudreDepartement(
                donnees.getIdDepartement(), donnees.getCodeDepartement());
        verifierNomDisponible(donnees.getNom(), departement.getId(), 0);
        Ville ville = new Ville(donnees.getNom(), donnees.getPopulation(), departement);
        villeDao.insertVille(ville);
        return extractVilles();
    }

    /**
     * Modifie l'entité avec les valeurs du DTO, sans changer son identifiant.
     * @param idVille identifiant de la ville existante
     * @param donnees nouvelles valeurs
     * @return les villes après modification
     * @throws ExceptionMetier si la ville ou ses nouvelles données sont invalides
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<VilleDto> modifierVille(int idVille, VilleDto donnees) throws ExceptionMetier {
        Ville existante = trouverEntite(idVille);
        verifierDonnees(donnees);
        Departement departement = departementService.resoudreDepartement(
                donnees.getIdDepartement(), donnees.getCodeDepartement());
        verifierNomDisponible(donnees.getNom(), departement.getId(), idVille);
        existante.setNom(donnees.getNom());
        existante.setPopulation(donnees.getPopulation());
        existante.setDepartement(departement);
        villeDao.modifierVille(existante);
        return extractVilles();
    }

    /**
     * Supprime une ville existante.
     * @param idVille identifiant à supprimer
     * @return les villes restantes
     * @throws ExceptionMetier si la ville est absente
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    public List<VilleDto> supprimerVille(int idVille) throws ExceptionMetier {
        villeDao.supprimerVille(trouverEntite(idVille));
        return extractVilles();
    }

    private List<VilleDto> convertir(List<Ville> villes) {
        // Cette copie termine l'accès aux relations avant que le contrôleur renvoie du JSON.
        return villes.stream().map(dtoMapper::versVilleDto).toList();
    }

    private Ville trouverEntite(int id) throws ExceptionMetier {
        Ville ville = villeDao.extractVille(id);
        if (ville == null) {
            throw new ExceptionMetier("Ville introuvable", HttpStatus.NOT_FOUND);
        }
        return ville;
    }

    private void verifierDonnees(VilleDto donnees) throws ExceptionMetier {
        if (donnees == null) {
            throw new ExceptionMetier("La ville est obligatoire");
        }
        if (donnees.getNom() != null) {
            donnees.setNom(donnees.getNom().trim());
        }
        Set<ConstraintViolation<VilleDto>> erreurs = validator.validate(donnees);
        if (!erreurs.isEmpty()) {
            throw new ExceptionMetier(erreurs.stream().map(ConstraintViolation::getMessage)
                    .sorted().collect(Collectors.joining(" ; ")));
        }
    }

    private void verifierBornes(int min, int max) throws ExceptionMetier {
        if (min >= max) {
            throw new ExceptionMetier("La borne min doit être inférieure à la borne max");
        }
    }

    private void verifierNomDisponible(String nom, int idDepartement, int idExclu) throws ExceptionMetier {
        if (villeDao.existeParNomEtDepartement(nom, idDepartement, idExclu)) {
            throw new ExceptionMetier("La ville \"" + nom + "\" existe déjà dans ce département");
        }
    }
}
