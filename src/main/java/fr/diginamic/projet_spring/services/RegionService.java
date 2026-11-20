package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.dao.RegionDao;
import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Fournit les régions nécessaires à la création des départements. */
@Service
@Transactional(readOnly = true)
public class RegionService {

    private final RegionDao regionDao;

    public RegionService(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    /**
     * Récupère les régions enregistrées.
     *
     * @return les régions disponibles en base.
     */
    public List<Region> extractRegions() {
        return regionDao.extractRegions();
    }

    /**
     * Recherche une région par son identifiant.
     *
     * @param id identifiant recherché
     * @return la région existante
     * @throws ExceptionMetier si la région est introuvable
     */
    public Region extractRegion(int id) throws ExceptionMetier {
        Region region = regionDao.extractRegion(id);
        if (region == null) {
            throw new ExceptionMetier("Région introuvable", HttpStatus.NOT_FOUND);
        }
        return region;
    }
}
