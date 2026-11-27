package fr.diginamic.projet_spring.securite;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Crée les JWT et vérifie leur signature et leur expiration. */
@Component
public class JwtUtil {

    // Cette clé sert à signer les tokens. En production, elle doit rester hors du code.
    private static final String SECRET = "secret-secret-secret-secret-secret-secret";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(String username) {
        // Le token contient le nom du compte et reste valable pendant 15 minutes.
        return Jwts.builder().subject(username).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        // Le parseur refuse aussi un token expiré ou dont la signature a changé.
        Claims claims = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
