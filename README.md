# Projet Spring - API de recensement

API Spring Boot de gestion des villes et des départements, avec recherche, pagination et exports CSV et PDF.

## Démarrage

Le projet utilise le JDK 27 et MariaDB, disponible avec XAMPP.

1. Démarrer MariaDB dans XAMPP.
2. Dans un client SQL, exécuter [creation-recensement.sql](database/creation-recensement.sql),
   puis [l'import des villes](database/tp-spring-10-recensement.sql)
   et [les comptes de démonstration](database/utilisateurs.sql).
3. Recharger les dépendances Maven et lancer `ProjetSpringApplication` depuis IntelliJ.

La connexion est configurée dans `src/main/resources/application.properties` : base `recensement`,
port `3306`, utilisateur `root`, sans mot de passe. Les variables `DB_URL`, `DB_USERNAME` et
`DB_PASSWORD` permettent de modifier ces valeurs.

Pour une ancienne base, les scripts [region-facultative.sql](database/tp09-region-facultative.sql)
et [suivi-modifications.sql](database/suivi-modifications.sql) permettent respectivement de rendre la région
facultative et d'ajouter les colonnes de suivi.

## Utilisation

- Le [guide HTTPie](httpie/README.md) explique l'import de la collection, la connexion et les scénarios de test.
- Documentation : [OpenAPI JSON](http://localhost:8080/v3/api-docs) et [Swagger UI](http://localhost:8080/swagger-ui.html), accès protégé par JWT.
- Export CSV : `GET /villes/export/csv?min=100000`.
- Rapport PDF d'un département : `GET /departements/34/export/pdf`.

## Configuration

Pour actualiser les noms des départements depuis l'API géographique, passer `application.init` à `true`,
démarrer une fois l'application, puis remettre la valeur à `false`.

Logback est configuré dans `src/main/resources/logback.xml`. Les logs s'affichent dans la console et dans
`logs/projet-spring.log`, avec une rotation des fichiers. `LOG_PATH` permet de changer le dossier de sortie.
Les écritures sont tracées ; `userMaj` et `dateMaj` gardent le dernier utilisateur et la date de modification.

Actuator expose `/actuator/health`, `/actuator/info` et `/actuator/metrics`.

## Tests

`DepartementServiceTest` teste la gestion des départements avec Mockito.
`DepartementControleurTest` vérifie les routes avec MockMvc.
Le profil `test` utilise H2 en mémoire et désactive l'initialisation externe.

Lancer les deux classes depuis IntelliJ ou avec :

```powershell
.\mvnw.cmd test
```

## Sécurité

L'authentification utilise JWT, avec les comptes en base et les mots de passe hachés avec BCrypt.

| Compte | Mot de passe | Rôle | Accès |
| --- | --- | --- | --- |
| `user` | `user1234` | `USER` | GET |
| `admin` | `admin1234` | `ADMIN` | GET, POST, PUT et DELETE |

`POST /login` retourne un JWT valable 15 minutes. Les autres routes demandent le header
`Authorization: Bearer <JWT>`. Le serveur ne garde pas de session de connexion.
