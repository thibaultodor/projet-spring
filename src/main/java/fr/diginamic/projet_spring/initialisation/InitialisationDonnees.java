package fr.diginamic.projet_spring.initialisation;

import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.DepartementService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Déclenche l'initialisation après la création des composants Spring. */
@Component
public class InitialisationDonnees implements CommandLineRunner {

    private final DepartementService departementService;

    public InitialisationDonnees(DepartementService departementService) {
        this.departementService = departementService;
    }

    @Override
    public void run(String... args) throws ExceptionMetier {
        // Le service quitte immédiatement la méthode si application.init vaut false.
        departementService.initData();
    }
}
