package fr.diginamic.projet_spring.mappers;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.entites.Departement;
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
        VilleDto dto = new VilleDto();
        dto.setId(ville.getId());
        dto.setNom(ville.getNom());
        dto.setPopulation(ville.getPopulation());
        dto.setIdDepartement(departement.getId());
        dto.setCodeDepartement(departement.getCode());
        return dto;
    }

    /**
     * Évite de parcourir la collection de villes du département.
     * @param departement entité chargée par le service
     * @return le département et l'id éventuel de sa région
     */
    public DepartementDto versDepartementDto(Departement departement) {
        DepartementDto dto = new DepartementDto();
        dto.setId(departement.getId());
        dto.setCode(departement.getCode());
        dto.setNom(departement.getNom());
        if (departement.getRegion() != null) {
            dto.setIdRegion(departement.getRegion().getId());
        }
        return dto;
    }
}
