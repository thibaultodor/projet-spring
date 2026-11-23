package fr.diginamic.projet_spring.controleurs.interfaces;

import io.swagger.v3.oas.annotations.Operation;

/** Documentation des salutations. */
public interface IHelloControleur {

    /** Renvoie les salutations fournies par le service. */
    @Operation(summary = "Retourne le message de salutation")
    String direHello();
}
