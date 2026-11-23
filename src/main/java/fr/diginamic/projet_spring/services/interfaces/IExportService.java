package fr.diginamic.projet_spring.services.interfaces;

import fr.diginamic.projet_spring.exceptions.ExceptionMetier;

/** Contrat des exports : les contrôleurs reçoivent les fichiers sous forme d'octets. */
public interface IExportService {
    byte[] exporterVillesCsv(int min);
    byte[] exporterDepartementPdf(String code) throws ExceptionMetier;
}
