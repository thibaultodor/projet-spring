package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.dto.LoginRequest;
import fr.diginamic.projet_spring.securite.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Vérifie les identifiants et renvoie un JWT. */
@RestController
public class LoginController {

    private final AuthenticationConfiguration config;
    private final JwtUtil jwtUtil;

    public LoginController(AuthenticationConfiguration config, JwtUtil jwtUtil) {
        this.config = config;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(value = "/login", produces = MediaType.TEXT_PLAIN_VALUE)
    public String login(@RequestBody LoginRequest req) throws Exception {
        // Spring retrouve le compte avec UserDetailsService et vérifie son mot de passe BCrypt.
        config.getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        // Le client devra renvoyer ce token dans le header Authorization: Bearer <JWT>.
        return jwtUtil.generateToken(req.getUsername());
    }
}
