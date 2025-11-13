# Spring Boot MVC – Gestion Clients (README)

Application MVC simple avec authentification, rôles et CRUD sur des clients, basée sur Spring Boot 3 + Thymeleaf + Spring Security + Spring Data JPA + PostgreSQL.

---

## 1. Fonctionnalités

* Authentification formulaire (Spring Security)
* Rôles `ADMIN` et `USER`

    * `ADMIN` : CRUD complet sur les clients
    * `USER` : lecture seule
* Pages Thymeleaf : `login`, `home`, `clients/list`, `clients/edit`
* CSRF activé par défaut
* Persistance PostgreSQL via JPA/Hibernate

---

## 2. Stack technique

* Java 17
* Spring Boot 3.3.x

    * spring-boot-starter-web
    * spring-boot-starter-thymeleaf
    * spring-boot-starter-security
    * spring-boot-starter-data-jpa
* thymeleaf-extras-springsecurity6
* PostgreSQL 14+ (pilote 42.x)

---

## 3. Prérequis

* JDK 17
* Maven 3.9+
* PostgreSQL accessible (local, VM, ou distant)

Vérifier versions :

```bash
mvn -version
java -version
```

---

## 4. Installation des dépendances

Maven gère tout via `pom.xml`. Commandes de base :

```bash
mvn -U clean package
mvn spring-boot:run
```

Si l’IDE ne “voit” pas les dépendances après un `BUILD SUCCESS`, recharger le projet Maven dans l’IDE.

---

## 5. Base de données PostgreSQL

### 5.1 Création utilisateur et base

Dans `psql` :

```sql
CREATE ROLE myapp WITH LOGIN PASSWORD 'root';
CREATE DATABASE myappdb OWNER myapp;
GRANT ALL PRIVILEGES ON DATABASE myappdb TO myapp;
```

### 5.2 Connexion locale (PostgreSQL sur la même machine que l’app)

`application.properties` :

```properties
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/myappdb
spring.datasource.username=myapp
spring.datasource.password=root
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update  # dev
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

### 5.3 VM VirtualBox en NAT (PostgreSQL dans une VM Ubuntu)

1. PostgreSQL côté VM
   `/etc/postgresql/16/main/postgresql.conf`

```
listen_addresses = '*'
```

`/etc/postgresql/16/main/pg_hba.conf`

```
host    myappdb    myapp    10.0.2.2/32    scram-sha-256
```

Redémarrer :

```bash
sudo systemctl restart postgresql
sudo ss -ltnp | grep 5432   # doit montrer 0.0.0.0:5432
```

2. VirtualBox → NAT Port Forwarding (VM éteinte)

```
Name: postgres, Protocol: TCP
Host IP: 127.0.0.1      Host Port: 5432          (ou 55432 si 5432 utilisé)
Guest IP: 10.0.2.15     Guest Port: 5432         (IP courante de la VM)
```

3. Côté Windows, tester :

```powershell
Test-NetConnection 127.0.0.1 -Port 5432
```

4. URL JDBC côté app (Windows) :

```properties
spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/myappdb
# ... username/password comme plus haut
```

---

## 6. Structure du projet (extrait)

```
src/
  main/
    java/com/example/
      SpringBootMvcLoginApplication.java
      config/
        SecurityConfig.java
        DataSeeder.java         # insère ADMIN/USER et admin/admin si absent
      controller/
        LoginController.java    # GET /login uniquement
        ClientController.java
      model/
        User.java
        Role.java
        Client.java
      repository/
        UserRepository.java
        RoleRepository.java
        ClientRepository.java
    resources/
      templates/
        login.html
        home.html
        clients/
          list.html
          edit.html
      application.properties
pom.xml
```

---

## 7. Sécurité

### 7.1 SecurityConfig (points clés)

* Ne pas gérer `POST /login` dans `LoginController` : le POST `/login` est géré par Spring Security.
* Exemple de règles :

```java
http
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/login","/css/**","/js/**").permitAll()
      .requestMatchers(HttpMethod.GET, "/clients/**").hasAnyRole("USER","ADMIN")
      .requestMatchers(HttpMethod.POST, "/clients/**").hasRole("ADMIN")
      .anyRequest().authenticated()
  )
  .formLogin(form -> form
      .loginPage("/login")
      .loginProcessingUrl("/login")
      .defaultSuccessUrl("/home", true)
  )
  .logout(l -> l
      .logoutUrl("/logout")               // POST /logout
      .logoutSuccessUrl("/login?logout")
      .invalidateHttpSession(true)
      .deleteCookies("JSESSIONID")
      .permitAll()
  );
```

### 7.2 Thymeleaf Security dialect

`pom.xml` :

```xml
<dependency>
  <groupId>org.thymeleaf.extras</groupId>
  <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

Dans les templates :

```html
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
```

Exemples :

```html
<p sec:authorize="isAuthenticated()">Connecté en tant que <strong sec:authentication="name">user</strong></p>

<form th:action="@{/logout}" method="post" sec:authorize="isAuthenticated()">
  <input type="hidden" th:if="${_csrf != null}" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
  <button type="submit">Se déconnecter</button>
</form>
```

---

## 8. Contrôleurs et vues

* `LoginController` : uniquement `GET /login` qui renvoie `login.html`.
* `ClientController` :

    * `GET /clients` : liste + formulaire d’ajout
    * `POST /clients` : création
    * `GET /clients/{id}/edit` : page d’édition
    * `POST /clients/{id}` : mise à jour
    * `POST /clients/{id}/delete` : suppression

Formulaires POST : inclure le token CSRF.

---

## 9. Démarrage

Mode plugin :

```bash
mvn spring-boot:run
```

Mode JAR :

```bash
mvn -DskipTests clean package
java -jar target/spring-boot-mvc-1.0-SNAPSHOT.jar
```

---

## 10. Données de départ

`DataSeeder` crée automatiquement :

* Rôles : `ADMIN`, `USER`
* Utilisateur : `admin` / `admin` (mot de passe BCrypt)

Si vous aviez un mot de passe en clair en base, supprimez l’utilisateur et laissez le seeder le recréer.

---

## 11. Dépannage (erreurs fréquentes)

* `Connection to 127.0.0.1:5432 refused`
  Port forwarding manquant ou mauvais port.
  Vérifier `Test-NetConnection 127.0.0.1 -Port 5432` et l’URL JDBC.

* `password authentication failed for user "myapp"`
  Mauvais mot de passe ou `pg_hba.conf` incorrect. Vérifier la ligne `10.0.2.2/32` en NAT.

* `relation "xxx" does not exist`
  Tables non créées. En dev, mettre `spring.jpa.hibernate.ddl-auto=update` ou exécuter le SQL de création.

* `package org.springframework.data.jpa.repository does not exist`
  Dépendance manquante ou IDE non synchronisé.
  Vérifier `spring-boot-starter-data-jpa` dans `pom.xml` et faire `mvn -U clean package` puis recharger Maven dans l’IDE.

* `Non-parseable POM` / `MalformedInputException` sur `application.properties`
  Encodage. Enregistrer les fichiers en UTF-8.
  Vous pouvez aussi désactiver le filtering des resources dans le `<build>`.

* `TemplateProcessingException` avec `${#httpServletRequest.remoteUser}`
  Utiliser le dialecte `sec:` ou protéger l’expression avec un test de null :

  ```html
  <p th:if="${#httpServletRequest != null and #httpServletRequest.remoteUser != null}">...</p>
  ```

* `GET /logout` donne 404
  Par défaut, logout est en POST. Utiliser un `<form method="post" action="/logout">` avec CSRF.

* Port 8080 occupé
  Changer le port :

  ```properties
  server.port=8081
  ```

---

## 12. Commandes utiles

Maven :

```bash
mvn clean package
mvn spring-boot:run
mvn dependency:purge-local-repository -DreResolve=true
```

PostgreSQL (psql) :

```sql
\conninfo
\l
\c myappdb
\dt
\d clients
\q
```

Windows (test port) :

```powershell
Test-NetConnection 127.0.0.1 -Port 5432
```

---

## 13. Bonnes pratiques

* Garder les entités JPA avec imports `jakarta.persistence.*` (Spring Boot 3).
* En prod : `spring.jpa.hibernate.ddl-auto=validate`, migrations via Flyway ou scripts SQL.
* Ne jamais stocker les mots de passe en clair : BCrypt via `PasswordEncoder`.

---

## 14. Licence

Usage pédagogique / interne. Adapter selon vos besoins.
