package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.entites.Ville;
import fr.diginamic.projet_spring.exceptions.VilleException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Expose les routes /villes. Les objets Ville renvoyés sont convertis en JSON.
@RestController
@RequestMapping("/villes")
public class VilleControleur {

    // Partage les villes en mémoire entre les requêtes.
    // Les données sont perdues au redémarrage de l'application.
    private final List<Ville> villes = new ArrayList<>();
    // Garde le prochain identifiant à attribuer, même après une suppression.
    private int prochainId = 1;

    // Spring appelle cette méthode une fois, après la création du contrôleur.
    @PostConstruct
    public void initData() {
        // Crée les villes de départ.
        // prochainId++ utilise l'id actuel, puis augmente le compteur.
        villes.add(new Ville(prochainId++, "Paris", 2_100_000));
        villes.add(new Ville(prochainId++, "Lyon", 520_000));
        villes.add(new Ville(prochainId++, "Marseille", 870_000));
        villes.add(new Ville(prochainId++, "Montpellier", 300_000));
    }

    // Renvoie toutes les villes pour GET /villes.
    @GetMapping
    public List<Ville> getVilles() {
        return villes;
    }

    // Recherche une ville grâce à l'id reçu dans l'URL, par exemple /villes/2.
    // Le gestionnaire d'exceptions s'occupe de la réponse si elle est introuvable.
    @GetMapping("/{id}")
    public ResponseEntity<Ville> getVille(@PathVariable int id) throws VilleException {
        return ResponseEntity.ok(trouverVille(id));
    }

    // @RequestBody transforme le JSON reçu en objet Ville.
    // @Valid vérifie ses contraintes avant d'exécuter la méthode.
    @PostMapping
    public ResponseEntity<String> ajouterVille(@Valid @RequestBody Ville ville) throws VilleException {
        // Refuse un nom déjà présent, même si les majuscules changent.
        for (Ville villeExistante : villes) {
            if (villeExistante.getNom().equalsIgnoreCase(ville.getNom())) {
                throw new VilleException("La ville \"" + ville.getNom() + "\" existe déjà");
            }
        }

        // Attribue un id côté serveur, même si la requête en contient déjà un.
        ville.setId(prochainId++);
        // Ajoute la ville à la liste partagée pour les prochains GET.
        villes.add(ville);
        return ResponseEntity.ok("Ville insérée avec succès (id : " + ville.getId() + ")");
    }

    // Modifie la ville désignée par l'id de l'URL avec les données JSON reçues.
    // @Valid applique les mêmes contraintes que pour l'ajout.
    @PutMapping("/{id}")
    public ResponseEntity<String> modifierVille(@PathVariable int id, @Valid @RequestBody Ville nouvellesDonnees)
            throws VilleException {
        // Retrouve la ville à modifier une fois les données validées.
        Ville ville = trouverVille(id);

        // Vérifie les doublons en excluant la ville en cours de modification.
        for (Ville villeExistante : villes) {
            if (villeExistante.getId() != id
                    && villeExistante.getNom().equalsIgnoreCase(nouvellesDonnees.getNom())) {
                throw new VilleException("La ville \"" + nouvellesDonnees.getNom() + "\" existe déjà");
            }
        }

        // Met à jour les données en conservant l'identifiant existant.
        ville.setNom(nouvellesDonnees.getNom());
        ville.setPopulation(nouvellesDonnees.getPopulation());
        return ResponseEntity.ok("Ville modifiée avec succès (id : " + ville.getId() + ")");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerVille(@PathVariable int id) throws VilleException {
        // Vérifie que la ville existe avant de la supprimer.
        Ville ville = trouverVille(id);

        // Retire la ville de la liste sans changer le compteur d'identifiants.
        villes.remove(ville);
        return ResponseEntity.ok("Ville supprimée avec succès (id : " + ville.getId() + ")");
    }

    // @RequestParam récupère la valeur placée après ?nom= dans l'URL.
    @GetMapping("/recherche/nom")
    public List<Ville> rechercherParNom(@RequestParam String nom) throws VilleException {
        List<Ville> resultats = new ArrayList<>();
        // Compare les débuts des noms sans tenir compte des majuscules.
        String prefixe = nom.toLowerCase(Locale.ROOT);
        for (Ville ville : villes) {
            if (ville.getNom().toLowerCase(Locale.ROOT).startsWith(prefixe)) {
                resultats.add(ville);
            }
        }

        // Signale l'absence de résultat avec le préfixe réellement demandé.
        if (resultats.isEmpty()) {
            throw new VilleException("Aucune ville dont le nom commence par " + nom + " n’a été trouvée",
                    HttpStatus.NOT_FOUND);
        }
        return resultats;
    }

    @GetMapping("/recherche/population")
    public List<Ville> rechercherParPopulationMin(@RequestParam int min) throws VilleException {
        List<Ville> resultats = new ArrayList<>();
        // Garde uniquement les populations strictement supérieures à min.
        for (Ville ville : villes) {
            if (ville.getPopulation() > min) {
                resultats.add(ville);
            }
        }

        if (resultats.isEmpty()) {
            throw new VilleException("Aucune ville n’a une population supérieure à " + min,
                    HttpStatus.NOT_FOUND);
        }
        return resultats;
    }

    @GetMapping("/recherche/population/entre")
    public List<Ville> rechercherParPopulationEntre(@RequestParam int min, @RequestParam int max)
            throws VilleException {
        // Refuse un intervalle impossible avant de parcourir les villes.
        if (min >= max) {
            throw new VilleException("La borne min doit être inférieure à la borne max");
        }

        List<Ville> resultats = new ArrayList<>();
        // Les deux bornes sont exclues : min < population < max.
        for (Ville ville : villes) {
            if (ville.getPopulation() > min && ville.getPopulation() < max) {
                resultats.add(ville);
            }
        }

        if (resultats.isEmpty()) {
            throw new VilleException("Aucune ville n’a une population comprise entre " + min + " et " + max,
                    HttpStatus.NOT_FOUND);
        }
        return resultats;
    }

    private Ville trouverVille(int id) throws VilleException {
        // Parcourt la liste pour retrouver l'identifiant demandé.
        for (Ville ville : villes) {
            if (ville.getId() == id) {
                return ville;
            }
        }

        // L'exception remonte au gestionnaire, qui construit la réponse HTTP 404.
        throw new VilleException("Ville introuvable", HttpStatus.NOT_FOUND);
    }
}
