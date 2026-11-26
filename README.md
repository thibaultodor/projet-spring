# Projet Spring - API de recensement

API Spring Boot de gestion des villes et des départements, avec exports CSV et PDF.

## Démarrage

Le projet utilise le JDK 27 et MariaDB, disponible avec XAMPP.

1. Démarrer MariaDB dans XAMPP.
2. Dans un client SQL, exécuter [creation-recensement.sql](database/creation-recensement.sql),
   puis [le script d'import des villes](database/tp-spring-10-recensement.sql).
3. Recharger les dépendances Maven et lancer `ProjetSpringApplication` depuis IntelliJ.

La connexion est configurée dans `src/main/resources/application.properties` : base `recensement`,
port `3306`, utilisateur `root`, sans mot de passe. Les variables `DB_URL`, `DB_USERNAME` et
`DB_PASSWORD` permettent de modifier ces valeurs.

Les scripts SQL s'exécutent manuellement. L'import met à jour les villes existantes sans créer de doublons.
Pour une ancienne base où la région est obligatoire, le script
[region-facultative](database/tp09-region-facultative.sql) permet d'adapter la table des départements.

## Utilisation

- [Swagger UI](http://localhost:8080/swagger-ui.html) permet de consulter et d'essayer les routes.
- La description [OpenAPI](http://localhost:8080/v3/api-docs) est disponible en JSON.
- Pour HTTPie Desktop, importer [projet-spring.httpie.json](httpie/projet-spring.httpie.json).
  Les variables, dont `baseUrl`, se trouvent dans l'environnement **Defaults**.

`GET /villes?page=0&size=20` renvoie une page de villes. Les résultats sont dans `content` et les informations
de pagination dans `page`. La première page porte le numéro `0` ; `size` peut aller de `1` à `100`.

Les routes des départements utilisent leur identifiant en base. L'export PDF utilise leur code,
par exemple `34` pour l'Hérault.

## Exports

- `GET /villes/export/csv?min=100000` exporte les villes de plus de 100000 habitants.
  Le fichier contient le nom, la population, le code et le nom du département.
- `GET /departements/34/export/pdf` produit le rapport de l'Hérault : statistiques, graphiques,
  classement et liste des villes. Un CSV est également joint au PDF.

Les exports contiennent tous les résultats, sans pagination.

## Configuration

Pour actualiser les noms des départements depuis l'API géographique, passer `application.init` à `true`,
démarrer une fois l'application, puis remettre la valeur à `false`.

Les logs sont configurés dans `src/main/resources/logback.xml`. Ils s'affichent dans la console et dans
`logs/projet-spring.log`, avec une rotation des fichiers. `LOG_PATH` permet de changer le dossier de sortie.

Actuator expose `/actuator/health`, `/actuator/info` et `/actuator/metrics` pour consulter l'état de l'application.

## Tests

`DepartementServiceTest` teste la gestion des départements avec Mockito.
`DepartementControleurTest` vérifie les routes avec MockMvc.
Le profil `test` utilise H2 en mémoire et désactive l'initialisation externe.

```powershell
.\mvnw.cmd test
```

Les deux classes peuvent aussi être lancées directement depuis IntelliJ.

## Sécurité

Les GET sur `/villes` et `/villes/**` sont publics. Les autres requêtes demandent une connexion HTTP Basic,
y compris les départements, Swagger, les PDF et Actuator.

Le compte par défaut est `user`. Son mot de passe est généré à chaque démarrage et affiché dans la console
après `Using generated security password`.

Dans HTTPie, renseigner `username` et `password` dans **Defaults**. La collection **10 - Sécurité HTTP Basic**
permet de comparer les appels sans connexion et les appels authentifiés.
