package fr.diginamic.projet_spring.services.interfaces;

import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import org.springframework.data.domain.Page;

import java.util.List;

/** Décrit les opérations disponibles sur les villes, sans imposer leur réalisation. */
public interface IVilleService {
    Page<VilleDto> extractVillesPaginees(int page, int taille) throws ExceptionMetier;
    VilleDto extractVille(int idVille) throws ExceptionMetier;
    List<VilleDto> extractVilles(String prefixe) throws ExceptionMetier;
    List<VilleDto> extractVilles(int min) throws ExceptionMetier;
    List<VilleDto> extractVilles(int min, int max) throws ExceptionMetier;
    List<VilleDto> extractVillesDuDepartement(int idDepartement) throws ExceptionMetier;
    List<VilleDto> extractVillesDuDepartement(int idDepartement, int min) throws ExceptionMetier;
    List<VilleDto> extractVillesDuDepartement(int idDepartement, int min, int max) throws ExceptionMetier;
    List<VilleDto> extractPlusGrandesVilles(int idDepartement, int nombre) throws ExceptionMetier;
    List<VilleDto> insertVille(VilleDto donnees) throws ExceptionMetier;
    List<VilleDto> modifierVille(int idVille, VilleDto donnees) throws ExceptionMetier;
    List<VilleDto> supprimerVille(int idVille) throws ExceptionMetier;
}
