package fr.diginamic.projet_spring.initialisation;

import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.interfaces.IDepartementService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Déclenche l'initialisation après la création des composants Spring. */
@Component
public class InitialisationDonnees implements CommandLineRunner {

    private final IDepartementService departementService;

    public InitialisationDonnees(IDepartementService departementService) {
        this.departementService = departementService;
    }

    @Override
    public void run(String... args) throws ExceptionMetier {
        // Le service quitte immédiatement la méthode si application.init vaut false.
        departementService.initData();
    }
}
