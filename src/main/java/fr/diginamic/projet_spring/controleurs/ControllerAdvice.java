package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/** Transforme les erreurs métier et de validation en réponses HTTP lisibles. */
@RestControllerAdvice
public class ControllerAdvice {

    /**
     * Transforme une erreur métier en réponse HTTP.
     *
     * @param exception erreur signalée par un service
     * @return son message et le code HTTP prévu
     */
    @ExceptionHandler(ExceptionMetier.class)
    public ResponseEntity<String> traiterExceptionMetier(ExceptionMetier exception) {
        return ResponseEntity.status(exception.getStatut()).body(exception.getMessage());
    }

    /**
     * Rassemble les erreurs de validation par champ.
     *
     * @param exception contraintes refusées par Bean Validation
     * @return les messages par champ avec HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> traiterValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        for (FieldError erreur : exception.getBindingResult().getFieldErrors()) {
            erreurs.put(erreur.getField(), erreur.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(erreurs);
    }

    /**
     * Explique une contrainte refusée par la base sans exposer le SQL.
     *
     * @param exception contrainte refusée par la base, par exemple une clé étrangère
     * @return une erreur HTTP 409 sans exposer les détails SQL
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> traiterContrainteBase(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).body("La modification ne respecte pas les contraintes de la base");
    }
}
