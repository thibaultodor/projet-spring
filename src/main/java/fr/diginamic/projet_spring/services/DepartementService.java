package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.services.interfaces.IDepartementService;

import fr.diginamic.projet_spring.repositories.DepartementRepository;
import fr.diginamic.projet_spring.dao.RegionDao;
import fr.diginamic.projet_spring.repositories.VilleRepository;
import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.DepartementApiDto;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.mappers.DtoMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

/** Contrôle les départements et renvoie des DTO indépendants des relations JPA. */
@Service
@Transactional(readOnly = true)
public class DepartementService implements IDepartementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DepartementService.class);
    private static final String URL_DEPARTEMENTS = "https://geo.api.gouv.fr/departements";

    private final DepartementRepository departementRepository;
    private final RegionDao regionDao;
    private final VilleRepository villeRepository;
    private final Validator validator;
    private final DtoMapper dtoMapper;
    private final RestTemplate restTemplate;
    private final boolean initialisationActive;

    public DepartementService(DepartementRepository departementRepository, RegionDao regionDao,
                              VilleRepository villeRepository, Validator validator, DtoMapper dtoMapper,
                              RestTemplate restTemplate, @Value("${application.init:false}") boolean initialisationActive) {
        this.departementRepository = departementRepository;
        this.regionDao = regionDao;
        this.villeRepository = villeRepository;
        this.validator = validator;
        this.dtoMapper = dtoMapper;
        this.restTemplate = restTemplate;
        this.initialisationActive = initialisationActive;
    }

    /**
     * Convertit la liste sans parcourir les villes de chaque département.
     * @return les départements destinés à l'API
     */
    @Override
    public List<DepartementDto> extractDepartements() {
        List<Departement> departements = departementRepository.findAllByOrderByIdAsc();
        List<DepartementDto> resultat = new ArrayList<>();
        for (Departement departement : departements) {
            resultat.add(dtoMapper.versDepartementDto(departement));
        }
        return resultat;
    }

    /**
     * Recherche un département existant.
     * @param id identifiant recherché
     * @return son DTO
     * @throws ExceptionMetier si le département est absent
     */
    @Override
    public DepartementDto extractDepartement(int id) throws ExceptionMetier {
        return dtoMapper.versDepartementDto(trouverEntite(id));
    }

    /**
     * Enregistre les données reçues dans un DTO.
     * @param donnees nouveau département
     * @return la liste après insertion
     * @throws ExceptionMetier si les données ou la région sont invalides
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    @Override
    public List<DepartementDto> insertDepartement(DepartementDto donnees) throws ExceptionMetier {
        creerEntite(donnees);
        return extractDepartements();
    }

    /**
     * Modifie un département en conservant son identifiant.
     * @param id département à modifier
     * @param donnees nouvelles données ; une région omise conserve le lien existant
     * @return la liste après modification
     * @throws ExceptionMetier si un contrôle échoue
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    @Override
    public List<DepartementDto> modifierDepartement(int id, DepartementDto donnees) throws ExceptionMetier {
        Departement existant = trouverEntite(id);
        verifierDonnees(donnees);
        verifierCodeDisponible(donnees.getCode(), id);
        Region region = existant.getRegion();
        if (donnees.getIdRegion() != null) {
            region = trouverRegion(donnees.getIdRegion());
        }
        existant.setCode(donnees.getCode());
        existant.setNom(donnees.getNom());
        existant.setRegion(region);
        departementRepository.saveAndFlush(existant);
        return extractDepartements();
    }

    /**
     * Supprime seulement un département sans villes.
     * @param id département à supprimer
     * @return les départements restants
     * @throws ExceptionMetier si le département est absent ou encore utilisé
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    @Override
    public List<DepartementDto> supprimerDepartement(int id) throws ExceptionMetier {
        Departement departement = trouverEntite(id);
        if (villeRepository.existsByDepartementId(id)) {
            throw new ExceptionMetier("Impossible de supprimer un département qui contient des villes",
                    HttpStatus.CONFLICT);
        }
        departementRepository.delete(departement);
        departementRepository.flush();
        return extractDepartements();
    }

    /**
     * Retrouve le département d'une ville par id ou par code.
     * Un code nouveau crée un département avec un nom provisoire et sans région.
     * @param id identifiant facultatif
     * @param code code facultatif
     * @return l'entité à rattacher à la ville, uniquement pour l'usage interne des services
     * @throws ExceptionMetier si les références sont absentes, incohérentes ou invalides
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    @Override
    public Departement resoudreDepartement(Integer id, String code) throws ExceptionMetier {
        String codeNormalise = normaliserCode(code);
        if (id == null && codeNormalise == null) {
            throw new ExceptionMetier("L’identifiant ou le code du département doit être renseigné");
        }
        if (id != null && id <= 0) {
            throw new ExceptionMetier("L’identifiant du département doit être positif");
        }
        if (codeNormalise != null && (codeNormalise.length() < 2 || codeNormalise.length() > 3)) {
            throw new ExceptionMetier("Le code du département doit contenir 2 ou 3 caractères");
        }

        // Un id connu reste prioritaire. Un code fourni en plus doit désigner le même département.
        if (id != null) {
            Departement trouve = departementRepository.findById(id).orElse(null);
            if (trouve != null) {
                if (codeNormalise != null && !trouve.getCode().equalsIgnoreCase(codeNormalise)) {
                    throw new ExceptionMetier("L’identifiant et le code du département ne correspondent pas");
                }
                return trouve;
            }
        }

        // Un id inconnu peut être remplacé par un code connu ou par la création d'un nouveau code.
        if (codeNormalise != null) {
            Departement trouve = departementRepository.findByCodeIgnoreCase(codeNormalise).orElse(null);
            if (trouve != null) {
                return trouve;
            }
            DepartementDto nouveau = new DepartementDto();
            nouveau.setCode(codeNormalise);
            nouveau.setNom("Département " + codeNormalise);
            return creerEntite(nouveau);
        }
        throw new ExceptionMetier("Département inconnu", HttpStatus.NOT_FOUND);
    }

    private Departement creerEntite(DepartementDto donnees) throws ExceptionMetier {
        verifierDonnees(donnees);
        verifierCodeDisponible(donnees.getCode(), 0);
        Region region = null;
        if (donnees.getIdRegion() != null) {
            region = trouverRegion(donnees.getIdRegion());
        }
        Departement departement = new Departement(donnees.getCode(), donnees.getNom(), region);
        departementRepository.saveAndFlush(departement);
        return departement;
    }

    private Departement trouverEntite(int id) throws ExceptionMetier {
        Departement departement = departementRepository.findById(id).orElse(null);
        if (departement == null) {
            throw new ExceptionMetier("Département introuvable", HttpStatus.NOT_FOUND);
        }
        return departement;
    }

    private void verifierDonnees(DepartementDto donnees) throws ExceptionMetier {
        if (donnees == null) {
            throw new ExceptionMetier("Le département est obligatoire");
        }
        donnees.setCode(normaliserCode(donnees.getCode()));
        if (donnees.getNom() != null) {
            donnees.setNom(donnees.getNom().trim());
        }
        // Bean Validation vérifie les annotations du DTO.
        Set<ConstraintViolation<DepartementDto>> erreurs = validator.validate(donnees);
        if (!erreurs.isEmpty()) {
            List<String> messages = new ArrayList<>();
            for (ConstraintViolation<DepartementDto> erreur : erreurs) {
                messages.add(erreur.getMessage());
            }
            Collections.sort(messages);
            throw new ExceptionMetier(String.join(" ; ", messages));
        }
    }

    private String normaliserCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Region trouverRegion(int id) throws ExceptionMetier {
        Region region = regionDao.extractRegion(id);
        if (region == null) {
            throw new ExceptionMetier("Région introuvable", HttpStatus.NOT_FOUND);
        }
        return region;
    }

    private void verifierCodeDisponible(String code, int idExclu) throws ExceptionMetier {
        if (departementRepository.existsByCodeIgnoreCaseAndIdNot(code, idExclu)) {
            throw new ExceptionMetier("Le code de département " + code + " existe déjà");
        }
    }

    /**
     * Met à jour les noms des départements existants à partir de l'API officielle.
     * @throws ExceptionMetier si l'API est indisponible ou renvoie des données invalides
     */
    @Transactional(rollbackFor = ExceptionMetier.class)
    @Override
    public void initData() throws ExceptionMetier {
        // Désactivée : aucun appel HTTP ni aucune recherche de département n'est effectué.
        if (!initialisationActive) {
            return;
        }

        DepartementApiDto[] donnees;
        try {
            // RestTemplate transforme le tableau JSON en objets Java.
            donnees = restTemplate.getForObject(URL_DEPARTEMENTS, DepartementApiDto[].class);
        } catch (RestClientException exception) {
            throw new ExceptionMetier("Impossible de récupérer les départements auprès de l’API géographique",
                    HttpStatus.BAD_GATEWAY);
        }
        if (donnees == null || donnees.length == 0) {
            throw new ExceptionMetier("L’API géographique ne renvoie aucun département", HttpStatus.BAD_GATEWAY);
        }

        // Vérifie toute la réponse avant de commencer les modifications en base.
        Map<String, String> nomsParCode = new HashMap<>();
        for (DepartementApiDto donnee : donnees) {
            if (donnee == null || donnee.getNom() == null) {
                throw new ExceptionMetier("L’API géographique renvoie un département invalide", HttpStatus.BAD_GATEWAY);
            }
            String code = normaliserCode(donnee.getCode());
            String nom = donnee.getNom().trim();
            if (code == null || code.length() < 2 || code.length() > 3 || nom.length() < 2 || nom.length() > 100) {
                throw new ExceptionMetier("L’API géographique renvoie un département invalide", HttpStatus.BAD_GATEWAY);
            }
            // Un même code ne doit pas apparaître deux fois dans la réponse.
            if (nomsParCode.containsKey(code)) {
                throw new ExceptionMetier("L’API géographique renvoie un département invalide", HttpStatus.BAD_GATEWAY);
            }
            nomsParCode.put(code, nom);
        }

        List<Departement> modifies = new ArrayList<>();
        for (Departement departement : departementRepository.findAllByOrderByIdAsc()) {
            String nom = nomsParCode.get(normaliserCode(departement.getCode()));
            // Un code local inconnu de l'API est conservé. Seul le nom peut changer.
            if (nom != null && !nom.equals(departement.getNom())) {
                departement.setNom(nom);
                modifies.add(departement);
            }
        }
        if (!modifies.isEmpty()) {
            departementRepository.saveAllAndFlush(modifies);
        }
        LOGGER.info("Initialisation des noms terminée : {} département(s) mis à jour.", modifies.size());
    }
}
