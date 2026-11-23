# Projet Spring — API de recensement

## Documentation Swagger

Après avoir rechargé les dépendances Maven et démarré l'application depuis IntelliJ :

- [Swagger UI](http://localhost:8080/swagger-ui.html) affiche les routes, leurs paramètres et les exemples de DTO.
- [OpenAPI JSON](http://localhost:8080/v3/api-docs) fournit la description de l'API au format OpenAPI 3.0.
- [OpenAPI YAML](http://localhost:8080/v3/api-docs.yaml) fournit la même description en YAML.

Dans Swagger, ouvrir une route puis cliquer sur **Try it out**, remplir les valeurs et cliquer sur **Execute**.
Les appels POST, PUT et DELETE modifient les données de la base, comme les requêtes HTTPie.

`SwaggerConfig` définit le titre et le contact. `@Operation` décrit une route, `@Parameter` ses paramètres,
`@ApiResponses` ses réponses HTTP et `@Schema` les données échangées.
Les erreurs de validation sont renvoyées par champ en JSON ; les erreurs métier sont renvoyées en texte.

Les scénarios HTTPie sont dans [httpie](httpie/), et les scripts de données dans [database](database/).
