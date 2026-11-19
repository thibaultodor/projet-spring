package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.entites.Ville;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/villes")
public class VilleControleur {

    @GetMapping
    public List<Ville> getVilles() {
        return List.of(
                new Ville("Paris", 2_100_000),
                new Ville("Lyon", 520_000),
                new Ville("Marseille", 870_000),
                new Ville("Montpellier", 300_000)
        );
    }
}
