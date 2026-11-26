package fr.diginamic.projet_spring.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Définit les routes publiques et celles qui demandent une connexion. */
@Configuration
public class SecurityConfig {

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
                .requestMatchers(HttpMethod.GET, "/villes", "/villes/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
