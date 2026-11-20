# Veltra

**Veltra** est une API REST de covoiturage développée avec **Java 25**, **Spring Boot 4.1.2** et **PostgreSQL 16**. Elle permet de gérer les conducteurs, les passagers, les véhicules, les trajets avec arrêts intermédiaires et les réservations.

> Projet backend pédagogique. Le périmètre actuel ne comprend ni authentification ni interface utilisateur.

## Fonctionnalités

- Gestion des conducteurs et passagers (création, consultation, recherche et mise à jour).
- Gestion des véhicules, avec suppression autorisée uniquement lorsqu'ils ne sont liés à aucun trajet.
- Publication de trajets avec des arrêts ordonnés, des horaires et des prix cumulés.
- Recherche de trajets par segment, date de départ, prix maximal et nombre de places, avec pagination.
- Réservation de places sur un segment et calcul du montant total.
- Consultation de l'historique des réservations d'un passager et des réservations d'un trajet.
- Annulation des réservations et des trajets, et clôture des trajets après le départ.
- Validation des données, gestion centralisée des erreurs et journalisation des opérations métier.

## Stack technique

| Composant | Technologie |
| --- | --- |
| Langage | Java 25 |
| Framework | Spring Boot 4.1.2, Spring MVC |
| Persistance | Spring Data JPA / Hibernate |
| Base de données | PostgreSQL 16 |
| Migrations | Flyway |
| Mapping | MapStruct 1.6.3 |
| Validation | Jakarta Bean Validation |
| Tests | JUnit, Mockito, AssertJ (via les starters de test Spring Boot) |
| Conteneurisation | Docker Compose (PostgreSQL) |
| Build | Maven Wrapper |

## Démarrage local

### Prérequis

- JDK **25** configuré pour le projet.
- Docker avec Docker Compose.
- Un terminal PowerShell sous Windows (commandes ci-dessous).

### 1. Cloner le dépôt

```powershell
git clone https://github.com/elmasghatimohamed-cmd/Veltra.git
cd Veltra
```

### 2. Configurer PostgreSQL

Créer le fichier local `.env` à partir de `.env.example`, puis renseigner les valeurs de votre choix. Ne jamais versionner le fichier `.env`.

```powershell
Copy-Item .env.example .env
```

Exemple des clés attendues :

```dotenv
POSTGRES_DB=veltra_db
POSTGRES_USER=veltra_user
POSTGRES_PASSWORD=change-me-locally
```

**Important :** le fichier `application.yaml` lit `POSTGRES_USER` et `POSTGRES_PASSWORD` depuis **l'environnement du processus Java**. Un fichier `.env` utilisé par Docker Compose n'est pas automatiquement chargé par Spring Boot.

Pour une session PowerShell locale, charger les variables :

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$') {
        Set-Item -Path "Env:$($matches[1])" -Value $matches[2]
    }
}
```

### 3. Démarrer la base de données

```powershell
docker compose up -d --wait
```

Le conteneur PostgreSQL écoute sur **127.0.0.1:5433** et utilise un volume Docker pour conserver les données.

### 4. Exécuter l'application

```powershell
.\mvnw.cmd spring-boot:run
```

L'API est disponible par défaut sur **http://localhost:8080**. Flyway applique la migration initiale située dans `src/main/resources/db/migration/` ; Hibernate vérifie la cohérence du schéma via `ddl-auto: validate`.

### 5. Compiler et tester

```powershell
.\mvnw.cmd clean verify
```

Pour construire le JAR et l'exécuter après compilation :

```powershell
java -jar .\target\veltra-0.0.1-SNAPSHOT.jar
```

## API REST

URL de base : `http://localhost:8080`

| Méthode | Endpoint | Action |
| --- | --- | --- |
| POST | `/api/conducteurs` | Créer un conducteur |
| GET | `/api/conducteurs` | Lister les conducteurs |
| GET | `/api/conducteurs/{id}` | Consulter un conducteur |
| GET | `/api/conducteurs/search?nom=...` ou `?email=...` | Rechercher un conducteur |
| PATCH | `/api/conducteurs/{id}` | Modifier un conducteur |
| POST | `/api/passagers` | Créer un passager |
| GET | `/api/passagers` | Lister les passagers |
| GET | `/api/passagers/{id}` | Consulter un passager |
| GET | `/api/passagers/search?email=...` | Rechercher un passager |
| PATCH | `/api/passagers/{id}` | Modifier un passager |
| POST | `/api/vehicules` | Ajouter un véhicule |
| GET | `/api/vehicules/{id}` | Consulter un véhicule |
| GET | `/api/vehicules?conducteurId=...` | Lister les véhicules d'un conducteur |
| PATCH | `/api/vehicules/{id}` | Modifier un véhicule |
| DELETE | `/api/vehicules/{id}` | Supprimer un véhicule non lié à un trajet |
| POST | `/api/trajets` | Publier un trajet |
| GET | `/api/trajets/{id}` | Consulter un trajet |
| GET | `/api/trajets/search` | Rechercher des trajets |
| GET | `/api/trajets/{trajetId}/reservations` | Consulter les réservations du trajet |
| PATCH | `/api/trajets/{id}/annulation` | Annuler un trajet |
| PATCH | `/api/trajets/{id}/terminaison` | Terminer un trajet |
| POST | `/api/reservations` | Réserver des places |
| GET | `/api/reservations/passager/{passagerId}` | Consulter l'historique d'un passager |
| PATCH | `/api/reservations/{reservationId}/annulation` | Annuler une réservation |

La recherche des trajets exige les paramètres `villeDepart`, `villeArrivee` et `dateDepart` (date ISO, par exemple `2026-12-10T09:00:00`). Les paramètres `prixMax`, `nombrePlaces`, `page` et `size` sont facultatifs. Les résultats sont paginés et ordonnés par date de départ croissante.

### Exemple : publier un trajet

```http
POST /api/trajets
Content-Type: application/json
```

```json
{
  "conducteurId": 1,
  "vehiculeId": 1,
  "dateDepart": "2026-12-10T09:00:00",
  "placesProposees": 3,
  "pointsArret": [
    {
      "ville": "Nador",
      "ordre": 0,
      "heurePassage": "2026-12-10T09:00:00",
      "prixDepuisDepart": 0
    },
    {
      "ville": "Fes",
      "ordre": 1,
      "heurePassage": "2026-12-10T12:00:00",
      "prixDepuisDepart": 100
    }
  ]
}
```

Les identifiants de conducteur et de véhicule sont des exemples ; ils doivent exister et le véhicule doit appartenir au conducteur.

### Exemple : réserver

Après création du trajet, récupérer les identifiants réels des points d'arrêt via `GET /api/trajets/{id}` :

```json
{
  "passagerId": 1,
  "trajetId": 1,
  "pointMonteeId": 1,
  "pointDescenteId": 2,
  "nombrePlaces": 1
}
```

Envoyer ce corps à `POST /api/reservations`, en remplaçant les identifiants d'exemple par ceux renvoyés par l'API.

## Règles métier principales

- La capacité d'un véhicule est comprise entre **1 et 8 places** ; les places proposées ne peuvent pas dépasser sa capacité.
- Un trajet doit avoir au moins **deux arrêts**. Les ordres sont continus à partir de zéro ; les villes ne sont pas répétées et les heures de passage sont strictement croissantes.
- Le prix cumulé du premier arrêt est **0** ; les prix suivants sont strictement croissants et utilisent au plus deux décimales.
- Une réservation requiert un trajet publié, un point de montée situé avant le point de descente et un nombre de places disponible suffisant.
- **Montant total** = (prix cumulé à la descente − prix cumulé à la montée) × nombre de places.
- Une réservation confirmée ne peut pas être doublée pour le même passager et le même trajet.
- Les créations de réservations concurrentes sont sérialisées par verrouillage du trajet afin de limiter les risques de surbooking.
- Une réservation peut être annulée uniquement **plus de 24 heures avant le départ** et lorsque le trajet est encore publié ou complet.
- L'annulation d'un trajet annule également ses réservations confirmées.
- Les statuts des trajets incluent `PUBLIE`, `COMPLET`, `ANNULE` et `TERMINE`.

## Organisation du projet

```text
docs/                          Collection Postman et diagramme UML
src/main/java/ma/veltra/
  controllers/                 Endpoints REST
  dto/request/                 Données entrantes
  dto/response/                Données sortantes
  entities/                    Entités JPA
  enums/                       Statuts métier
  exceptions/                  Exceptions et gestionnaire global
  mappers/                     Interfaces MapStruct
  repositories/                Accès aux données
  services/                    Contrats métier
  services/implementations/    Implémentations transactionnelles
src/main/resources/
  application.yaml             Configuration Spring
  db/migration/                Scripts Flyway
src/test/java/ma/veltra/        Tests
compose.yaml                   PostgreSQL local
.env.example                   Exemple de configuration
pom.xml                        Configuration Maven
```

## Documentation et validation

- [Collection Postman](docs/Veltra.postman_collection.json) : parcours des principales opérations de l'API.
- [Diagramme de classes UML](docs/Veltra%20Class%20Diagram.drawio.png) : vue du modèle objet.

**Résultats obtenus localement le 20 novembre 2026 :**

| Vérification | Résultat |
| --- | --- |
| `mvnw.cmd clean verify` | **BUILD SUCCESS** |
| Tests automatisés Maven | **46 exécutés, 0 échec, 0 erreur, 0 ignoré** |
| Audit fonctionnel V5 | **227 PASS, 0 FAIL, 9 SKIP, 2 INFO** |

Les tests non exécutés par l'audit ne sont pas considérés comme validés. Le rapport d'audit est un résultat de vérification locale et n'est pas un livrable versionné du dépôt.

## Auteur

Développé par [Mohamed El Masghati](https://github.com/elmasghatimohamed-cmd).

Projet réalisé dans un cadre pédagogique.
