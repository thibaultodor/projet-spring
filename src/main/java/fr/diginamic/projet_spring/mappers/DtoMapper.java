package fr.diginamic.projet_spring.mappers;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.RegionDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.entites.Ville;
import org.springframework.stereotype.Component;

/** Convertit les entités en DTO sans renvoyer leurs relations au client. */
@Component
public class DtoMapper {

    /**
     * Aplatit le département de la ville en deux valeurs simples.
     * @param ville entité chargée par le service
     * @return les données de la ville pour l'API
     */
    public VilleDto versVilleDto(Ville ville) {
        Departement departement = ville.getDepartement();
        return new VilleDto(ville.getId(), ville.getNom(), ville.getPopulation(),
                departement.getId(), departement.getCode());
    }

    /**
     * Évite de parcourir la collection de villes du département.
     * @param departement entité chargée par le service
     * @return le département et l'id éventuel de sa région
     */
    public DepartementDto versDepartementDto(Departement departement) {
        Integer idRegion = departement.getRegion() == null ? null : departement.getRegion().getId();
        return new DepartementDto(departement.getId(), departement.getCode(), departement.getNom(), idRegion);
    }

    /**
     * Copie les informations utiles de la région.
     * @param region entité à convertir
     * @return le DTO destiné à l'API
     */
    public RegionDto versRegionDto(Region region) {
        return new RegionDto(region.getId(), region.getCode(), region.getNom());
    }
}
