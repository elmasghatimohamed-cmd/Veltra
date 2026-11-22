
# Veltra

**Veltra** est une API REST de covoiturage développée avec **Java 25, Spring Boot 4.1.2 et PostgreSQL 16**.

Elle permet de gérer les conducteurs, les passagers, les véhicules et les trajets comportant plusieurs points d'arrêt, ainsi que les réservations de places entre deux villes d'un même trajet.

L'application intègre les règles métier liées à la disponibilité des places, au calcul des tarifs par segment, aux annulations et à la gestion des réservations concurrentes.

> Projet backend pédagogique, sans authentification ni interface utilisateur.

## Fonctionnalités

### Gestion des utilisateurs et véhicules
- Création, consultation, recherche et modification des conducteurs et passagers.
- Enregistrement et gestion des véhicules associés aux conducteurs.
- Contrôle de la capacité des véhicules et de leur utilisation dans les trajets.
- Suppression d'un véhicule uniquement lorsqu'il n'est associé à aucun trajet.

### Gestion des trajets
- Publication de trajets avec points d'arrêt intermédiaires ordonnés.
- Définition des horaires de passage et des tarifs cumulés.
- Recherche de trajets par villes de départ et d'arrivée, date, budget et places disponibles.
- Pagination et tri des résultats.
- Gestion du cycle de vie des trajets : `PUBLIE`, `COMPLET`, `ANNULE`, `TERMINE`.

### Gestion des réservations
- Réservation d'une ou plusieurs places entre deux points d'arrêt.
- Vérification de l'ordre des arrêts et des places disponibles.
- Calcul automatique du tarif selon le segment et le nombre de places.
- Consultation des réservations par passager ou par trajet.
- Annulation des réservations selon les contraintes temporelles.
- Annulation automatique des réservations confirmées lors de l'annulation d'un trajet.
- Protection contre les réservations concurrentes susceptibles de provoquer un surbooking.

### Qualité et fiabilité
- Validation des données entrantes avec Jakarta Bean Validation.
- Gestion centralisée des exceptions et des réponses d'erreur.
- Transactions Spring pour les opérations métier.
- Verrouillage pessimiste lors de la création des réservations.
- Journalisation des opérations métier.
- Tests automatisés avec JUnit, Mockito et AssertJ.

## Stack technique

| Composant | Technologie |
|---|---|
| Langage | Java 25 |
| Framework | Spring Boot 4.1.2, Spring MVC |
| Persistance | Spring Data JPA, Hibernate |
| Base de données | PostgreSQL 16 |
| Migrations | Flyway |
| Mapping DTO | MapStruct 1.6.3 |
| Validation | Jakarta Bean Validation |
| Tests | JUnit, Mockito, AssertJ |
| Conteneurisation | Docker Compose |
| Build | Maven Wrapper |

## Architecture

L'application adopte une architecture backend en couches afin de séparer les responsabilités :

- **Controllers** : exposition des endpoints REST et traitement des requêtes HTTP.
- **DTOs** : définition des contrats d'entrée et de sortie.
- **Services** : implémentation des règles métier et gestion transactionnelle.
- **Repositories** : accès aux données avec Spring Data JPA.
- **Entities** : représentation du modèle métier et des relations JPA.
- **Mappers** : conversion entre entités et DTOs avec MapStruct.
- **Exceptions** : gestion centralisée des erreurs fonctionnelles et techniques.

La persistance repose sur PostgreSQL. Les migrations sont gérées par Flyway et la conformité du schéma est vérifiée par Hibernate avec `ddl-auto: validate`.

Les points d'arrêt sont rattachés à leur trajet et représentent son itinéraire ordonné. Ils sont également utilisés pour identifier les segments de réservation et calculer leurs tarifs.

## Démarrage local

### Prérequis

- JDK 25
- Docker et Docker Compose
- PowerShell sous Windows

### 1. Cloner le dépôt

```powershell
git clone https://github.com/elmasghatimohamed-cmd/Veltra.git
cd Veltra
```

### 2. Configurer l'environnement

Créer le fichier `.env` à partir du modèle fourni :

```powershell
Copy-Item .env.example .env
```

Variables attendues :

```dotenv
POSTGRES_DB=veltra_db
POSTGRES_USER=veltra_user
POSTGRES_PASSWORD=change-me-locally
```

Le fichier `.env` doit rester local et ne doit pas être versionné.

**Important :** Spring Boot récupère les identifiants PostgreSQL depuis les variables d'environnement du processus Java. Docker Compose ne transmet pas automatiquement le contenu du fichier `.env` à l'application.

Sous PowerShell, charger les variables avec :

```powershell
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$') {
        Set-Item -Path "Env:$($matches[1])" -Value $matches[2]
    }
}
```

### 3. Démarrer PostgreSQL

```powershell
docker compose up -d --wait
```

PostgreSQL est accessible sur `127.0.0.1:5433`. Les données sont conservées dans un volume Docker.

### 4. Lancer l'application

```powershell
.\mvnw.cmd spring-boot:run
```

L'API est accessible sur :

`http://localhost:8080`

Au démarrage, Flyway applique les migrations SQL disponibles dans `src/main/resources/db/migration/`.

### 5. Compiler et exécuter les tests

```powershell
.\mvnw.cmd clean verify
```

Pour exécuter le JAR généré :

```powershell
java -jar .\target\veltra-0.0.1-SNAPSHOT.jar
```

## API REST

**URL de base :** `http://localhost:8080`

### Conducteurs

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/conducteurs` | Créer un conducteur |
| GET | `/api/conducteurs` | Lister les conducteurs |
| GET | `/api/conducteurs/{id}` | Consulter un conducteur |
| GET | `/api/conducteurs/search` | Rechercher par nom ou email |
| PATCH | `/api/conducteurs/{id}` | Modifier un conducteur |

### Passagers

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/passagers` | Créer un passager |
| GET | `/api/passagers` | Lister les passagers |
| GET | `/api/passagers/{id}` | Consulter un passager |
| GET | `/api/passagers/search` | Rechercher par email |
| PATCH | `/api/passagers/{id}` | Modifier un passager |

### Véhicules

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/vehicules` | Ajouter un véhicule |
| GET | `/api/vehicules/{id}` | Consulter un véhicule |
| GET | `/api/vehicules?conducteurId={id}` | Lister les véhicules d'un conducteur |
| PATCH | `/api/vehicules/{id}` | Modifier un véhicule |
| DELETE | `/api/vehicules/{id}` | Supprimer un véhicule non utilisé |

### Trajets

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/trajets` | Publier un trajet |
| GET | `/api/trajets/{id}` | Consulter un trajet |
| GET | `/api/trajets/search` | Rechercher des trajets |
| GET | `/api/trajets/{trajetId}/reservations` | Consulter les réservations |
| PATCH | `/api/trajets/{id}/annulation` | Annuler un trajet |
| PATCH | `/api/trajets/{id}/terminaison` | Terminer un trajet |

### Réservations

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/reservations` | Effectuer une réservation |
| GET | `/api/reservations/passager/{passagerId}` | Consulter l'historique |
| PATCH | `/api/reservations/{reservationId}/annulation` | Annuler une réservation |

### Recherche de trajets

`GET /api/trajets/search`

**Paramètres obligatoires :**
- `villeDepart`
- `villeArrivee`
- `dateDepart` au format ISO 8601

**Paramètres facultatifs :**
- `prixMax`
- `nombrePlaces`
- `page`
- `size`

Les résultats sont paginés et triés par date de départ croissante.

## Exemples d'utilisation

### Publier un trajet

`POST /api/trajets`

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

Le conducteur et le véhicule doivent exister dans la base de données. Le véhicule doit appartenir au conducteur.

### Réserver des places

`POST /api/reservations`

```json
{
  "passagerId": 1,
  "trajetId": 1,
  "pointMonteeId": 1,
  "pointDescenteId": 2,
  "nombrePlaces": 1
}
```

Les identifiants présentés sont indicatifs. Les identifiants réels des points d'arrêt doivent être récupérés depuis `GET /api/trajets/{id}`.

Le montant de la réservation est calculé automatiquement en fonction du segment et du nombre de places.

## Règles métier principales

### Véhicules et trajets

- La capacité d'un véhicule doit être comprise entre **1 et 8 places**.
- Le nombre de places proposées ne peut pas dépasser la capacité du véhicule.
- Chaque trajet doit comporter au minimum deux points d'arrêt.
- Les points d'arrêt suivent un ordre continu à partir de zéro.
- Les villes d'un même trajet ne peuvent pas être répétées.
- Les horaires de passage doivent être strictement croissants.
- Le prix cumulé du premier arrêt est égal à zéro.
- Les prix cumulés suivants sont strictement croissants, avec deux décimales au maximum.

### Réservations

- Seuls les trajets publiés disposant de places suffisantes peuvent recevoir de nouvelles réservations.
- Le point de montée doit précéder le point de descente.
- Un passager ne peut pas disposer de deux réservations confirmées pour un même trajet.
- Le nombre de places réservées ne peut pas dépasser les places restantes.
- Les opérations concurrentes de réservation sont coordonnées par verrouillage pessimiste du trajet.

**Calcul du montant :**

```text
Montant total =
(Prix cumulé à la descente - Prix cumulé à la montée)
× Nombre de places réservées
```

### Annulations et cycle de vie

- Une réservation peut être annulée uniquement plus de 24 heures avant le départ, lorsque le trajet est `PUBLIE` ou `COMPLET`.
- L'annulation d'un trajet entraîne l'annulation de ses réservations confirmées.
- Un trajet peut être clôturé après son départ.
- Les statuts métier des trajets sont `PUBLIE`, `COMPLET`, `ANNULE` et `TERMINE`.

## Structure du projet

```text
Veltra/
├── docs/
│   ├── Veltra.postman_collection.json
│   └── Veltra Class Diagram.drawio.png
├── src/
│   ├── main/
│   │   ├── java/ma/veltra/
│   │   │   ├── controllers/
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── entities/
│   │   │   ├── enums/
│   │   │   ├── exceptions/
│   │   │   ├── mappers/
│   │   │   ├── repositories/
│   │   │   └── services/
│   │   │       └── implementations/
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/migration/
│   └── test/java/ma/veltra/
├── compose.yaml
├── .env.example
├── pom.xml
└── README.md
```

## Documentation

- [Collection Postman](docs/Veltra.postman_collection.json) : requêtes et scénarios d'utilisation de l'API.
- [Diagramme de classes UML](docs/Veltra%20Class%20Diagram.drawio.png) : entités, relations et structure du modèle métier.

## Auteur

Développé par **[Mohamed El Masghati](https://github.com/elmasghatimohamed-cmd)**.

Projet réalisé dans un cadre pédagogique.
