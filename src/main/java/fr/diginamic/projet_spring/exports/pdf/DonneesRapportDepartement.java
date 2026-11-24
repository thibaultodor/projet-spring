package fr.diginamic.projet_spring.exports.pdf;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

/** Copie immuable des données : le dessinateur du PDF n'a pas besoin d'accéder à JPA. */
public record DonneesRapportDepartement(int id, String code, String nom, Integer idRegion, String region,
                                       ZonedDateTime dateExport, List<VilleChiffres> villes,
                                       List<ReferenceDepartement> references) {
    public DonneesRapportDepartement {
        // L'id départage les populations égales pour garder un ordre stable dans tout le rapport.
        villes = villes.stream().sorted(Comparator.comparingInt(VilleChiffres::population).reversed()
                .thenComparingInt(VilleChiffres::id)).toList();
        references = List.copyOf(references);
    }

    public record VilleChiffres(int id, String nom, int population) { }
    public record ReferenceDepartement(int id, String code, String nom, Integer idRegion,
                                      long population, long nombreVilles) { }
}
