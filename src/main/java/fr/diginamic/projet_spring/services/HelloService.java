package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.services.interfaces.IHelloService;

import org.springframework.stereotype.Service;

// Déclare un bean : Spring crée et gère cette instance.
@Service
public class HelloService implements IHelloService {

    @Override
    public String salutations() {
        return "Je suis la classe de service et je vous dis Bonjour";
    }
}
