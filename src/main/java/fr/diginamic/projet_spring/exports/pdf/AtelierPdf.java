package fr.diginamic.projet_spring.exports.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Outils de composition : typographie, filets, graphiques et liens du sommaire. */
final class AtelierPdf {
    static final float LARGEUR = 595.28f;
    static final float HAUTEUR = 841.89f;
    static final float MARGE = 48;
    static final float DROITE = LARGEUR - MARGE;
    static final float CONTENU = DROITE - MARGE;
    static final BaseColor ENCRE = couleur("#283B39");
    static final BaseColor SECONDAIRE = couleur("#69716C");
    static final BaseColor VERT = couleur("#3C6D5C");
    static final BaseColor SAUGE = couleur("#AAC4B4");
    static final BaseColor GRIS = couleur("#D8DCD7");
    static final BaseColor BORDURE = couleur("#D6DBD5");
    static final BaseColor PAPIER = couleur("#FFFEFB");
    static final String[] DESTINATIONS = {"synthese", "villes", "repartition", "reperes", "annuaire"};

    final PdfWriter writer;
    final PdfContentByte canvas;
    final BaseFont regular;
    final BaseFont bold;
    private final BaseFont serif;

    AtelierPdf(PdfWriter writer) throws IOException, DocumentException {
        this.writer = writer;
        this.canvas = writer.getDirectContent();
        // Les polices sont embarquées pour garder le même rendu sur chaque poste.
        regular = police("Lato-Regular.ttf");
        bold = police("Lato-Bold.ttf");
        serif = police("CrimsonText-Regular.ttf");
    }

    private BaseFont police(String nom) throws IOException, DocumentException {
        try (InputStream flux = AtelierPdf.class.getResourceAsStream("/fonts/" + nom)) {
            if (flux == null) {
                throw new IOException("Police PDF absente : " + nom);
            }
            return BaseFont.createFont(nom, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, flux.readAllBytes(), null);
        }
    }

    void fond(boolean couverture) {
        PdfContentByte dessous = writer.getDirectContentUnder();
        dessous.saveState();
        dessous.setColorFill(PAPIER);
        dessous.rectangle(0, 0, LARGEUR, HAUTEUR);
        dessous.fill();
        if (couverture) {
            dessous.paintShading(PdfShading.simpleAxial(writer, 0, HAUTEUR, LARGEUR, 0,
                    couleur("#F5FAF8"), couleur("#F6F2FB")));
        } else {
            dessous.paintShading(PdfShading.simpleAxial(writer, 0, HAUTEUR, 0, 535,
                    couleur("#F2F5F8"), PAPIER));
        }
        halo(dessous, 536, 726, couverture ? 228 : 172, couleur("#DAD0EF"), couverture ? .055f : .025f);
        halo(dessous, 40, couverture ? 539 : 762, couverture ? 223 : 150,
                couleur("#CAE6D9"), couverture ? .065f : .018f);
        if (couverture) {
            halo(dessous, 470, 130, 185, couleur("#F0D8C8"), .05f);
        }
        dessous.restoreState();
    }

    private void halo(PdfContentByte fond, float x, float y, float rayon, BaseColor teinte, float opacite) {
        fond.saveState();
        PdfGState etat = new PdfGState();
        etat.setFillOpacity(opacite);
        fond.setGState(etat);
        fond.setColorFill(teinte);
        for (int index = 8; index >= 0; index--) {
            fond.circle(x, y, rayon * (.65f + index * .045f));
            fond.fill();
        }
        fond.restoreState();
    }

    void verreCouverture() {
        // Une seule surface de verre porte le titre et la présentation, comme une jaquette.
        canvas.saveState();
        PdfGState ombre = new PdfGState();
        ombre.setFillOpacity(.025f);
        canvas.setGState(ombre);
        canvas.setColorFill(ENCRE);
        canvas.roundRectangle(31, 443, LARGEUR - 60, 297, 24);
        canvas.fill();
        PdfGState verre = new PdfGState();
        verre.setFillOpacity(.52f);
        verre.setStrokeOpacity(.9f);
        canvas.setGState(verre);
        canvas.setColorFill(BaseColor.WHITE);
        canvas.setColorStroke(BaseColor.WHITE);
        canvas.setLineWidth(1);
        canvas.roundRectangle(30, 447, LARGEUR - 60, 297, 24);
        canvas.fillStroke();
        canvas.restoreState();
    }

    void entete(String departement, String section) {
        fond(false);
        texte(abreger(departement, 350, 9, false), MARGE, 791, 9, false, SECONDAIRE);
        droite(section, DROITE, 791, 8, true, SECONDAIRE);
        trait(MARGE, 778, DROITE, 778, BORDURE);
    }

    void rectangle(float x, float y, float largeur, float hauteur, BaseColor couleur) {
        if (largeur <= 0 || hauteur <= 0) {
            return;
        }
        canvas.saveState();
        canvas.setColorFill(couleur);
        canvas.rectangle(x, y, largeur, hauteur);
        canvas.fill();
        canvas.restoreState();
    }

    void trait(float x1, float y1, float x2, float y2, BaseColor couleur) {
        canvas.saveState();
        canvas.setColorStroke(couleur);
        canvas.setLineWidth(.55f);
        canvas.moveTo(x1, y1);
        canvas.lineTo(x2, y2);
        canvas.stroke();
        canvas.restoreState();
    }

    void texte(String valeur, float x, float y, float taille, boolean gras, BaseColor couleur) {
        aligner(valeur, x, y, taille, gras ? bold : regular, couleur, Element.ALIGN_LEFT);
    }

    void droite(String valeur, float x, float y, float taille, boolean gras, BaseColor couleur) {
        aligner(valeur, x, y, taille, gras ? bold : regular, couleur, Element.ALIGN_RIGHT);
    }

    private void aligner(String valeur, float x, float y, float taille, BaseFont police,
                         BaseColor couleur, int alignement) {
        ColumnText.showTextAligned(canvas, alignement,
                new Phrase(valeur, new Font(police, taille, Font.NORMAL, couleur)), x, y, 0);
    }

    float titre(String valeur, float x, float y, float largeur, float taille, float interligne, BaseColor couleur) {
        for (String ligne : lignes(valeur, largeur, taille, serif)) {
            aligner(ligne, x, y, taille, serif, couleur, Element.ALIGN_LEFT);
            y -= interligne;
        }
        return y;
    }

    int lignesTitre(String valeur, float largeur, float taille) {
        return lignes(valeur, largeur, taille, serif).size();
    }

    float paragraphe(String valeur, float x, float y, float largeur, float taille, float interligne,
                     boolean gras, BaseColor couleur) {
        for (String ligne : lignes(valeur, largeur, taille, gras)) {
            texte(ligne, x, y, taille, gras, couleur);
            y -= interligne;
        }
        return y;
    }

    List<String> lignes(String valeur, float largeur, float taille, boolean gras) {
        return lignes(valeur, largeur, taille, gras ? bold : regular);
    }

    private List<String> lignes(String valeur, float largeur, float taille, BaseFont police) {
        List<String> resultat = new ArrayList<>();
        String courante = "";
        // Coupe aussi un mot très long pour qu'aucun nom ne sorte de la colonne.
        String texte = valeur.trim().replaceAll("(?<=\\d) (?=\\d{3}(?:\\D|$)|%)", "\u00A0");
        for (String mot : texte.split("\\s+")) {
            String essai = courante.isEmpty() ? mot : courante + " " + mot;
            if (police.getWidthPoint(essai, taille) <= largeur) {
                courante = essai;
                continue;
            }
            if (!courante.isEmpty()) {
                resultat.add(courante);
                courante = "";
            }
            for (int point : mot.codePoints().toArray()) {
                String lettre = new String(Character.toChars(point));
                if (!courante.isEmpty() && police.getWidthPoint(courante + lettre, taille) > largeur) {
                    resultat.add(courante);
                    courante = "";
                }
                courante += lettre;
            }
        }
        if (!courante.isEmpty()) {
            resultat.add(courante);
        }
        // Rétablit les espaces ordinaires après avoir gardé les nombres groupés pendant la mise en page.
        return resultat.isEmpty() ? List.of("")
                : resultat.stream().map(ligne -> ligne.replace('\u00A0', ' ')).toList();
    }

    String abreger(String valeur, float largeur, float taille, boolean gras) {
        BaseFont police = gras ? bold : regular;
        if (police.getWidthPoint(valeur, taille) <= largeur) {
            return valeur;
        }
        String court = valeur;
        while (!court.isEmpty() && police.getWidthPoint(court + "…", taille) > largeur) {
            court = court.substring(0, court.offsetByCodePoints(court.length(), -1));
        }
        return court + "…";
    }

    void lien(float x, float y, float largeur, float hauteur, String destination) {
        PdfAnnotation lien = PdfAnnotation.createLink(writer, new Rectangle(x, y, x + largeur, y + hauteur),
                PdfAnnotation.HIGHLIGHT_INVERT, PdfAction.gotoLocalPage(destination, false));
        lien.setBorder(new PdfBorderArray(0, 0, 0));
        writer.addAnnotation(lien);
    }

    void destination(String nom) {
        canvas.localDestination(nom, new PdfDestination(PdfDestination.FIT));
    }

    static String entier(long nombre) {
        return NumberFormat.getIntegerInstance(Locale.FRANCE).format(nombre).replace('\u202F', ' ').replace('\u00A0', ' ');
    }

    static String pourcent(double nombre) { return String.format(Locale.FRANCE, "%.1f %%", nombre); }
    static BaseColor couleur(String hex) {
        return new BaseColor(Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16));
    }
}
