package fr.diginamic.projet_spring.services;

import com.itextpdf.text.DocumentException;
import fr.diginamic.projet_spring.services.interfaces.IExportService;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Ville;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement;
import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement.ReferenceDepartement;
import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement.VilleChiffres;
import fr.diginamic.projet_spring.exports.pdf.RapportDepartementPdf;
import fr.diginamic.projet_spring.repositories.DepartementRepository;
import fr.diginamic.projet_spring.repositories.VilleRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

/** Charge les données des exports ; la mise en page PDF est confiée à son générateur. */
@Service
@Transactional(readOnly = true)
public class ExportService implements IExportService {
    private final VilleRepository villeRepository;
    private final DepartementRepository departementRepository;
    private final RapportDepartementPdf rapportPdf;

    public ExportService(VilleRepository villeRepository, DepartementRepository departementRepository,
                         RapportDepartementPdf rapportPdf) {
        this.villeRepository = villeRepository;
        this.departementRepository = departementRepository;
        this.rapportPdf = rapportPdf;
    }

    /**
     * Exporte toutes les villes au-dessus du minimum, sans pagination.
     * @param min population minimale exclue
     * @return un CSV UTF-8 ; seul l'en-tête est présent si aucune ville ne correspond
     */
    @Override
    public byte[] exporterVillesCsv(int min) {
        List<Ville> villes = villeRepository.findByPopulationGreaterThanOrderByPopulationDescIdAsc(min);
        // Le marqueur UTF-8 aide les tableurs à afficher correctement les accents.
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Nom de la ville,Nombre d'habitants,Code département,Nom du département\r\n");
        for (Ville ville : villes) {
            Departement departement = ville.getDepartement();
            csv.append(champCsv(ville.getNom())).append(',')
                    .append(ville.getPopulation()).append(',')
                    .append(champCsv(departement.getCode())).append(',')
                    .append(champCsv(departement.getNom())).append("\r\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String champCsv(String valeur) {
        // Les guillemets protègent les virgules et les retours à la ligne présents dans un nom.
        return "\"" + (valeur == null ? "" : valeur.replace("\"", "\"\"")) + "\"";
    }


    /**
     * Prépare le portrait, les graphiques, les comparaisons et l'annuaire du département.
     * @param code code départemental, par exemple 34, 2A ou 974
     * @return le rapport complet avec ses polices et sa navigation embarquées
     * @throws ExceptionMetier si le département est absent ou si le PDF ne peut pas être créé
     */
    @Override
    public byte[] exporterDepartementPdf(String code) throws ExceptionMetier {
        if (code == null || code.isBlank()) {
            throw new ExceptionMetier("Le code du département est obligatoire");
        }
        Departement departement = departementRepository.findByCodeIgnoreCase(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ExceptionMetier("Département introuvable", HttpStatus.NOT_FOUND));
        List<VilleChiffres> villes = villeRepository.findByDepartementIdOrderByPopulationDescIdAsc(
                        departement.getId(), Pageable.unpaged()).stream()
                .map(ville -> new VilleChiffres(ville.getId(), ville.getNom(), ville.getPopulation())).toList();
        List<ReferenceDepartement> references = departementRepository.extraireStatistiquesRapport().stream()
                .map(reference -> new ReferenceDepartement(reference.getId(), reference.getCode(), reference.getNom(),
                        reference.getIdRegion(), reference.getPopulation(), reference.getNombreVilles())).toList();
        String nom = departement.getNom() == null || departement.getNom().isBlank()
                ? "Département " + departement.getCode() : departement.getNom();
        Integer idRegion = departement.getRegion() == null ? null : departement.getRegion().getId();
        String region = departement.getRegion() == null ? null : departement.getRegion().getNom();
        // Cette copie termine le chargement JPA avant de passer au dessin du document.
        DonneesRapportDepartement donnees = new DonneesRapportDepartement(departement.getId(), departement.getCode(),
                nom, idRegion, region, ZonedDateTime.now(ZoneId.of("Europe/Paris")), villes, references);
        try {
            return rapportPdf.generer(donnees);
        } catch (IOException | DocumentException exception) {
            throw new ExceptionMetier("Impossible de générer le PDF du département", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
