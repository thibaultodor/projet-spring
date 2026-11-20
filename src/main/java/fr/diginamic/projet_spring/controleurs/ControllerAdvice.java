package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.exceptions.VilleException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Centralise les erreurs des contrôleurs et écrit leur message dans la réponse HTTP.
@RestControllerAdvice
public class ControllerAdvice {

    // Spring appelle cette méthode quand une VilleException remonte d'un contrôleur.
    @ExceptionHandler(VilleException.class)
    public ResponseEntity<String> traiterVilleException(VilleException exception) {
        return ResponseEntity.status(exception.getStatut()).body(exception.getMessage());
    }

    // Intercepte les erreurs détectées par @Valid avant l'exécution du contrôleur.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> traiterValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        // Associe chaque champ invalide à son message pour le renvoyer en JSON.
        for (FieldError erreur : exception.getBindingResult().getFieldErrors()) {
            erreurs.put(erreur.getField(), erreur.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(erreurs);
    }

}
