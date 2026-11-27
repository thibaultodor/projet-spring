package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.controleurs.interfaces.IHelloControleur;
import fr.diginamic.projet_spring.services.interfaces.IHelloService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Place le résultat des méthodes directement dans la réponse HTTP.
@RestController
@RequestMapping("/hello")
public class HelloControleur implements IHelloControleur {

    // Le contrôleur utilise un contrat. Spring fournit la classe qui le réalise.
    private final IHelloService helloService;

    // Spring fournit le service au constructeur : c'est l'injection de dépendance.
    public HelloControleur(IHelloService helloService) {
        this.helloService = helloService;
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping
    public String direHello() {
        return helloService.salutations();
    }
}
