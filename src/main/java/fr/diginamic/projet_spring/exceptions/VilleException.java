package fr.diginamic.projet_spring.exceptions;

import org.springframework.http.HttpStatus;

// Signale une erreur fonctionnelle : données invalides ou ville introuvable.
// Elle hérite d'Exception : les méthodes concernées déclarent throws VilleException.
public class VilleException extends Exception {

    private final HttpStatus statut;

    // Une donnée invalide renvoie HTTP 400 par défaut.
    public VilleException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public VilleException(String message, HttpStatus statut) {
        super(message);
        this.statut = statut;
    }

    public HttpStatus getStatut() {
        return statut;
    }
}
