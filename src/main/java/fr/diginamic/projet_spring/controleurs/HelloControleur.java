package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.services.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Place le résultat des méthodes directement dans la réponse HTTP.
@RestController
@RequestMapping("/hello")
public class HelloControleur {

    private final HelloService helloService;

    // Spring fournit le service au constructeur : c'est l'injection de dépendance.
    public HelloControleur(HelloService helloService) {
        this.helloService = helloService;
    }

    // Répond à GET /hello avec le message fourni par le service.
    @GetMapping
    public String direHello() {
        return helloService.salutations();
    }
}
