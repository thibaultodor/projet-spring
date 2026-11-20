package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.dao.RegionDao;
import fr.diginamic.projet_spring.dto.RegionDto;
import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.mappers.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Fournit les régions sous forme de DTO pour les échanges avec le client. */
@Service
@Transactional(readOnly = true)
public class RegionService {

    private final RegionDao regionDao;
    private final DtoMapper dtoMapper;

    public RegionService(RegionDao regionDao, DtoMapper dtoMapper) {
        this.regionDao = regionDao;
        this.dtoMapper = dtoMapper;
    }

    /** @return les DTO des régions disponibles. */
    public List<RegionDto> extractRegions() {
        return regionDao.extractRegions().stream().map(dtoMapper::versRegionDto).toList();
    }

    /**
     * Retrouve une région par id.
     * @param id identifiant recherché
     * @return son DTO
     * @throws ExceptionMetier si elle est absente
     */
    public RegionDto extractRegion(int id) throws ExceptionMetier {
        Region region = regionDao.extractRegion(id);
        if (region == null) {
            throw new ExceptionMetier("Région introuvable", HttpStatus.NOT_FOUND);
        }
        return dtoMapper.versRegionDto(region);
    }
}
