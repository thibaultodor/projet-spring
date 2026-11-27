package fr.diginamic.projet_spring.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/** Définit les droits d'accès aux routes. */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    /** Fournit le hachage utilisé pour vérifier les mots de passe. */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Autorise les appels du front local vers l'API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Construit les règles appliquées avant l'appel des contrôleurs. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // HTTP Basic lit l'identifiant et le mot de passe envoyés avec la requête.
        http.httpBasic(Customizer.withDefaults());
        http.cors(Customizer.withDefaults());
        // L'API est appelée sans cookie depuis HTTPie.
        http.csrf(csrf -> csrf.disable());
        // Le navigateur limite les ressources aux origines déclarées dans cet en-tête.
        String csp = "script-src 'self' https://cdn.jsdelivr.net;"
                + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com;"
                + "img-src 'self';"
                + "font-src 'self' https://cdnjs.cloudflare.com";
        http.headers(headers -> headers.contentSecurityPolicy(configCsp -> configCsp.policyDirectives(csp)));
        http.authorizeHttpRequests(auth -> auth
                // Laisse Spring produire les réponses d'erreur après le contrôle initial.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                // Le navigateur vérifie CORS avant d'envoyer la requête authentifiée.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Les contrôleurs précisent ensuite les rôles avec @Secured.
                .anyRequest().authenticated());
        return http.build();
    }
}
