package fr.diginamic.projet_spring.services;

import org.springframework.stereotype.Service;

// Déclare un bean : Spring crée et gère cette instance.
@Service
public class HelloService {

    public String salutations() {
        return "Je suis la classe de service et je vous dis Bonjour";
    }
}
