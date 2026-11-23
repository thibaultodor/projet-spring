package fr.diginamic.projet_spring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Définit les informations affichées en haut de la documentation Swagger. */
@Configuration
public class SwaggerConfig implements OpenApiCustomizer {

    /** @return la présentation de l'API utilisée par springdoc. */
    @Bean
    public OpenAPI customOpenAPI() {
        // Spring garde ce bean et springdoc complète les routes à partir des contrôleurs.
        return new OpenAPI().info(new Info()
                .title("API de recensement")
                .version("1.0")
                .description("API pour consulter et gérer les villes et les départements français. "
                        + "Elle permet de rechercher les villes par nom, population et département.")
                .contact(new Contact().name("Thibault Odor")
                        .url("https://github.com/thibaultodor/projet-spring")));
    }

    /** Garde max facultatif pour les deux recherches qui partagent la même URL. */
    @Override
    public void customise(OpenAPI openApi) {
        // OpenAPI décrit une seule opération par verbe et URL, même si Java utilise deux méthodes.
        PathItem route = openApi.getPaths().get("/departements/{id}/villes/population");
        if (route == null || route.getGet() == null || route.getGet().getParameters() == null) {
            return;
        }
        for (Parameter parametre : route.getGet().getParameters()) {
            if ("max".equals(parametre.getName()) && "query".equals(parametre.getIn())) {
                parametre.setRequired(false);
            }
        }
    }
}
