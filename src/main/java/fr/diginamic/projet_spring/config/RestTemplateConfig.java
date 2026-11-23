package fr.diginamic.projet_spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/** Fournit le client HTTP pour appeler l'API externe. */
@Configuration
public class RestTemplateConfig {

    /** @return un client avec des délais d'attente limités. */
    @Bean
    public RestTemplate restTemplate() {
        // Évite de bloquer indéfiniment le démarrage si l'API ne répond pas.
        SimpleClientHttpRequestFactory requetes = new SimpleClientHttpRequestFactory();
        requetes.setConnectTimeout(Duration.ofSeconds(5));
        requetes.setReadTimeout(Duration.ofSeconds(10));
        return new RestTemplate(requetes);
    }
}
