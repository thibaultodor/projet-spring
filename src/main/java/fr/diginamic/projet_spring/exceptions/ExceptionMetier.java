package fr.diginamic.projet_spring.exceptions;

import org.springframework.http.HttpStatus;

/** Erreur fonctionnelle commune aux villes, départements et régions. */
public class ExceptionMetier extends Exception {

    private final HttpStatus statut;

    public ExceptionMetier(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public ExceptionMetier(String message, HttpStatus statut) {
        super(message);
        this.statut = statut;
    }

    public HttpStatus getStatut() {
        return statut;
    }
}
