package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.entites.Role;
import fr.diginamic.projet_spring.entites.Utilisateur;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Retrouve les comptes conservés en mémoire. */
@Service
public class MemoireService implements UserDetailsService {

    private final Map<String, Utilisateur> utilisateurs = new HashMap<>();

    public MemoireService(PasswordEncoder encoder) {
        // Crée les deux comptes au démarrage. Seuls les mots de passe hachés sont stockés.
        utilisateurs.put("user", new Utilisateur("user", encoder.encode("user1234"),
                List.of(new Role("ROLE_USER"))));
        utilisateurs.put("admin", new Utilisateur("admin", encoder.encode("admin1234"),
                List.of(new Role("ROLE_ADMIN"))));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Le nom sert de clé dans la Map. Spring vérifiera ensuite le mot de passe.
        Utilisateur utilisateur = utilisateurs.get(username);
        if (utilisateur == null) {
            throw new UsernameNotFoundException("Utilisateur inconnu.");
        }
        return utilisateur;
    }
}
