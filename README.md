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

## Exports de fichiers

- `GET /villes/export/csv?min=100000` télécharge toutes les villes dont la population est strictement supérieure à 100000.
  Le CSV contient le nom de la ville, sa population, le code et le nom du département. Il est en UTF-8, avec des virgules comme séparateurs.
- `GET /departements/34/export/pdf` télécharge la fiche de l'Hérault avec toutes ses villes et leurs populations.
  Ici `34` est le **code départemental**, différent de l'identifiant utilisé par les routes CRUD.

Les exports ne sont pas paginés. Un CSV vide garde ses colonnes, et un département sans villes conserve sa fiche PDF.
Les deux routes sont également documentées dans Swagger et dans la collection HTTPie **08 — Exports CSV et PDF**.

## Initialisation des noms des départements

`application.init=false` est la configuration habituelle : aucun appel externe n'est réalisé au démarrage.
Pour actualiser les noms, passer cette propriété à `true`, démarrer l'application une fois, puis la remettre à `false`.

Le `CommandLineRunner` appelle `DepartementService.initData()`. Le service utilise `RestTemplate` pour lire
`https://geo.api.gouv.fr/departements`, retrouve les départements par leur code et met uniquement leur nom à jour.
Les identifiants, les régions, les villes et les codes locaux inconnus de l'API sont conservés.
Une réponse externe invalide est refusée avant les modifications ; une API indisponible produit un message d'erreur explicite.
