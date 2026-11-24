package fr.diginamic.projet_spring.exports.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement.ReferenceDepartement;
import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement.VilleChiffres;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static fr.diginamic.projet_spring.exports.pdf.AtelierPdf.*;

/** Compose le rapport départemental, ses graphiques et son annexe avec iText. */
@Component
public class RapportDepartementPdf {

    /** @return le rapport complet, avec sommaire, signets et données CSV jointes. */
    public byte[] generer(DonneesRapportDepartement donnees) throws DocumentException, IOException {
        ByteArrayOutputStream flux = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, MARGE, MARGE, 48, 58);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, flux);
            writer.setPdfVersion(PdfWriter.VERSION_1_7);
            writer.setFullCompression();
            writer.setLanguage("fr-FR");
            writer.setViewerPreferences(PdfWriter.PageModeUseOutlines | PdfWriter.DisplayDocTitle);
            document.addTitle(donnees.nom() + " - Villes et population");
            document.addAuthor("Projet Spring");
            document.addSubject("Rapport sur les villes du département " + donnees.code());
            document.addCreator("API de recensement - iText");
            document.open();
            joindreDonnees(writer, donnees);
            AtelierPdf p = new AtelierPdf(writer);
            writer.setPageEvent(new PiedDePage(p));
            StatistiquesRapport s = new StatistiquesRapport(donnees);

            // Le sommaire et les signets partagent les mêmes destinations dans le document.
            ouvrir(p, 0, "Présentation");
            couverture(p, donnees, s);
            document.newPage();
            ouvrir(p, 1, "Les villes les plus peuplées");
            classement(p, donnees, s);
            document.newPage();
            ouvrir(p, 2, "Taille des villes");
            repartition(p, donnees, s);
            document.newPage();
            ouvrir(p, 3, "Comparaison des départements");
            comparaison(p, donnees, s);
            document.newPage();
            ouvrir(p, 4, "Annexe - Liste des villes");
            annexe(document, p, donnees, s);
        } finally {
            document.close();
        }
        return flux.toByteArray();
    }

    private void ouvrir(AtelierPdf p, int section, String titre) {
        p.destination(DESTINATIONS[section]);
        new PdfOutline(p.writer.getRootOutline(), PdfAction.gotoLocalPage(DESTINATIONS[section], false), titre);
    }

    private void joindreDonnees(PdfWriter writer, DonneesRapportDepartement d) throws IOException {
        StringBuilder csv = new StringBuilder("\uFEFFnom,population,codeDepartement,nomDepartement\r\n");
        for (VilleChiffres ville : d.villes()) {
            csv.append(champ(ville.nom())).append(',').append(ville.population()).append(',')
                    .append(champ(d.code())).append(',').append(champ(d.nom())).append("\r\n");
        }
        writer.addFileAttachment("Données des villes du département " + d.code(),
                csv.toString().getBytes(StandardCharsets.UTF_8), null,
                "communes-" + d.code().replaceAll("[^a-zA-Z0-9]", "_") + ".csv");
    }

    private String champ(String texte) { return "\"" + texte.replace("\"", "\"\"") + "\""; }

    private void couverture(AtelierPdf p, DonneesRapportDepartement d, StatistiquesRapport s) {
        p.fond(true);
        p.verreCouverture();
        p.texte("VILLES ET POPULATION", MARGE, 784, 9, true, VERT);
        p.droite(d.dateExport().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH)),
                DROITE, 784, 9, false, SECONDAIRE);
        p.trait(MARGE, 765, DROITE, 765, VERT);

        float taille = 56;
        while (taille > 26 && p.lignesTitre(d.nom(), CONTENU, taille) > 3) {
            taille -= 1;
        }
        float bas = p.titre(d.nom(), MARGE, 690, CONTENU, taille, taille * 1.04f, ENCRE);
        String region = d.region() == null ? "Région non renseignée" : d.region();
        p.paragraphe("Département " + d.code() + " / " + region, MARGE, Math.min(601, bas - 16),
                CONTENU, 11, 16, false, SECONDAIRE);

        String introduction = s.nombreVilles() == 0 ? "Aucune ville enregistrée dans ce département."
                : "La base recense " + entier(s.nombreVilles()) + (s.nombreVilles() == 1 ? " ville" : " villes")
                + " dans ce département, pour une population cumulée de " + entier(s.population()) + " habitants.";
        p.paragraphe(introduction, MARGE, 498, CONTENU - 12, 12.5f, 20, false, ENCRE);

        p.trait(MARGE, 407, DROITE, 407, BORDURE);
        p.texte("SOMMAIRE", MARGE, 376, 8.5f, true, VERT);
        String[] titres = {"Les villes les plus peuplées", "Taille des villes",
                "Comparaison des départements", "Annexe - Liste des villes"};
        for (int index = 0; index < titres.length; index++) {
            float y = 340 - index * 34;
            p.texte(index == 3 ? "A" : "0" + (index + 1), MARGE, y, 9, true, VERT);
            p.texte(titres[index], MARGE + 32, y, 11, false, ENCRE);
            p.droite(Integer.toString(index + 2), DROITE, y, 10, false, SECONDAIRE);
            p.trait(MARGE + 32, y - 12, DROITE, y - 12, BORDURE);
            p.lien(MARGE, y - 12, CONTENU, 30, DESTINATIONS[index + 1]);
        }

        p.titre("Données et périmètre", MARGE, 157, CONTENU, 17, 21, ENCRE);
        p.paragraphe("Source : base de l'application. Les populations peuvent relever d'années différentes. "
                        + "Les totaux et les classements décrivent les données enregistrées à la date de cet export.",
                MARGE, 134, CONTENU, 9, 13, false, SECONDAIRE);
        p.texte("Le fichier CSV des villes est joint à ce document.", MARGE, 83, 9, false, SECONDAIRE);
    }

    private void titre(AtelierPdf p, DonneesRapportDepartement d, String section, String titre) {
        p.entete(d.nom(), section);
        p.titre(titre, MARGE, 731, CONTENU, 29, 33, ENCRE);
    }

    private void classement(AtelierPdf p, DonneesRapportDepartement d, StatistiquesRapport s) {
        titre(p, d, "01", "Les villes les plus peuplées");
        List<VilleChiffres> villes = d.villes().stream().limit(10).toList();
        String introduction = villes.isEmpty() ? "Aucune ville enregistrée dans ce département."
                : s.nombreVilles() == 1 ? "Une seule ville est enregistrée ; il n'y a pas de classement entre villes."
                : s.nombreVilles() <= 10 ? "Le classement porte sur l'ensemble des villes enregistrées."
                : "Les dix premières villes réunissent " + pourcent(s.partTop(10))
                + " des habitants du département.";
        p.paragraphe(introduction, MARGE, 689, CONTENU, 11.2f, 17, false, ENCRE);
        if (villes.isEmpty()) {
            return;
        }

        p.texte("Ville", MARGE + 25, 625, 9, true, SECONDAIRE);
        p.droite("Habitants", DROITE, 625, 9, true, SECONDAIRE);
        p.trait(MARGE, 612, DROITE, 612, BORDURE);
        long maximum = villes.getFirst().population();
        grille(p, 260, 292, 190, 603);
        for (int index = 0; index < villes.size(); index++) {
            VilleChiffres ville = villes.get(index);
            float y = 588 - index * 31;
            p.texte(Integer.toString(index + 1), MARGE, y, 9, false, SECONDAIRE);
            p.texte(p.abreger(ville.nom(), 173, 10.5f, false), MARGE + 25, y, 10.5f, false, ENCRE);
            barre(p, 260, y - 1, 190, 11, ville.population(), maximum, SAUGE);
            p.droite(entier(ville.population()), DROITE, y, 10.5f, false, ENCRE);
        }
        p.titre("Moyenne et médiane", MARGE, 224, CONTENU, 21, 25, ENCRE);
        p.paragraphe("La moyenne s'établit à " + entier(Math.round(s.moyenne()))
                        + " habitants par ville, contre " + entier(Math.round(s.mediane()))
                        + " pour la médiane. La moitié des villes compte au plus la population médiane.",
                MARGE, 191, CONTENU, 11, 17, false, ENCRE);
        p.texte("Moyenne et médiane arrondies à l'habitant.", MARGE, 103, 8.5f, false, SECONDAIRE);
    }

    private void repartition(AtelierPdf p, DonneesRapportDepartement d, StatistiquesRapport s) {
        titre(p, d, "02", "Taille des villes");
        if (s.nombreVilles() == 0) {
            p.texte("Aucune ville enregistrée : la répartition ne peut pas être calculée.", MARGE, 689, 11, false, ENCRE);
            return;
        }
        p.paragraphe("Les villes de moins de 2 000 habitants représentent "
                        + pourcent(StatistiquesRapport.pourcentage(s.petitesVilles(), s.nombreVilles()))
                        + " des villes, pour "
                        + pourcent(StatistiquesRapport.pourcentage(s.populationPetitesVilles(), s.population()))
                        + " des habitants.",
                MARGE, 689, CONTENU, 11.2f, 17, false, ENCRE);
        p.texte("Habitants par ville", MARGE, 625, 9, true, SECONDAIRE);
        p.droite("Villes", 465, 625, 9, true, SECONDAIRE);
        p.droite("Part des villes", DROITE, 625, 9, true, SECONDAIRE);
        p.trait(MARGE, 612, DROITE, 612, BORDURE);
        List<StatistiquesRapport.TranchePopulation> tranches = s.tranches();
        long maximum = tranches.stream().mapToLong(StatistiquesRapport.TranchePopulation::nombre).max().orElse(0);
        grille(p, 252, 357, 145, 603);
        for (int index = 0; index < tranches.size(); index++) {
            StatistiquesRapport.TranchePopulation tranche = tranches.get(index);
            float y = 582 - index * 40;
            p.texte(tranche.etiquette(), MARGE, y, 11, false, ENCRE);
            barre(p, 252, y - 1, 145, 12, tranche.nombre(), maximum, SAUGE);
            p.droite(entier(tranche.nombre()), 465, y, 11, false, ENCRE);
            p.droite(pourcent(StatistiquesRapport.pourcentage(tranche.nombre(), s.nombreVilles())),
                    DROITE, y, 10.5f, false, ENCRE);
        }

        p.titre("Part des habitants", MARGE, 275, CONTENU, 21, 25, ENCRE);
        int nombre = Math.min(5, s.nombreVilles());
        float largeur = (float) (CONTENU * s.partTop(nombre) / 100);
        p.rectangle(MARGE, 224, largeur, 19, VERT);
        p.rectangle(MARGE + largeur, 224, CONTENU - largeur, 19, GRIS);
        legende(p, 191, VERT, nombre == 1 ? "La ville la plus peuplée" : "Les " + nombre + " villes les plus peuplées",
                pourcent(s.partTop(nombre)));
        int autres = s.nombreVilles() - nombre;
        if (autres > 0) {
            legende(p, 165, GRIS, autres == 1 ? "L'autre ville" : "Les " + entier(autres) + " autres villes",
                    pourcent(StatistiquesRapport.pourcentage(s.population() - s.populationTop(nombre), s.population())));
        }
    }

    private void legende(AtelierPdf p, float y, com.itextpdf.text.BaseColor couleur, String libelle, String valeur) {
        p.rectangle(MARGE, y, 8, 8, couleur);
        p.texte(libelle, MARGE + 18, y, 10.5f, false, ENCRE);
        p.droite(valeur, DROITE, y, 10.5f, false, ENCRE);
    }

    private void comparaison(AtelierPdf p, DonneesRapportDepartement d, StatistiquesRapport s) {
        titre(p, d, "03", "Comparaison des départements");
        int rang = s.rang(s.classement());
        String introduction = rang == 0 ? "Classement non disponible (n.d.) : aucune population de référence n'est disponible."
                : s.classement().size() == 1 ? "Ce département est le seul renseigné dans la base."
                : "Le département occupe le " + ordinal(rang) + " rang sur " + s.classement().size()
                + " départements renseignés. Il représente "
                + pourcent(StatistiquesRapport.pourcentage(s.population(), s.populationBase()))
                + " de leur population cumulée.";
        p.paragraphe(introduction, MARGE, 689, CONTENU, 11.2f, 17, false, ENCRE);

        List<ReferenceDepartement> references = s.reperes();
        if (!references.isEmpty()) {
            p.texte("Département", MARGE, 625, 9, true, SECONDAIRE);
            p.droite("Habitants", DROITE, 625, 9, true, SECONDAIRE);
            p.trait(MARGE, 612, DROITE, 612, BORDURE);
            long maximum = references.stream().mapToLong(ReferenceDepartement::population).max().orElse(0);
            grille(p, 260, 348, 190, 603);
            for (int index = 0; index < references.size(); index++) {
                ReferenceDepartement reference = references.get(index);
                boolean courant = reference.id() == d.id();
                float y = 582 - index * 40;
                String nom = reference.nom() == null ? "Département " + reference.code() : reference.nom();
                p.texte(p.abreger(reference.code() + " / " + nom, 196, 10, courant),
                        MARGE, y, 10, courant, courant ? VERT : ENCRE);
                barre(p, 260, y - 1, 190, 12, reference.population(), maximum, courant ? VERT : GRIS);
                p.droite(entier(reference.population()), DROITE, y, 10.5f, courant, courant ? VERT : ENCRE);
            }
            int nombre = Math.min(5, s.classement().size());
            String perimetre = nombre < 2 ? "Département étudié, en vert."
                    : "Les " + nombre + " premiers départements de la base et le département étudié (en vert).";
            p.paragraphe(perimetre,
                    MARGE, 321, CONTENU, 8.5f, 12, false, SECONDAIRE);
        }

        String region = d.region() == null ? "Repères régionaux" : d.region();
        float y = p.titre(region, MARGE, 255, CONTENU, 23, 27, ENCRE) - 13;
        int rangRegion = s.rang(s.region());
        if (d.idRegion() == null || rangRegion == 0) {
            p.paragraphe("La comparaison régionale n'est pas disponible pour ce département.",
                    MARGE, y, CONTENU, 11, 17, false, ENCRE);
            return;
        }
        long populationRegion = s.region().stream().mapToLong(ReferenceDepartement::population).sum();
        String texteRegion = s.region().size() == 1 ? "Ce département est le seul renseigné pour cette région."
                : "Le département occupe le " + ordinal(rangRegion) + " rang parmi les "
                        + s.region().size() + " départements renseignés de la région. Il réunit "
                        + pourcent(StatistiquesRapport.pourcentage(s.population(), populationRegion))
                        + " de leurs habitants.";
        p.paragraphe(texteRegion, MARGE, y, CONTENU, 11, 17, false, ENCRE);
    }

    private String ordinal(int rang) { return rang == 1 ? "1er" : rang + "e"; }

    private void grille(AtelierPdf p, float x, float bas, float largeur, float haut) {
        for (int index = 0; index <= 4; index++) {
            float position = x + largeur * index / 4;
            p.trait(position, bas, position, haut, couleur("#E8ECE6"));
        }
    }

    private void barre(AtelierPdf p, float x, float y, float largeur, float hauteur,
                       long valeur, long maximum, com.itextpdf.text.BaseColor couleur) {
        if (valeur > 0 && maximum > 0) {
            p.rectangle(x, y, Math.max(.5f, largeur * valeur / maximum), hauteur, couleur);
        }
    }

    private void annexe(Document document, AtelierPdf p, DonneesRapportDepartement d, StatistiquesRapport s) {
        int index = 0;
        boolean premierePage = true;
        do {
            p.entete(d.nom(), "ANNEXE");
            float entete;
            if (premierePage) {
                p.titre("Liste des villes", MARGE, 731, CONTENU, 29, 33, ENCRE);
                p.texte("Classement par population décroissante.", MARGE, 689, 10.5f, false, SECONDAIRE);
                entete = 645;
            } else {
                p.texte("Liste des villes (suite)", MARGE, 746, 11, true, ENCRE);
                entete = 716;
            }
            if (d.villes().isEmpty()) {
                p.texte("Aucune ville enregistrée dans ce département.", MARGE, 635, 11, false, ENCRE);
                return;
            }
            p.texte("Rang", MARGE, entete, 8.5f, true, SECONDAIRE);
            p.texte("Ville", 88, entete, 9, true, SECONDAIRE);
            p.droite("Habitants", 457, entete, 9, true, SECONDAIRE);
            p.droite("Part du total", DROITE, entete, 9, true, SECONDAIRE);
            p.trait(MARGE, entete - 12, DROITE, entete - 12, VERT);
            float y = entete - 33;
            // Une ligne suit la hauteur du nom complet ; elle n'est jamais coupée entre deux pages.
            while (index < d.villes().size()) {
                VilleChiffres ville = d.villes().get(index);
                List<String> lignes = p.lignes(ville.nom(), 282, 10.5f, false);
                float hauteur = Math.max(20, lignes.size() * 13 + 7);
                if (y - hauteur < 82) {
                    break;
                }
                p.texte(Integer.toString(index + 1), MARGE, y, 9.5f, false, SECONDAIRE);
                float ligneY = y;
                for (String ligne : lignes) {
                    p.texte(ligne, 88, ligneY, 10.5f, false, ENCRE);
                    ligneY -= 13;
                }
                p.droite(entier(ville.population()), 457, y, 10.5f, false, ENCRE);
                p.droite(pourcent(StatistiquesRapport.pourcentage(ville.population(), s.population())),
                        DROITE, y, 9.5f, false, SECONDAIRE);
                p.trait(MARGE, y - hauteur + 13, DROITE, y - hauteur + 13, BORDURE);
                y -= hauteur;
                index++;
            }
            premierePage = false;
            if (index < d.villes().size()) {
                document.newPage();
            }
        } while (index < d.villes().size());
    }

    private static final class PiedDePage extends PdfPageEventHelper {
        private final AtelierPdf p;
        private final PdfTemplate total;

        private PiedDePage(AtelierPdf p) {
            this.p = p;
            total = p.canvas.createTemplate(24, 13);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            p.trait(MARGE, 58, DROITE, 58, BORDURE);
            p.texte("Villes et population", MARGE, 38, 8, false, SECONDAIRE);
            p.droite("Page " + writer.getPageNumber() + " /", DROITE - 26, 38, 8, false, SECONDAIRE);
            p.canvas.addTemplate(total, DROITE - 22, 38);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            total.beginText();
            total.setFontAndSize(p.regular, 8);
            total.setColorFill(SECONDAIRE);
            total.setTextMatrix(0, 0);
            total.showText(Integer.toString(writer.getPageNumber()));
            total.endText();
        }
    }
}
