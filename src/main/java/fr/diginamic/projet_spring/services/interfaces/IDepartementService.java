package fr.diginamic.projet_spring.services.interfaces;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;

import java.util.List;

/** Contrat des départements partagé par les contrôleurs et les autres services. */
public interface IDepartementService {
    List<DepartementDto> extractDepartements();
    DepartementDto extractDepartement(int id) throws ExceptionMetier;
    List<DepartementDto> insertDepartement(DepartementDto donnees) throws ExceptionMetier;
    List<DepartementDto> modifierDepartement(int id, DepartementDto donnees) throws ExceptionMetier;
    List<DepartementDto> supprimerDepartement(int id) throws ExceptionMetier;

    // Réservé aux échanges internes entre services : cette entité n'est pas renvoyée par les contrôleurs.
    Departement resoudreDepartement(Integer id, String code) throws ExceptionMetier;
    void initData() throws ExceptionMetier;
}
