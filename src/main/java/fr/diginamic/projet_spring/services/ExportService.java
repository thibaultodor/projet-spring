package fr.diginamic.projet_spring.services;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Ville;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.repositories.DepartementRepository;
import fr.diginamic.projet_spring.repositories.VilleRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Prépare les fichiers en gardant les relations JPA accessibles pendant leur construction. */
@Service
@Transactional(readOnly = true)
public class ExportService {

    private static final BaseColor BLEU = new BaseColor(35, 58, 78);
    private static final BaseColor BORDURE = new BaseColor(220, 226, 231);

    private final VilleRepository villeRepository;
    private final DepartementRepository departementRepository;

    public ExportService(VilleRepository villeRepository, DepartementRepository departementRepository) {
        this.villeRepository = villeRepository;
        this.departementRepository = departementRepository;
    }

    /**
     * Exporte toutes les villes au-dessus du minimum, sans pagination.
     * @param min population minimale exclue
     * @return un CSV UTF-8 ; seul l'en-tête est présent si aucune ville ne correspond
     */
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
     * Construit la fiche PDF d'un département retrouvé par son code.
     * @param code code départemental, par exemple 34, 2A ou 974
     * @return le PDF avec le nom, le code et toutes les villes du département
     * @throws ExceptionMetier si le département est absent ou si le PDF ne peut pas être créé
     */
    public byte[] exporterDepartementPdf(String code) throws ExceptionMetier {
        if (code == null || code.isBlank()) {
            throw new ExceptionMetier("Le code du département est obligatoire");
        }
        Departement departement = departementRepository.findByCodeIgnoreCase(code.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ExceptionMetier("Département introuvable", HttpStatus.NOT_FOUND));
        // unpaged conserve toutes les villes ; le tri reste celui des populations décroissantes.
        List<Ville> villes = villeRepository.findByDepartementIdOrderByPopulationDescIdAsc(
                departement.getId(), Pageable.unpaged());
        String nom = departement.getNom() == null || departement.getNom().isBlank()
                ? "Département " + departement.getCode() : departement.getNom();

        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 44, 44, 48, 44);
        Font texte = new Font(Font.FontFamily.HELVETICA, 11);
        try {
            // iText écrit dans la mémoire : aucun fichier temporaire n'est créé sur le serveur.
            PdfWriter writer = PdfWriter.getInstance(document, sortie);
            writer.setLanguage("fr-FR");
            writer.setPageEvent(new NumerotationPages());
            document.addTitle(nom);
            document.addSubject("Villes du département " + departement.getCode());
            document.addCreator("API de recensement");
            document.open();

            Paragraph titre = new Paragraph(nom, new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BLEU));
            titre.setSpacingAfter(16);
            document.add(titre);
            document.add(new Paragraph("Code du département : " + departement.getCode(), texte));
            document.add(new Paragraph("Nom du département : " + nom, texte));
            Paragraph sousTitre = new Paragraph("Villes du département (" + villes.size() + ")",
                    new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, BLEU));
            sousTitre.setSpacingBefore(20);
            sousTitre.setSpacingAfter(10);
            document.add(sousTitre);

            if (villes.isEmpty()) {
                document.add(new Paragraph("Aucune ville enregistrée dans ce département.", texte));
            } else {
                ajouterTableau(document, villes, texte);
            }
        } catch (DocumentException exception) {
            throw new ExceptionMetier("Impossible de générer le PDF du département", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // Ferme le document pour terminer la structure du PDF avant de renvoyer ses octets.
            document.close();
        }
        return sortie.toByteArray();
    }

    private void ajouterTableau(Document document, List<Ville> villes, Font texte) throws DocumentException {
        PdfPTable tableau = new PdfPTable(new float[]{3, 1});
        tableau.setWidthPercentage(100);
        // Répète les titres de colonnes lorsque la liste passe à la page suivante.
        tableau.setHeaderRows(1);
        Font entete = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        ajouterCellule(tableau, "Nom de la ville", entete, Element.ALIGN_LEFT, BLEU);
        ajouterCellule(tableau, "Population", entete, Element.ALIGN_RIGHT, BLEU);
        int ligne = 0;
        for (Ville ville : villes) {
            BaseColor fond = ligne++ % 2 == 0 ? new BaseColor(246, 248, 250) : BaseColor.WHITE;
            ajouterCellule(tableau, ville.getNom(), texte, Element.ALIGN_LEFT, fond);
            ajouterCellule(tableau, Integer.toString(ville.getPopulation()), texte, Element.ALIGN_RIGHT, fond);
        }
        document.add(tableau);
    }

    private void ajouterCellule(PdfPTable tableau, String valeur, Font police, int alignement, BaseColor fond) {
        PdfPCell cellule = new PdfPCell(new Phrase(valeur, police));
        cellule.setPadding(6);
        cellule.setHorizontalAlignment(alignement);
        cellule.setBorderColor(BORDURE);
        cellule.setBackgroundColor(fond);
        tableau.addCell(cellule);
    }

    private static class NumerotationPages extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            Font police = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), police), document.right(), document.bottom() - 22, 0);
        }
    }
}
