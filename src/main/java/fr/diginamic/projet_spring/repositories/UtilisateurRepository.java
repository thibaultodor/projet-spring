package fr.diginamic.projet_spring.repositories;

import fr.diginamic.projet_spring.entites.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

/** Recherche les comptes enregistrés en base. */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    Utilisateur findByUsername(String username);
}
