package fr.diginamic.projet_spring.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Définit les droits d'accès aux routes. */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    /** Fournit le hachage utilisé pour vérifier les mots de passe. */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Construit les règles appliquées avant l'appel des contrôleurs. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // HTTP Basic lit l'identifiant et le mot de passe envoyés avec la requête.
        http.httpBasic(Customizer.withDefaults());
        // L'API est appelée sans cookie depuis HTTPie.
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                // Laisse Spring produire les réponses d'erreur après le contrôle initial.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                // Les contrôleurs précisent ensuite les rôles avec @Secured.
                .anyRequest().authenticated());
        return http.build();
    }
}
