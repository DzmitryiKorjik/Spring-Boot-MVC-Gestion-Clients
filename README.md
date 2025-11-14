# Spring Boot MVC – Clients & Utilisateurs

Application MVC avec authentification, rôles, **gestion des clients (CRUD)** et **gestion des utilisateurs** (création par un admin).
Stack : Spring Boot 3, Spring Security, Spring Data JPA, Thymeleaf, PostgreSQL.

---

## 1) Fonctionnalités

* **Login/Logout** via formulaire (CSRF activé).
* **Rôles** :

    * `ADMIN` : CRUD complet sur **clients** + gestion des **utilisateurs**.
    * `USER`  : lecture seule des **clients**.
* **Gestion des clients** : lister, créer, modifier, supprimer.
* **Gestion des utilisateurs (admin)** :

    * Lister les utilisateurs.
    * **Créer un utilisateur** (username + mot de passe confirmé).
    * Mots de passe stockés en **BCrypt**.
* **Pages Thymeleaf** harmonisées (même CSS) :

    * `login`, `home`, `clients/list`, `clients/edit`, `users/list`, `users/register`.

> Option possible (non activée par défaut) : **inscription publique** `/register` pour créer un compte `USER`. Voir plus bas.

---

## 2) Prérequis

* Java **17**
* Maven **3.9+**
* PostgreSQL **14+** (local ou VM)

Vérifier :

```bash
mvn -version
java -version
```

---

## 3) Dépendances principales (`pom.xml`)

* `spring-boot-starter-web`
* `spring-boot-starter-thymeleaf`
* `spring-boot-starter-security`
* `spring-boot-starter-data-jpa`
* `spring-boot-starter-validation`  ← pour `jakarta.validation.*`
* `org.postgresql:postgresql`
* `thymeleaf-extras-springsecurity6`  ← pour `sec:authorize`

---

## 4) Base de données PostgreSQL

### 4.1 Création utilisateur & base

Dans `psql` :

```sql
CREATE ROLE myapp WITH LOGIN PASSWORD 'root';
CREATE DATABASE myappdb OWNER myapp;
GRANT ALL PRIVILEGES ON DATABASE myappdb TO myapp;
```

### 4.2 Configuration Spring (`src/main/resources/application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/myappdb
spring.datasource.username=myapp
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update     # dev
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

### 4.3 PostgreSQL dans une VM VirtualBox (NAT)

* VM éteinte → **NAT Port Forwarding** :

    * Name: `postgres`, TCP, **Host IP `127.0.0.1`**, **Host Port `5432`** (ou `55432`)
    * Guest IP `10.0.2.15`, Guest Port `5432`
* Dans la VM :

    * `postgresql.conf` : `listen_addresses='*'`
    * `pg_hba.conf`     : `host  myappdb  myapp  10.0.2.2/32  scram-sha-256`
    * `sudo systemctl restart postgresql`
* Test côté Windows :

  ```powershell
  Test-NetConnection 127.0.0.1 -Port 5432
  ```

---

## 5) Lancer l’application

### Via plugin

```bash
mvn spring-boot:run
```

### Via JAR

```bash
mvn -DskipTests clean package
java -jar target/spring-boot-mvc-1.0-SNAPSHOT.jar
```

Ouvrir : `http://localhost:8080/login`

---

## 6) Données de départ (seeder)

Un `DataSeeder` (si présent dans `config`) crée :

* Rôles : `ADMIN`, `USER`
* Utilisateur : **admin / admin** (mot de passe BCrypt)

> Si vous aviez un ancien `admin` en clair, supprimez-le d’abord pour laisser le seeder le recréer.

---

## 7) Sécurité (extrait)

```java
http
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/login","/css/**","/js/**").permitAll()
      .requestMatchers(HttpMethod.GET,  "/clients/**").hasAnyRole("USER","ADMIN")
      .requestMatchers(HttpMethod.POST, "/clients/**").hasRole("ADMIN")
      .requestMatchers(HttpMethod.GET,  "/users/**").hasRole("ADMIN")
      .requestMatchers(HttpMethod.POST, "/users/**").hasRole("ADMIN")
      .anyRequest().authenticated()
  )
  .formLogin(form -> form
      .loginPage("/login")
      .loginProcessingUrl("/login")
      .defaultSuccessUrl("/home", true)
  )
  .logout(l -> l
      .logoutUrl("/logout")              // POST /logout (avec CSRF)
      .logoutSuccessUrl("/login?logout")
      .invalidateHttpSession(true)
      .deleteCookies("JSESSIONID")
      .permitAll()
  );
```

**UserDetailsService** : charge l’utilisateur depuis `UserRepository`, applique les rôles `ROLE_...`, et **.disabled(!enabled)**.

---

## 8) Routes & pages

| Route                  | Méthode | Rôle       | Vue                   | Description                       |
| ---------------------- | ------- | ---------- | --------------------- | --------------------------------- |
| `/login`               | GET     | public     | `login.html`          | Formulaire de connexion           |
| `/home` `/`            | GET     | auth       | `home.html`           | Tableau de bord                   |
| `/clients`             | GET     | USER/ADMIN | `clients/list.html`   | Liste des clients                 |
| `/clients`             | POST    | ADMIN      | redirect              | Créer un client                   |
| `/clients/{id}/edit`   | GET     | USER/ADMIN | `clients/edit.html`   | Formulaire d’édition              |
| `/clients/{id}`        | POST    | ADMIN      | redirect              | Mettre à jour un client           |
| `/clients/{id}/delete` | POST    | ADMIN      | redirect              | Supprimer un client               |
| `/users`               | GET     | ADMIN      | `users/list.html`     | Liste des utilisateurs            |
| `/users/new`           | GET     | ADMIN      | `users/register.html` | Créer un utilisateur              |
| `/users`               | POST    | ADMIN      | redirect              | Enregistrer un nouvel utilisateur |
| `/logout`              | POST    | auth       | redirect              | Déconnexion                       |

> Tous les formulaires POST incluent le **CSRF**.

---

## 9) Gestion des utilisateurs (admin)

* **DTO** : `RegisterForm` (`jakarta.validation`), champs `username`, `password`, `confirmPassword`.
* **Service** : `UserService#createUser(form)` :

    * vérifie l’unicité du username
    * vérifie la correspondance des mots de passe
    * `BCrypt` du mot de passe
    * rôle par défaut `USER`
* **Vues** :

    * `users/list.html` : tableau des utilisateurs (username, rôles, enabled)
    * `users/register.html` : formulaire de création (admin)

> Pour promouvoir un utilisateur en `ADMIN`, on peut ajouter une action (ex. POST `/users/{id}/promote`) qui ajoute le rôle `ADMIN` — facile à étendre.

---

## 10) Structure du projet (extrait)

```
src/main/java/com/example/
  SpringBootMvcLoginApplication.java
  config/
    SecurityConfig.java
    DataSeeder.java
  controller/
    LoginController.java
    ClientController.java
    UserController.java
  model/
    User.java
    Role.java
    Client.java
  repository/
    UserRepository.java
    RoleRepository.java
    ClientRepository.java
  service/
    UserService.java
  web/
    RegisterForm.java
src/main/resources/
  templates/
    home.html
    login.html
    clients/
      list.html
      edit.html
    users/
      list.html
      register.html
  static/css/app.css
  application.properties
```

---

## 11) Dépannage rapide

* **`Connection to 127.0.0.1:5432 refused`**
  Port forwarding manquant/port erroné. Tester :
  `Test-NetConnection 127.0.0.1 -Port 5432` (Windows).
  Adapter `spring.datasource.url` au **Host Port**.

* **`password authentication failed`**
  Mauvais mot de passe ou `pg_hba.conf`. Vérifier la ligne `10.0.2.2/32` (NAT).

* **Template 500 / Whitelabel**
  Fichier manquant : s’assurer que `users/list.html` et `clients/list.html` existent aux bons chemins.

* **`Cannot resolve symbol validation` dans l’IDE**
  Ajouter `spring-boot-starter-validation`, recharger Maven, imports en `jakarta.validation.*`.

* **`GET /logout` → 404**
  Logout est **POST** par défaut. Utiliser un `<form method="post" action="/logout">` + CSRF.

---

## 12) Activer l’inscription **publique** (option)

Si vous voulez une page d’inscription ouverte à tous (`/register`) qui crée un **USER** :

* Ajoutez un contrôleur `RegisterController` (GET/POST `/register`) qui réutilise `UserService#createUser`.
* Dans `SecurityConfig` :
  `.requestMatchers("/register", "/register/**").permitAll()`
* Vue : `templates/register.html` (copie de `users/register.html` sans restrictions ADMIN).

---

## 13) Bonnes pratiques

* Imports JPA en **`jakarta.persistence.*`** (Spring Boot 3).
* En prod : `spring.jpa.hibernate.ddl-auto=validate` + **Flyway**/scripts SQL.
* Ne jamais stocker de mot de passe en clair (toujours `PasswordEncoder` / BCrypt).
* Protéger les actions sensibles côté **UI** *et* côté **SecurityConfig**.
