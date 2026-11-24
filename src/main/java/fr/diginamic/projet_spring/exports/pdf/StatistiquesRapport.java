package fr.diginamic.projet_spring.exports.pdf;

import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement.ReferenceDepartement;
import fr.diginamic.projet_spring.exports.pdf.DonneesRapportDepartement.VilleChiffres;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Calcule les indicateurs uniquement à partir des données effectivement enregistrées. */
public final class StatistiquesRapport {
    private final DonneesRapportDepartement donnees;
    private final long population;
    private final List<ReferenceDepartement> classement;

    public StatistiquesRapport(DonneesRapportDepartement donnees) {
        this.donnees = donnees;
        // long évite le dépassement d'un int lorsque plusieurs grandes populations sont additionnées.
        this.population = donnees.villes().stream().mapToLong(VilleChiffres::population).sum();
        this.classement = donnees.references().stream().filter(reference -> reference.nombreVilles() > 0)
                .sorted(Comparator.comparingLong(ReferenceDepartement::population).reversed()
                        .thenComparing(ReferenceDepartement::code)).toList();
    }

    public long population() { return population; }
    public int nombreVilles() { return donnees.villes().size(); }
    public double moyenne() { return nombreVilles() == 0 ? 0 : (double) population / nombreVilles(); }

    public double mediane() {
        int nombre = nombreVilles();
        if (nombre == 0) {
            return 0;
        }
        List<VilleChiffres> villes = donnees.villes();
        if (nombre % 2 == 1) {
            return villes.get(nombre / 2).population();
        }
        return ((long) villes.get(nombre / 2 - 1).population() + villes.get(nombre / 2).population()) / 2.0;
    }

    public long populationTop(int nombre) {
        return donnees.villes().stream().limit(nombre).mapToLong(VilleChiffres::population).sum();
    }

    public double partTop(int nombre) { return pourcentage(populationTop(nombre), population); }
    public List<ReferenceDepartement> classement() { return classement; }
    public long populationBase() { return classement.stream().mapToLong(ReferenceDepartement::population).sum(); }

    public List<ReferenceDepartement> region() {
        if (donnees.idRegion() == null) {
            return List.of();
        }
        return classement.stream().filter(reference -> Objects.equals(reference.idRegion(), donnees.idRegion())).toList();
    }

    public int rang(List<ReferenceDepartement> references) {
        for (int index = 0; index < references.size(); index++) {
            if (references.get(index).id() == donnees.id()) {
                return index + 1;
            }
        }
        return 0;
    }

    public List<ReferenceDepartement> reperes() {
        List<ReferenceDepartement> resultats = new ArrayList<>(classement.stream().limit(5).toList());
        // Garde le département exporté visible même s'il n'appartient pas aux cinq premiers.
        donnees.references().stream().filter(reference -> reference.id() == donnees.id())
                .filter(reference -> resultats.stream().noneMatch(deja -> deja.id() == reference.id()))
                .findFirst().ifPresent(resultats::add);
        return List.copyOf(resultats);
    }

    public List<TranchePopulation> tranches() {
        long[] bornes = {0, 500, 2000, 5000, 10000, 50000, Long.MAX_VALUE};
        String[] etiquettes = {"Moins de 500", "500 à 1 999", "2 000 à 4 999",
                "5 000 à 9 999", "10 000 à 49 999", "50 000 et plus"};
        List<TranchePopulation> resultat = new ArrayList<>();
        for (int index = 0; index < etiquettes.length; index++) {
            long min = bornes[index];
            long max = bornes[index + 1];
            long nombre = donnees.villes().stream()
                    .filter(ville -> ville.population() >= min && ville.population() < max).count();
            resultat.add(new TranchePopulation(etiquettes[index], nombre));
        }
        return List.copyOf(resultat);
    }

    public long petitesVilles() {
        return donnees.villes().stream().filter(ville -> ville.population() < 2000).count();
    }

    public long populationPetitesVilles() {
        return donnees.villes().stream().filter(ville -> ville.population() < 2000)
                .mapToLong(VilleChiffres::population).sum();
    }

    public static double pourcentage(long valeur, long total) {
        return total <= 0 ? 0 : 100.0 * valeur / total;
    }

    public record TranchePopulation(String etiquette, long nombre) { }
}
