package fr.diginamic.projet_spring.entites;

import org.springframework.security.core.GrantedAuthority;

/** Représente un rôle reconnu par Spring Security. */
public class Role implements GrantedAuthority {

    private String nom;

    public Role(String nom) {
        this.nom = nom;
    }

    @Override
    public String getAuthority() {
        return nom;
    }
}
