# Projet Spring - API de recensement

API Spring Boot de gestion des villes et des départements, avec exports CSV et PDF.

## Démarrage

Le projet utilise le JDK 27 et MariaDB, disponible avec XAMPP.

1. Démarrer MariaDB dans XAMPP.
2. Dans un client SQL, exécuter [creation-recensement.sql](database/creation-recensement.sql),
   puis [le script d'import des villes](database/tp-spring-10-recensement.sql)
   et [les comptes de démonstration](database/utilisateurs.sql).
3. Recharger les dépendances Maven et lancer `ProjetSpringApplication` depuis IntelliJ.

La connexion est configurée dans `src/main/resources/application.properties` : base `recensement`,
port `3306`, utilisateur `root`, sans mot de passe. Les variables `DB_URL`, `DB_USERNAME` et
`DB_PASSWORD` permettent de modifier ces valeurs.

Les scripts SQL s'exécutent manuellement. L'import met à jour les villes existantes sans créer de doublons.
Pour une ancienne base où la région est obligatoire, le script
[region-facultative](database/tp09-region-facultative.sql) permet d'adapter la table des départements.

Pour une base existante, exécuter aussi [suivi-modifications.sql](database/suivi-modifications.sql)
pour ajouter les colonnes de suivi des villes et départements.

## Utilisation

- [Swagger UI](http://localhost:8080/swagger-ui.html) permet de consulter et d'essayer les routes.
- La description [OpenAPI](http://localhost:8080/v3/api-docs) est disponible en JSON.
- Pour HTTPie Desktop, importer [projet-spring.httpie.json](httpie/projet-spring.httpie.json).
  Les variables, dont `baseUrl`, se trouvent dans l'environnement **Defaults**.
  Pour réimporter dans un espace existant, décocher les environnements, puis actualiser les quatre
  variables de connexion dans **Defaults**.

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

Les comptes et les rôles sont enregistrés en base, dans `utilisateur`, `role` et `utilisateur_role`.
`JpaUserDetailsService` les retrouve avec `UtilisateurRepository`. Les mots de passe sont hachés avec BCrypt.

| Compte | Mot de passe | Rôle | Accès |
| --- | --- | --- | --- |
| `user` | `user1234` | `USER` | GET |
| `admin` | `admin1234` | `ADMIN` | GET, POST, PUT et DELETE |

Le script `database/utilisateurs.sql` crée ces deux comptes et peut être relancé sans doublon.
Toutes les routes demandent une connexion. Les contrôleurs utilisent `@Secured` : USER ou ADMIN
pour les GET, ADMIN pour les POST, PUT et DELETE. Une absence de connexion ou un mauvais mot de passe
renvoie 401 ; un rôle insuffisant renvoie 403.

Dans HTTPie, les variables `username` et `password` servent aux GET, `adminUsername` et `adminPassword`
aux autres méthodes. Elles sont renseignées dans **Defaults**. La collection **10 - Sécurité par rôles**
contient les cas autorisés et les refus attendus.

## Suivi des modifications

Les villes et départements gardent `userMaj` et `dateMaj` après chaque ajout ou modification.
Ces champs sont visibles dans les réponses JSON et remplis par le serveur avec le compte connecté.
Ils restent vides pour les anciennes données qui n'ont pas encore été modifiées.

Les services tracent les ajouts, modifications et suppressions dans `logs/projet-spring.log` avec
l'utilisateur, l'identifiant et le nom de la donnée. L'actualisation automatique des départements
est signée `initialisation` lorsqu'elle se fait au démarrage, sans compte connecté.

La collection HTTPie **11 - Suivi des modifications** permet de vérifier les signatures. Ses champs
`userMaj` et `dateMaj` envoyés dans le corps sont volontairement ignorés par le serveur.
