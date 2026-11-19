package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.entites.Ville;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/villes")
public class VilleControleur {

    private final List<Ville> villes = new ArrayList<>();

    @PostConstruct
    public void initData() {
        villes.add(new Ville("Paris", 2_100_000));
        villes.add(new Ville("Lyon", 520_000));
        villes.add(new Ville("Marseille", 870_000));
        villes.add(new Ville("Montpellier", 300_000));
    }

    @GetMapping
    public List<Ville> getVilles() {
        return villes;
    }

    @PostMapping
    public ResponseEntity<String> ajouterVille(@RequestBody Ville ville) {
        for (Ville villeExistante : villes) {
            if (villeExistante.getNom().equalsIgnoreCase(ville.getNom())) {
                return ResponseEntity.badRequest().body("La ville \""+ville.getNom()+"\" existe déjà");
            }
        }

        villes.add(ville);
        return ResponseEntity.ok("Ville insérée avec succès");
    }
}
