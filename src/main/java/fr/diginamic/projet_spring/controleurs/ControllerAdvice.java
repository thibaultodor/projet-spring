package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.exceptions.VilleException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Centralise les erreurs des contrôleurs et écrit leur message dans la réponse HTTP.
@RestControllerAdvice
public class ControllerAdvice {

    // Spring appelle cette méthode quand une VilleException remonte d'un contrôleur.
    @ExceptionHandler(VilleException.class)
    public ResponseEntity<String> traiterVilleException(VilleException exception) {
        return ResponseEntity.status(exception.getStatut()).body(exception.getMessage());
    }
}
