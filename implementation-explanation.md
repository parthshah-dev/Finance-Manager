# 1. Project Overview

## What Problem This Project Solves
In today's fast-paced economic environment, individuals often struggle to keep track of their personal finances. Ad-hoc spending, unmonitored income sources, and a lack of structured logging lead to poor financial visibility, overspending, and difficulty in achieving savings goals. 

The **Money Manager** project is a self-hosted, multi-user personal finance tracker. It provides a centralized, secure platform for users to:
1. Log multi-category incomes and expenses.
2. View real-time aggregated metrics (Total Income, Total Expense, Net Balance).
3. Query and filter historical transactions based on custom date ranges, keywords, and types.
4. Establish disciplined habits via automated daily reminders and structured report cards sent directly to their email.

## Real-World Use Case
Consider an individual who manages multiple income streams (salary, freelancing, investments) and incurs numerous daily expenses (rent, groceries, entertainment, utilities). Instead of utilizing disjointed spreadsheets, they use this web service. 
- At the end of every day, the system sends an email reminder if they haven't logged their transactions.
- At 11:00 PM, they receive a detailed email report containing a structured table of everything they spent that day, encouraging immediate visual awareness of their expenditure.
- When applying for a loan or planning a budget, they can filter their transactions over the last 6 months to extract precise categories of spending.

## Target Users
- **Salaried Professionals:** Wanting to budget their monthly income and monitor fixed vs. variable expenses.
- **Freelancers/Gig Workers:** Managing irregular income streams and tracking business-related expenses.
- **Students & Individuals:** Seeking to build financial discipline through automated alerts and visual reporting.

## Key Features
- **Secure Registration & Email Activation:** Accounts are created in an inactive state. A cryptographically secure, UUID-based activation link is emailed to the user, preventing fake accounts and spam.
- **JWT-Based Stateless Authentication:** Secure login returns a JSON Web Token (JWT) containing user identity, allowing subsequent requests to be stateless and highly scalable.
- **Dynamic Category Management:** Users can customize categories with custom names, type boundaries (INCOME/EXPENSE), and icons.
- **Flexible Transaction Logging:** Track transactions with name, icon, amount, category, and date.
- **Advanced Filtering and Sorting:** Query endpoints support combinations of search queries, start/end dates, sorting fields (amount, date), and sorting directions (ASC/DESC).
- **Scheduled Automated Services:** Spring Boot's internal task scheduling executes daily:
  - Daily reminders to log transactions.
  - Daily expense digest formatted in an HTML email table.

## High-Level Architecture
The project is built on a **Layered Architecture (N-Tier)** pattern, emphasizing a strict separation of concerns:

```
+-------------------------------------------------------------+
|                       Client App                            |
|             (React/Vite Frontend, Postman, curl)            |
+------------------------------+------------------------------+
                               | HTTPS / REST APIs
                               v
+------------------------------+------------------------------+
|                     Spring Boot Backend                     |
|                                                             |
|  +-------------------------------------------------------+  |
|  |                   Security Filter Chain               |  |
|  |   - JwtRequestFilter (Authenticates stateless JWTs)   |  |
|  |   - CorsConfiguration (Enables cross-origin accesses) |  |
|  +---------------------------+---------------------------+  |
|                              |
|                              v
|  +---------------------------+---------------------------+  |
|  |                     Controller Layer                  |  |
|  |   - Profiles, Incomes, Expenses, Categories, Filters   |  |
|  +---------------------------+---------------------------+  |
|                              |
|                              v
|  +---------------------------+---------------------------+  |
|  |                       Service Layer                   |  |
|  |   - Core business rules, transaction boundaries,       |  |
|  |     ModelMapper transfers, SecurityContext lookup     |  |
|  +---------------------------+---------------------------+  |
|                              |
|                              v
|  +---------------------------+---------------------------+  |
|  |                     Repository Layer                  |  |
|  |   - JpaRepository interface abstraction               |  |
|  |   - Custom JPQL/derived SQL generation               |  |
|  +---------------------------+---------------------------+  |
|                              |
|                              v
|  +---------------------------+---------------------------+  |
|  |                 Hibernate (JPA Provider)              |  |
|  |   - Entity mappings, Persistence Context, L1 Caching  |  |
|  +---------------------------+---------------------------+  |
+------------------------------+------------------------------+
                               | JDBC Connection
                               v
+------------------------------+------------------------------+
|                       Database Engine                       |
|           (MySQL locally, PostgreSQL in Production)         |
+-------------------------------------------------------------+
```

---

# 2. Folder Structure

The project conforms to standard Maven directory conventions, placing all source code inside `src/main/java` under the root package `com.example.moneymanager`. Below is the package structure and the design rationale for each:

```
e:/moneymanager/src/main/java/com/example/moneymanager/
├── MoneymanagerApplication.java
├── config/
│   ├── MapperConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── CategoryController.java
│   ├── DashboardController.java
│   ├── ExpenseController.java
│   ├── FilterController.java
│   ├── HomeController.java
│   ├── IncomeController.java
│   └── ProfileController.java
├── dto/
│   ├── AuthDto.java
│   ├── CategoryDto.java
│   ├── ExpenseDto.java
│   ├── FilterDto.java
│   ├── IncomeDto.java
│   ├── ProfileDto.java
│   ├── ProfileDtoResponse.java
│   └── RecentTransactionDto.java
├── entity/
│   ├── Category.java
│   ├── Expense.java
│   ├── Income.java
│   └── Profile.java
├── enums/
│   └── Type.java
├── exception/
│   └── GlobalExceptionHandler.java
├── repository/
│   ├── CategoryRepository.java
│   ├── ExpenseRepository.java
│   ├── IncomeRepository.java
│   └── ProfileRepository.java
├── security/
│   └── JwtRequestFilter.java
├── service/
│   ├── CategoryService.java
│   ├── CustomUserDetails.java
│   ├── DashboardService.java
│   ├── EmailService.java
│   ├── ExpenseService.java
│   ├── IncomeService.java
│   ├── NotificationService.java
│   └── ProfileService.java
└── util/
    └── JwtUtil.java
```

### Description and Interactions of Packages:

1. **`com.example.moneymanager` (Root Package):**
   - **Why it exists:** Serves as the starting point of the application. Contains `MoneymanagerApplication.java`, which houses the `main` method.
   - **Why needed:** Defines the base component scan path. Spring Boot recursively scans everything under this package for stereotypic annotations (`@Component`, `@Service`, `@Repository`, `@RestController`).
   - **Interactions:** Boots the entire application context and configures global flags (like `@EnableJpaAuditing` and `@EnableScheduling`).

2. **`config` (Configuration Package):**
   - **Why it exists:** Houses configuration classes containing `@Bean` definitions that customize third-party libraries or framework infrastructure (e.g., Spring Security, ModelMapper).
   - **Why needed:** Separates system infrastructure setup from business logic.
   - **Interactions:** `SecurityConfig` interceptors guard the HTTP endpoints exposed by the `controller` package, and `MapperConfig` supplies the mapping engine used by services.

3. **`controller` (Presentation Layer):**
   - **Why it exists:** Defines REST controllers exposing HTTP endpoints to the outside world.
   - **Why needed:** Handles incoming HTTP requests, binds parameters, delegates execution to the service layer, and returns standardized HTTP responses.
   - **Interactions:** Interacts directly with the `service` layer to complete operations and uses `dto` objects for request payload binding and response serialization.

4. **`dto` (Data Transfer Object Package):**
   - **Why it exists:** Contains pure data classes used to model request payloads and API responses.
   - **Why needed:** Decouples the database schema (entities) from the REST API contract. This prevents database layout changes from breaking clients and prevents security issues like over-posting.
   - **Interactions:** Mapped to and from entities inside the `service` layer; serialized/deserialized by Jackson in the `controller` layer.

5. **`entity` (Domain Model Layer):**
   - **Why it exists:** Holds JPA entities mapped directly to database tables.
   - **Why needed:** Represents the persistent state of the application.
   - **Interactions:** Managed by Hibernate (JPA provider) in the `repository` layer; mapped to DTOs in the service layer.

6. **`enums` (Enumeration Package):**
   - **Why it exists:** Houses system-wide enumerations (such as transaction `Type` containing `INCOME` and `EXPENSE`).
   - **Why needed:** Enforces type safety and compile-time boundaries.
   - **Interactions:** Referenced by entities (`Category`), DTOs (`CategoryDto`, `RecentTransactionDto`, `FilterDto`), and controller filter logic.

7. **`exception` (Cross-cutting Concerns - Exceptions):**
   - **Why it exists:** Declares global interceptors for application exceptions.
   - **Why needed:** Standardizes error response payloads, preventing internal stack traces from leaking to clients.
   - **Interactions:** Catches exceptions thrown from the `controller`, `service`, or `repository` layers and translates them into uniform JSON responses.

8. **`repository` (Data Access Layer):**
   - **Why it exists:** Defines interfaces extending `JpaRepository` to abstract database interactions.
   - **Why needed:** Eliminates boilerplate JDBC/SQL query coding.
   - **Interactions:** Injected into the `service` layer to retrieve and persist entities.

9. **`security` (Security Infrastructure Package):**
   - **Why it exists:** Houses the security filters (e.g., `JwtRequestFilter`) that execute on every HTTP request.
   - **Why needed:** Extracts headers, validates JWT validity, and registers authenticated principals in the `SecurityContext`.
   - **Interactions:** Placed before standard Spring Security filters in `SecurityConfig`; relies on `CustomUserDetails` and `JwtUtil`.

10. **`service` (Business Logic Layer):**
    - **Why it exists:** Contains service classes containing core business logic.
    - **Why needed:** The brains of the application. Orchestrates database transactions, maps DTOs, performs authorization checks, and interacts with third-party components (like mail senders).
    - **Interactions:** Injected with `repository` components and configurations; exposes API methods to `controller` components.

11. **`util` (Utilities Package):**
    - **Why it exists:** Houses standalone helper classes (like `JwtUtil`).
    - **Why needed:** Keeps helper code isolated, reusable, and testable.
    - **Interactions:** Utilized by `JwtRequestFilter` and `ProfileService` to generate and parse cryptographic tokens.

---

# 3. Dependency Analysis

The project's dependency hierarchy is managed in `pom.xml`. Let's analyze every dependency:

### 1. `spring-boot-starter-data-jpa`
- **Why it is used:** To implement object-relational mapping (ORM) and abstract data access using JPA (Java Persistence API) with Hibernate as the underlying engine.
- **Under the Hood:** Spring Boot auto-configures a datasource, an `EntityManagerFactory`, and a `JpaTransactionManager`. It scans all interfaces extending `JpaRepository` and dynamically generates proxy implementations at runtime using JDK Dynamic Proxies.
- **Alternatives:** MyBatis (direct SQL mapping), JdbcTemplate (low-level query mapping), Spring Data JDBC (lightweight relational database access without full ORM features like dirty checking or L1 cache).
- **Interview Detail:**
  *Question:* What is the difference between JPA and Hibernate?
  *Answer:* JPA is a specification (a set of guidelines and interfaces under `jakarta.persistence`), while Hibernate is the implementation of that specification. JPA defines how mapping works, but Hibernate actually generates SQL statements and manages caches.

### 2. `spring-boot-starter-security`
- **Why it is used:** To secure REST endpoints through authentication and authorization filters.
- **Under the Hood:** Plugs in the `DelegatingFilterProxy`, which intercepts incoming HTTP requests and routes them to a chain of Spring-managed filters (`SecurityFilterChain`). It disables public access to resources by default unless specifically overridden.
- **Alternatives:** Apache Shiro, Spring Security OAuth2 Client (for delegating identity to providers like Google/GitHub), custom servlet filters.
- **Interview Detail:**
  *Question:* How does a request reach a Spring controller through Spring Security?
  *Answer:* The request first enters the servlet container (Tomcat) and is intercepted by a servlet filter called `DelegatingFilterProxy`. This proxy delegates to a Spring bean called `FilterChainProxy` (often known as the virtual filter chain). The request passes sequentially through registered filters (like `CsrfFilter`, `UsernamePasswordAuthenticationFilter`, and our custom `JwtRequestFilter`). If all filters pass without throwing authentication or authorization exceptions, the request finally hits the `DispatcherServlet` which routes it to the target `@RestController`.

### 3. `spring-boot-starter-mail`
- **Why it is used:** To enable SMTP-based email notifications for user activation and daily logs.
- **Under the Hood:** Configures a `JavaMailSender` bean based on the properties defined under `spring.mail.*`. It manages socket connections, connection timeouts, and authentication protocols (TLS/SSL) with the SMTP server.
- **Alternatives:** SendGrid Java SDK, Mailgun API SDK, AWS SES Java Client.
- **Interview Detail:**
  *Question:* What happens if the mail server is down during user registration?
  *Answer:* If the SMTP server is down, `javaMailSender.send(message)` will throw a `MailException`. Because registration is wrapped in a `@Transactional` block inside `ProfileService.registerProfile`, throwing this exception causes Spring to roll back the database transaction. The user record is not saved, avoiding database pollution with unverified, unreachable accounts.

### 4. `spring-boot-starter-webmvc`
- **Why it is used:** To build RESTful web applications using Spring MVC, using Jackson for JSON serialization/deserialization and embedded Apache Tomcat as the servlet container.
- **Under the Hood:** Auto-configures the central servlet (`DispatcherServlet`), mapping resolver strategies, exception resolvers, and content-negotiation layers. It boots an in-memory Tomcat instance on port 8080.
- **Alternatives:** Spring Boot Starter Webflux (for non-blocking reactive streams, Netty web server), JAX-RS (Jersey implementation).
- **Interview Detail:**
  *Question:* Explain the flow of a request in Spring MVC.
  *Answer:* 
  1. The client sends an HTTP request.
  2. Tomcat intercepts the request and forwards it to the `DispatcherServlet`.
  3. The `DispatcherServlet` queries the `HandlerMapping` to find the corresponding `@RestController` and controller method matching the request URI.
  4. The `DispatcherServlet` routes the request to a `HandlerAdapter`.
  5. The handler adapter resolves parameter bindings (deserializing JSON payloads to DTOs via Jackson using `@RequestBody`) and executes the controller method.
  6. The controller returns a response object.
  7. The `HandlerAdapter` handles serialization of the response to JSON and passes it back to the `DispatcherServlet`, which writes it to the HTTP response stream.

### 5. `mysql-connector-j` & `postgresql` (Runtime scope)
- **Why they are used:** JDBC drivers required for Java applications to establish network sockets and execute queries against MySQL (local development) and PostgreSQL (production) databases.
- **Under the Hood:** Loaded dynamically by the application's ClassLoader at runtime. The driver registers itself with Java's `DriverManager`, facilitating JDBC communication.
- **Alternatives:** H2 Database Driver (for quick local testing, in-memory storage), Oracle/Microsoft SQL Server JDBC drivers.

### 6. `lombok` (Optional)
- **Why it is used:** To reduce boilerplate code by auto-generating getters, setters, constructors, builders, and loggers during compilation.
- **Under the Hood:** Plugs into the Java Compiler (`javac`) compilation pipeline as an annotation processor. It inspects AST (Abstract Syntax Tree) elements decorated with Lombok annotations (like `@Data`, `@RequiredArgsConstructor`) and injects bytecode directly into target `.class` files.
- **Alternatives:** Manual boilerplate code writing, Kotlin data classes, standard Java Records (records are immutable, while Lombok-generated classes can remain mutable).

### 7. `poi` & `poi-ooxml` (Version 5.2.5)
- **Why they are used:** To read, write, and manipulate Microsoft Office formats (specifically Excel spreadsheets `.xls` and `.xlsx`). 
- **Under the Hood:** Uses low-level stream parsers to serialize and deserialize XML representations of spreadsheets, cell structures, styles, and rows.
- **Alternatives:** JExcelAPI (deprecated), Docx4j, EasyExcel (alibaba library optimized for low memory usage).

### 8. `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (Version 0.11.5)
- **Why they are used:** To construct, sign, parse, and validate JSON Web Tokens using cryptographic algorithms (like HMAC-SHA).
- **Under the Hood:** Uses cryptographic providers to sign headers and payloads using secret keys. `jjwt-jackson` uses Jackson to convert claim objects to JSON strings before encoding them to Base64.
- **Alternatives:** Spring Security OAuth2 Resource Server, Auth0 Java-JWT library.
- **Interview Detail:**
  *Question:* Why do we separate `jjwt-api` and `jjwt-impl` dependencies?
  *Answer:* This adheres to clean API-implementation separation. `jjwt-api` contains the public interfaces, compiling fast without coupling code to concrete cryptographic algorithms. `jjwt-impl` contains the implementation details and is resolved at runtime, making it easy to swap implementations without recompiling the code.

### 9. `modelmapper` (Version 3.2.4)
- **Why it is used:** To simplify mapping between entities and DTOs.
- **Under the Hood:** Uses reflection to inspect object properties and matches fields by name and type, automating assignment logic.
- **Alternatives:** MapStruct (safer compile-time mapping code generator), manual mapping methods (cleanest performance, zero reflection overhead).

---

# 4. Application Startup

When you execute `SpringApplication.run(MoneymanagerApplication.class, args)`, Spring Boot initializes the application context:

```
[JVM Boots]
     |
     v
[SpringApplication.run() Executed]
     |
     v
[Prepare Environment] (Loads application.properties, active profiles)
     |
     v
[Create ApplicationContext] (Instantiates AnnotationConfigServletWebServerApplicationContext)
     |
     v
[Component Scan] (Scans from root package recursively for stereotype annotations)
     |
     v
[Register Bean Definitions] (Creates blueprints of configurations, services, repositories)
     |
     v
[Process Auto-Configuration] (Reads META-INF/spring.factories & imports starter configurations)
     |
     v
[Instantiate & Initialize Beans] (Executes constructor injection, processes @PostConstruct)
     |
     v
[Start Embedded Tomcat] (Tomcat starts on port 8080, registers DispatcherServlet & filters)
     |
     v
[ApplicationReadyEvent Fired] (Ready to serve traffic)
```

### Detailed Under-the-Hood Breakdown:

1. **JVM Initialization:** The Java Virtual Machine loads class definitions, initializes memory layouts (Heap, Stack, Metaspace), and calls the main method.
2. **Environment Preparation:** Spring Boot creates a `StandardServletEnvironment` instance. It reads configuration parameters from command-line arguments, system properties, environment variables, and files (`application.properties` and `application-prod.properties`).
3. **ApplicationContext Creation:** Spring instantiates `AnnotationConfigServletWebServerApplicationContext`. This context acts as the central registry for IoC beans.
4. **Component Scan:** An internal reader class (`ClassPathBeanDefinitionScanner`) scans the root package `com.example.moneymanager` and its sub-packages. It detects classes decorated with `@Component`, `@Service`, `@Repository`, and `@RestController` and registers their structural blueprints (BeanDefinitions) in a central registry.
5. **Auto-Configuration Processing:** The framework scans for auto-configuration properties. It parses conditional declarations (`@ConditionalOnClass`, `@ConditionalOnMissingBean`) to configure default beans. For example, if it detects `mysql-connector-j` on the classpath, it automatically creates a HikariCP `DataSource` pointing to the database.
6. **Bean Instantiation & Dependency Injection:** Spring initializes the registered beans in order:
   - Beans are instantiated (usually using constructor injection).
   - Dependencies are resolved and injected.
   - Bean post-processors run. For example, `@Value` annotations are resolved and injected, and dynamic proxy interfaces (such as repositories extending `JpaRepository`) are generated.
7. **Embedded Tomcat Container Startup:** Spring launches an embedded Apache Tomcat web server. It binds to the specified port (default `8080`), registers the `DispatcherServlet` at `/`, and adds servlet filters (such as `DelegatingFilterProxy` for Spring Security) to Tomcat's request handler interceptor pipelines.
8. **Application Ready:** The context fires an `ApplicationReadyEvent`. Scheduled tasks registered using `@Scheduled` are started, and the application begins listening for incoming HTTP requests.

---

# 5. Configuration Files

The project contains two main configuration files in `src/main/resources`: `application.properties` and `application-prod.properties`.

## Analysis of `application.properties`

### 1. Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/moneymanager
spring.datasource.username=root
spring.datasource.password=Parth@1475
```
- **Why used:** Configures the JDBC connection details for the database pool (HikariCP).
- **Alternatives:** Using container-managed JNDI data sources, or defining configuration parameters as environment variables (`spring.datasource.url=${DB_URL}`).
- **Interview Detail:**
  *Question:* Why is keeping credentials in plaintext inside `application.properties` a security vulnerability, and how do we resolve it?
  *Answer:* Hardcoding credentials exposes secrets to anyone with access to the source code repository. A best practice is to use placeholders, like `spring.datasource.password=${DB_PASSWORD}`, and pass the actual value as an environment variable or retrieve it from a secure vault (like AWS Secrets Manager or HashiCorp Vault) at runtime.

### 2. Context Path
```properties
server.servlet.context-path=/api/v1.0
```
- **Why used:** Prefixes all HTTP endpoint routes with a standard API namespace.
- **Alternatives:** Standard path mapping at `/` (default).
- **Interview Detail:**
  *Question:* How does changing the servlet context path affect filters?
  *Answer:* It shifts the interceptor matches. The context path prefix is handled at the servlet container level. Spring MVC's request mapping path matching is relative to this context path, meaning a controller mapped to `/dashboard` actually resolves to `/api/v1.0/dashboard`.

### 3. JPA/Hibernate Properties
```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
- **Why used:** 
  - `ddl-auto=update` checks the database schema matches JPA entities on startup and modifies it if needed (adds columns, updates indices, etc.).
  - `show-sql` and `format_sql` format and print generated SQL statements to the standard console.
- **Alternatives:** For production, `ddl-auto` should be set to `validate` or `none`, using database migration frameworks like Liquibase or Flyway instead.
- **Interview Detail:**
  *Question:* Why is `ddl-auto=update` dangerous for production databases?
  *Answer:* Hibernate may modify the schema automatically on startup, which can lock tables or drop columns if mappings change. It can also cause sync issues in multi-instance deployments where multiple services attempt schema updates simultaneously.

### 4. Mail Configuration
```properties
spring.mail.host=smtp-relay.brevo.com
spring.mail.port=587
spring.mail.username=${BREVO_USERNAME}
spring.mail.password=${BREVO_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.protocol=smtp
spring.mail.properties.mail.smtp.from=${BREVO_FROM_EMAIL:kiddiecorner06@gmail.com}
spring.mail.properties.mail.smtp.ssl.trust=smtp-relay.brevo.com
```
- **Why used:** Configures SMTP relay connections for Brevo (formerly Sendinblue) using standard TLS encryption on port 587. It uses environment variable placeholders to avoid exposing credentials.
- **Alternatives:** Direct API integration using HTTP-based mail senders.

### 5. Application E-mail Toggle
```properties
app.email.enabled=${APP_EMAIL_ENABLED:true}
```
- **Why used:** A custom property to toggle email sending. If disabled locally, the application skips calling SMTP endpoints, avoiding connection timeouts or errors when testing without valid email credentials.

### 6. Security Configurations (JWT and URLs)
```properties
jwt.secret=8c4f9a3e6b2d1c5f8e7a0b9d4c3f2a1e6d5b8c9f0a2e4d7b3c1a5f6e8d9b0c2a
jwt.expiration=86400000
money.manager.frontend.url=${MONEY_MANAGER_FRONTEND_URL:http://localhost:3000}
app.activation.url=${MONEY_MANAGER_BACKEND_URL:http://localhost:8080/api/v1.0}
```
- **Why used:**
  - `jwt.secret` sets the signing key for HS256 JWT generation (256-bit key).
  - `jwt.expiration` sets token validity to 24 hours (86,400,000 ms).
  - `money.manager.frontend.url` defines the frontend origin for CORS and email links.
  - `app.activation.url` specifies the base path for account activation links.

## Analysis of `application-prod.properties`
This profile is activated via `spring.profiles.active=prod`.
```properties
spring.datasource.url=jdbc:postgresql://dpg-d8v23elckfvc73f59sn0-a.oregon-postgres.render.com/moneymanager_r0hm
spring.datasource.username=moneymanager_r0hm_user
spring.datasource.password=S0uz0F4cPx37TuYVz8wlVhB40UxkWSsO
```
- **Why used:** Overrides local MySQL configuration with production-ready PostgreSQL connection credentials hosted on Render.

---

# 6. Database Design

The schema is generated by Hibernate based on entity annotations. Below is the database schema design:

```
                  +------------------------+
                  |        profile         |
                  +------------------------+
                  | id (PK) (BIGINT)       |
                  | email (UK) (VARCHAR)   |
                  | fullname (VARCHAR)     |
                  | password (VARCHAR)     |
                  | profile_img_url (VC)   |
                  | activation_token (VC)  |
                  | is_active (BOOLEAN)    |
                  | created_at (DATETIME)  |
                  | updated_at (DATETIME)  |
                  +------------------------+
                             |
         +-------------------+-------------------+
         | 1                                     | 1
         |                                       |
         | N                                     | N
+------------------------+              +------------------------+
|        category        |              |         income         |
+------------------------+              +------------------------+
| id (PK) (BIGINT)       | 1          N | id (PK) (BIGINT)       |
| name (VARCHAR)         |<-------------| name (VARCHAR)         |
| icon (VARCHAR)         |              | amount (DECIMAL)       |
| type (VARCHAR)         |              | date (DATE)            |
| profile_id (FK) (BI)   |              | profile_id (FK) (BI)   |
| created_at (DATETIME)  |              | category_id (FK) (BI)  |
| updated_at (DATETIME)  |              | created_at (DATETIME)  |
+------------------------+              | updated_at (DATETIME)  |
         ^                              +------------------------+
         | 1
         |
         | N
+------------------------+
|        expense         |
+------------------------+
| id (PK) (BIGINT)       |
| name (VARCHAR)         |
| amount (DECIMAL)       |
| date (DATE)            |
| profile_id (FK) (BI)   |
| category_id (FK) (BI)  |
| created_at (DATETIME)  |
| updated_at (DATETIME)  |
+------------------------+
```

### Description of Tables, Relationships, and Lifecycle:

1. **`profile` Table (Primary User Record):**
   - **Fields:**
     - `id`: Auto-incrementing primary key (`BIGINT`).
     - `email`: User's primary identifier. Mapped to a `UNIQUE` index constraint to prevent duplicate registrations.
     - `fullname`, `password`: Stores name and the hashed BCrypt password.
     - `activation_token`: Stores a temporary UUID string for account verification. Nullified once verified.
     - `is_active`: Boolean flag indicating if the account has been activated. Checked during login.
   - **Lifecycle:** Created when registering a user. Modified when activating the account. It acts as the root configuration owner for categories and transactions.

2. **`category` Table (Transaction Categorization):**
   - **Fields:**
     - `id`: Auto-incrementing primary key (`BIGINT`).
     - `name`: Category name (e.g., "Salary", "Rent").
     - `icon`: Icon identifier string for UI rendering.
     - `type`: String type (`INCOME` or `EXPENSE`).
     - `profile_id`: Foreign key pointing to `profile(id)`. Ensures categories are user-scoped.
   - **Relationships:** Many-to-One with `profile`.
   - **Lifecycle:** Created by users to custom-categorize their transactions. Managed independently of transactions, but referenced by foreign keys in income and expense tables.

3. **`income` and `expense` Tables (Financial Log Ledger):**
   - **Fields:**
     - `id`: Auto-incrementing primary key (`BIGINT`).
     - `name`: Name or short description of the transaction.
     - `amount`: Transaction amount. Uses `DECIMAL(38,2)` to prevent floating-point rounding issues when handling currencies.
     - `date`: The transaction date (`DATE`).
     - `profile_id`: Foreign key pointing to `profile(id)`.
     - `category_id`: Foreign key pointing to `category(id)`.
   - **Relationships:** Many-to-One with `profile` and `category`.
   - **Lifecycle:** Created when logging a transaction. Deleted via manual delete actions.

---

# 7. Entity Explanation

## 1. `Profile` Entity
Mapped to the database table `profile`.
- **`@Entity`:** Identifies this class as a JPA entity mapped to a database table.
- **`@Table(name = "profile")`:** Sets the table name to `profile` instead of defaulting to the class name.
- **`@Id`:** Marks `id` as the primary key.
- **`@GeneratedValue(strategy = GenerationType.IDENTITY)`:** Configures primary key generation to use the database's auto-increment feature (e.g., MySQL's `AUTO_INCREMENT` or PostgreSQL's `SERIAL`).
- **`@Column(unique = true)` on `email`:** Creates a unique index constraint on the `email` column in the database schema.
- **`@CreationTimestamp` and `@UpdateTimestamp`:** Hibernate-specific annotations. `@CreationTimestamp` automatically populates the `createdAt` timestamp when saving the entity for the first time. `@UpdateTimestamp` updates `updatedAt` whenever the entity is updated.
- **`@PrePersist` method `prePersist()`:** A JPA callback method. Automatically runs before Hibernate executes the `INSERT` SQL statement, ensuring `isActive` defaults to `false` if not specified.

## 2. `Category` Entity
- **`@ManyToOne(fetch = FetchType.LAZY)` on `profile`:** Specifies a Many-to-One relationship between categories and users. `FetchType.LAZY` tells Hibernate to load the associated `Profile` record proxy only when explicitly requested (e.g., calling `profile.getEmail()`), avoiding unnecessary database queries.
- **`@JoinColumn(name = "profile_id", nullable = false)`:** Names the foreign key column `profile_id` and configures it to be non-nullable.
- **`@Enumerated(EnumType.STRING)` on `type`:** Saves the enum value as a string (e.g., `"INCOME"` or `"EXPENSE"`) instead of its ordinal integer representation (`0`, `1`). This prevents database migration bugs if enum values are added or reordered.

## 3. `Income` and `Expense` Entities
- **Mappings:** Both entities contain `@ManyToOne` relationships referencing `Profile` and `Category`.
- **`@PrePersist` method `prePersist()`:** If the transaction date is not specified in the payload, this callback automatically sets it to the current date (`LocalDate.now()`).

---

# 8. DTO Layer

## Why DTOs Exist and the Difference from Entities
- **Entities** represent database tables and Hibernate's persistent state. They contain annotations, relations, and internal data structures like hashed passwords and activation tokens.
- **DTOs (Data Transfer Objects)** are plain Java classes used to exchange data between the REST API and the service layer.

```
+---------------+     Jackson Deserializes     +-------------+     Service Layer Maps     +--------------+
| Client JSON   | ===========================> | Request DTO | =========================> | JPA Entity   |
| (Raw Network) |                              | (Java DTO)  |   (e.g., ModelMapper)      | (Database)   |
+---------------+                              +-------------+                            +--------------+
```

## Benefits of Using DTOs:
1. **Decoupling/Abstraction:** Changes to database columns do not break the client-facing API contract.
2. **Security (Prevent Over-posting):** If an entity has fields like `isActive` or `isAdmin`, an attacker could send a payload containing `"isActive": true` to escalate privileges. Using DTOs like `ProfileDto` ensures the API only accepts expected fields.
3. **Optimized Payloads:** Custom DTOs can exclude heavy relationships or flatten maps. For instance, `ExpenseDto` returns `categoryName` and `categoryId` instead of serializing the entire `Category` entity.
4. **Serialization and Validation Separation:** Bean validation annotations can be placed on DTOs without cluttering persistence entities.

---

# 9. Repository Layer

Repositories extend `JpaRepository<Entity, ID>` to manage database operations.

## Repository Interface Methods

### 1. Derived Query Methods
Spring Data JPA parses method names to generate SQL queries automatically. For example:
- `findByEmail(String email)` maps to `SELECT * FROM profile WHERE email = ?`
- `findByProfileIdAndDateBetween(Long profileId, LocalDate start, LocalDate end)` maps to `SELECT * FROM ... WHERE profile_id = ? AND date BETWEEN ? AND ?`

### 2. Custom `@Query` Methods
When queries are too complex for derived method names, custom JPQL (Java Persistence Query Language) queries can be defined:
```java
@Query("SELECT SUM(i.amount) FROM Income i WHERE i.profile.id = :profileId")
BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);
```
- **JPQL vs. Native Queries:** JPQL queries target entity classes and fields rather than database tables and columns, making them database-agnostic. Setting `nativeQuery = true` configures the repository to run raw SQL queries directly on the database engine instead.

### 3. Under-the-Hood SQL Generation
Hibernate intercepts repository calls, parses the metadata (derived syntax or `@Query` definitions), and compiles them into SQL queries. The parameters are bound safely using prepared statements, which protects the application against SQL injection attacks.

---

# 10. Service Layer

The service layer contains the application's core business logic, orchestrating repositories and managing transaction boundaries.

## 1. `ProfileService.java`
- **`registerProfile(ProfileDto)`:** Handles user registration. Checks if the email is already in use. If emails are enabled, it generates a random UUID activation token, hashes the password using BCrypt, saves the profile in an inactive state (`isActive = false`), and emails the activation link to the user.
- **`activateAccount(String token)`:** Searches for a profile matching the activation token. If found, it activates the profile, clears the token, and updates the database.
- **`getCurrentProfile()`:** Retrieves the authenticated user's email from Spring Security's `SecurityContextHolder` and fetches their profile from the database.

## 2. `CategoryService.java`
- **`saveCategory(CategoryDto)`:** Saves a new category. Checks if a category with the same name already exists for the user to prevent duplicate entries.
- **`getCategoryForCurrentUser()` / `getCategoriesByTypeForCurrentUser(Type)`:** Fetches user-scoped categories.
- **`updateCategory(Long id, CategoryDto)`:** Updates the details of a category owned by the current user.

## 3. `IncomeService.java` / `ExpenseService.java`
- **`saveIncome(IncomeDto)` / `saveExpense(ExpenseDto)`:** Resolves the category associated with the transaction, binds it to the current user's profile, maps the DTO to the entity, and persists it.
- **`deletedIncomeById(Long id)` / `deletedExpenseById(Long id)`:** Deletes a transaction by ID after verifying the authenticated user is the owner.
- **`filterIncomes(...)` / `filterExpenses(...)`:** Evaluates dynamic search criteria, dates, and sort directions to query transactions.

## 4. `DashboardService.java`
- **`getDashboardData()`:** Gathers dashboard metrics, combining the 5 most recent incomes and expenses, mapping them to recent transactions sorted chronologically, and calculating net balance, total income, and total expenses.

## 5. `NotificationService.java` & `EmailService.java`
- **`NotificationService`** runs daily scheduled jobs to send reminders and daily expense summaries.
- **`EmailService`** wraps Spring's `JavaMailSender` to deliver simple text and HTML-formatted emails.

---

# 11. Controller Layer

REST Controllers expose HTTP endpoints, prefixing route mapping configurations with the servlet context path `/api/v1.0`.

| Controller Class | HTTP Method | Route | Request Payload | Security | Behavior / Flow |
|---|---|---|---|---|---|
| **ProfileController** | POST | `/register` | `ProfileDto` | Public | Registers a new user, hashes password, emails activation link. |
| | GET | `/activate` | `token` (Query Param) | Public | Validates token, activates account. |
| | POST | `/login` | `AuthDto` | Public | Authenticates credentials, returns JWT. |
| **HomeController** | GET | `/health`, `/status` | None | Public | Returns `"Application is running"`. |
| **CategoryController**| POST | `/categories` | `CategoryDto` | Authenticated | Creates a new user-scoped category. |
| | GET | `/categories` | None | Authenticated | Lists all categories for the current user. |
| | GET | `/categories/{type}` | `type` (Path Variable) | Authenticated | Lists categories filtered by type (`INCOME` / `EXPENSE`). |
| | PUT | `/categories/{id}` | `CategoryDto` | Authenticated | Updates category name, type, or icon. |
| **IncomeController** | POST | `/incomes` | `IncomeDto` | Authenticated | Logs a new income transaction. |
| | GET | `/incomes` | None | Authenticated | Retrieves current month's income logs. |
| | DELETE | `/incomes/{id}`| ID (Path Variable) | Authenticated | Deletes an income transaction by ID (owner check enforced). |
| **ExpenseController**| POST | `/expenses` | `ExpenseDto` | Authenticated | Logs a new expense transaction. |
| | GET | `/expenses` | None | Authenticated | Retrieves current month's expense logs. |
| | DELETE | `/expenses/{id}`| ID (Path Variable) | Authenticated | Deletes an expense transaction by ID (owner check enforced). |
| **FilterController** | POST | `/filter` | `FilterDto` | Authenticated | Dynamic query sorting and filtering for transactions. |
| **DashboardController**| GET | `/dashboard` | None | Authenticated | Fetches recent transactions, totals, and net balance. |

---

# 12. Authentication Flow

The backend implements JWT stateless authentication. Below is the registration, activation, and authentication sequence:

```
Client                      Database                     SMTP Server                   Backend
  |                            |                              |                           |
  |========= Register ========>|                              |                           |
  |   (POST /register)         |-- Check if email exists      |                           |
  |                            |-- Encrypt password (BCrypt)  |                           |
  |                            |-- Save profile (isActive=F)  |                           |
  |                            |-- Generate UUID token        |                           |
  |                            |                              |                           |
  |                            |=========== Send Mail =======>|                           |
  |                            |   (Activation link sent)     |                           |
  |<======= 201 Created =======|                              |                           |
  |                            |                              |                           |
  |==== Click Activation =====>|                              |                           |
  |   (GET /activate?token=)   |-- Find profile by token      |                           |
  |                            |-- Set isActive = true        |                           |
  |                            |-- Clear token                |                           |
  |<======= 200 Activated =====|                              |                           |
  |                            |                              |                           |
  |========== Login ===========>|                              |                           |
  |    (POST /login)           |-- Check credentials          |                           |
  |                            |-- Verify account is active   |                           |
  |                            |-- Generate JWT (HMAC-SHA256) |                           |
  |<==== JWT Token + User =====|                              |                           |
  |                            |                              |                           |
```

### Steps:
1. **Registration:** The client registers with email and password. The server hashes the password and generates an activation token, saving the user as inactive. If email sending is enabled, an activation email is sent.
2. **Activation:** The user clicks the activation link (`/api/v1.0/activate?token=...`). The server validates the token, activates the profile (`isActive = true`), and clears the token.
3. **Login:** The user submits their credentials to `/login`. The authentication manager verifies the password, confirms the account is active, and returns a signed JWT.
4. **Authorized Requests:** For subsequent requests, the client includes the JWT in the HTTP headers:
   `Authorization: Bearer <token>`
   The server intercepts the header, validates the JWT, and loads the user's details into the `SecurityContext`.

---

# 13. Spring Security

Spring Security runs on a chain of servlet filters. The configuration is defined in `SecurityConfig.java`:

```
Incoming Request
       |
       v
+-----------------------------------------------------------+
| Spring Security Filter Chain                              |
|                                                           |
|  +------------------------+                               |
|  | CorsFilter             | (Enforces origin access keys) |
|  +-----------+------------+                               |
|              |                                            |
|              v                                            |
|  +------------------------+                               |
|  | CsrfFilter             | (Disabled for stateless APIs) |
|  +-----------+------------+                               |
|              |                                            |
|              v                                            |
|  +------------------------+                               |
|  | JwtRequestFilter       | (Parses and validates JWT)    |
|  +-----------+------------+                               |
|              |                                            |
|              v                                            |
|  +------------------------+                               |
|  | AuthorizationFilter    | (Verifies permissions)        |
|  +------------------------+                               |
+-----------------------------------------------------------+
       |
       v
DispatcherServlet ---> Controller
```

### Core Architecture Components:

1. **`SecurityFilterChain`:** Configures HTTP route access, disables CSRF protection, and configures the session manager to use stateless sessions.
2. **`JwtRequestFilter`:** Extracts the `Authorization` header. If it starts with `Bearer `, it parses the token, extracts the user's email, and loads their details using `UserDetailsService` if they are not already authenticated. If the token is valid, it registers the user in the `SecurityContext`.
3. **`AuthenticationManager`:** Manages user authentication. Plugs in a `DaoAuthenticationProvider` configured to use `CustomUserDetails` and a BCrypt password encoder.
4. **`UserDetailsService`:** Implemented by `CustomUserDetails.java`, which queries the profile repository to load user records by email.
5. **`BCryptPasswordEncoder`:** A slow, salted hashing algorithm used to securely store passwords, helping protect against rainbow table attacks.

---

# 14. Email Flow

```
+--------------------+
| NotificationService|  ---> [Fires @Scheduled Trigger]
+---------+----------+
          |
          v
+--------------------+
| EmailService       |  ---> [Configured from application.properties]
+---------+----------+
          |
          v
+--------------------+
| JavaMailSender     |  ---> [Formats MimeMessage or SimpleMailMessage]
+---------+----------+
          |
          v
+--------------------+
| SMTP Relay Server  |  ---> [Brevo Relay Host port 587 (STARTTLS)]
+---------+----------+
          |
          v
+--------------------+
| Recipient Mailbox  |
+--------------------+
```

- **Mechanism:** Spring Boot's `@EnableScheduling` runs a background task scheduler.
- **Trigger Profiles:**
  - `@Scheduled(cron = "0 0 22 * * *", zone = "IST")` emails users daily at 10:00 PM IST to remind them to log their transactions.
  - `@Scheduled(cron = "0 0 23 * * *", zone = "IST")` emails users daily at 11:00 PM IST with an HTML-formatted table of their logged expenses for the day.
- **Failures:** If email delivery fails, the mail sender throws a runtime exception, which will trigger a transaction rollback during registration.

---

# 15. Validation

Spring Boot validates incoming payloads using Jakarta Bean Validation.

## Mechanics and Reflection
When a controller method parameter is annotated with `@Valid`, the `MethodValidationPostProcessor` intercepts the request. It uses reflection to inspect the DTO's properties and validate constraints (e.g., `@NotNull`, `@NotBlank`, `@Email`). If validation fails, it throws a `MethodArgumentNotValidException`.

- **`BindingResult`:** An interface containing validation error details. If passed as a method parameter immediately following the validated DTO, Spring will populate it with the errors instead of throwing an exception, allowing the controller to handle validation errors inline.

---

# 16. Exception Handling

The application handles exceptions globally using a centralized exception handler:

- **`@RestControllerAdvice`:** Combines `@ControllerAdvice` and `@ResponseBody`. It acts as an interceptor for exceptions thrown across all controllers.
- **`GlobalExceptionHandler`:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
```
- **Rationale:** Standardizes error responses (returning a JSON payload like `{"error": "message"}` instead of a raw stack trace), which improves security and helps frontend developers handle errors consistently.

---

# 17. Request Lifecycle

The lifecycle of an API request to an authenticated endpoint:

```
[Client App]
     | HTTP Request: GET /api/v1.0/dashboard
     v
[Tomcat Servlet Container]
     | Matches servlet context path "/api/v1.0"
     v
[Spring Security Filter Chain]
     | Intercepts request via DelegatingFilterProxy
     | - CorsFilter: Checks origin headers
     | - JwtRequestFilter: Parses JWT from header, authenticates user
     v
[DispatcherServlet]
     | Maps URI using HandlerMapping
     v
[Controller: DashboardController]
     | Receives request, delegates to DashboardService
     v
[Service: DashboardService]
     | Retrieves current user's profile from SecurityContextHolder
     | Fetches incomes and expenses from respective services
     v
[Repository Layer: IncomeRepository]
     | Queries the database using Hibernate
     v
[Hibernate (ORM Provider)]
     | Translates JPQL to SQL, fetches JDBC ResultSet
     v
[Database Engine]
     | Executes query, returns dataset
     v
[Hibernate (Object Mapping)]
     | Maps ResultSet to entity models
     v
[Service Mapping]
     | ModelMapper maps entities to DTOs
     v
[Controller]
     | Wraps response in a ResponseEntity
     v
[DispatcherServlet]
     | Uses Jackson to serialize DTOs to JSON
     v
[Client App]
     | Receives 200 OK JSON payload
```

---

# 18. Hibernate Internals

Understanding Hibernate's inner workings is crucial for managing performance and data consistency:

1. **Persistence Context:** A first-level (L1) cache managed by the `EntityManager`. It tracks changes to entity instances loaded within a transaction.
2. **Dirty Checking:** Before completing a transaction, Hibernate compares the current state of entities in the persistence context with their loaded state. It automatically writes any changes to the database using `UPDATE` statements, eliminating the need to call `repository.save()` manually.
3. **Lazy vs. Eager Loading:**
   - `LAZY` mapping loads related entities only when they are accessed.
   - `EAGER` mapping loads all related entities immediately in a single query.
4. **Transactions:** `@Transactional` configurations ensure database operations succeed or fail as a single unit. It manages transaction boundaries, rolling back modifications if an unhandled runtime exception occurs.
5. **Entity States:**
   - **Transient:** Newly created instances not yet managed by the persistence context or saved to the database.
   - **Persistent:** Instances currently tracked by the persistence context, with a corresponding record in the database.
   - **Detached:** Instances with a database identifier that are no longer managed by the persistence context (e.g., after the session closes).
   - **Removed:** Instances scheduled to be deleted from the database.

---

# 19. Every API Flow

Detailed request execution flows for core endpoints:

### 1. Register User (`POST /api/v1.0/register`)
- **Controller:** `ProfileController.registerProfile` receives `ProfileDto`.
- **Service:** `ProfileService.registerProfile` maps DTO to `Profile` entity, hashes the password using `PasswordEncoder.encode()`, generates a UUID token, and persists the user using `ProfileRepository`.
- **Database:** Executes `INSERT INTO profile (email, password, is_active, activation_token, ...)`.
- **Email:** If email sending is enabled, it sends an activation link containing the UUID token.
- **Response:** Returns `ProfileDtoResponse` containing the user's ID, email, name, and timestamps.

### 2. Login User (`POST /api/v1.0/login`)
- **Controller:** `ProfileController.login` receives `AuthDto`.
- **Service:** `ProfileService.authenticateAndGenerateToken` queries user by email. If the account is active, it calls the `AuthenticationManager` to authenticate the user and uses `JwtUtil.generateToken` to generate a JWT.
- **Response:** Returns the JWT and the user's profile information.

### 3. Log Income (`POST /api/v1.0/incomes`)
- **Controller:** `IncomeController.addIncome` receives `IncomeDto`.
- **Service:** `IncomeService.saveIncome` retrieves the active profile from the security context, fetches the associated category, maps the DTO to the `Income` entity, and persists it.
- **Database:** Executes `INSERT INTO income (profile_id, category_id, name, amount, date, ...)`.
- **Response:** Returns `IncomeDto` containing the created transaction details.

### 4. Fetch Dashboard (`GET /api/v1.0/dashboard`)
- **Controller:** `DashboardController.getDashboardData`.
- **Service:** `DashboardService.getDashboardData` retrieves the active profile, fetches the 5 most recent incomes and expenses, combines them chronologically, and calculates the net balance.
- **Response:** Returns a map containing the transaction summaries and balance metrics.

---

# 20. Code Walkthrough

## 1. `MoneymanagerApplication.java`
- **Purpose:** Entry point for bootstrapping the Spring Boot application.
- **Annotations:**
  - `@SpringBootApplication`: Combines `@SpringBootConfiguration`, `@EnableAutoConfiguration`, and `@ComponentScan`.
  - `@EnableJpaAuditing`: Enables tracking of entity creation and modification timestamps.
  - `@EnableScheduling`: Activates Spring's scheduled task execution.

## 2. `JwtUtil.java`
- **Methods:**
  - `generateToken(String email)`: Generates a JWT signed with HMAC-SHA256 containing the user's email as the subject.
  - `extractEmail(String token)`: Parses and extracts the email subject from a JWT.
  - `validateToken(String token, UserDetails)`: Validates the token's signature, issuer, and expiration date.

## 3. `JwtRequestFilter.java`
- **Purpose:** A custom filter extending `OncePerRequestFilter`. It intercepts incoming requests, extracts the JWT from the `Authorization` header, validates it, and registers the user in Spring Security's `SecurityContext`.

## 4. `SecurityConfig.java`
- **Purpose:** Configures Spring Security settings, including route authorization rules, CORS, session management, and password encoders.

## 5. `ProfileService.java`
- **Purpose:** Manages user account registration, activation, authentication, and retrieval.

## 6. `NotificationService.java`
- **Purpose:** A service class running daily scheduled jobs to send transaction reminders and expense digests to users.

---

# 21. Interview Questions

## Basic Level
1. **Q:** What is the purpose of `@SpringBootApplication`?
   **A:** It is a convenience annotation that combines `@Configuration` (marks the class as a source of bean definitions), `@EnableAutoConfiguration` (enables Spring Boot's auto-configuration engine), and `@ComponentScan` (tells Spring to scan for components in the current package and its sub-packages).
2. **Q:** What is the difference between `@RestController` and `@Controller`?
   **A:** `@RestController` is a convenience annotation that combines `@Controller` and `@ResponseBody`. It indicates that the class is a controller where every method returns a domain object directly serialized into the HTTP response body (usually as JSON) instead of rendering a view template.

## Intermediate Level
1. **Q:** How is stateless authentication implemented using Spring Security and JWT?
   **A:** We set session creation to stateless (`SessionCreationPolicy.STATELESS`) in `SecurityConfig`. We implement a custom filter (`JwtRequestFilter`) that intercepts requests, extracts the JWT from the `Authorization` header, validates it, and registers the authenticated user in Spring Security's `SecurityContextHolder`.
2. **Q:** Why do we use `@Transactional`?
   **A:** `@Transactional` ensures that a group of database operations run within a single transaction boundary. If any operation in the method throws a runtime exception, Spring automatically rolls back the entire transaction, keeping the database in a consistent state.

## Advanced Level
1. **Q:** What is the N+1 select problem in JPA, and how do you resolve it?
   **A:** The N+1 query problem occurs when the parent entity is fetched using one query, and then the application executes N additional queries to load lazy-associated collections for each of the N returned parents. It can be resolved by using fetch joins in JPQL (`JOIN FETCH`), using `@EntityGraph` definitions, or configuring batch fetching.
2. **Q:** Explain how Spring Boot's auto-configuration works under the hood.
   **A:** On startup, Spring Boot scans classpath declarations, looks for properties defined in `META-INF/spring.factories` or auto-configuration classes, and processes conditional annotations like `@ConditionalOnClass` or `@ConditionalOnMissingBean` to decide which beans to instantiate automatically.

---

# 22. Possible Improvements

1. **Fix Activation Link Bug:**
   Currently, `activationLink = activationUrl + "/api/v1.0/activate?token=..."` can cause `/api/v1.0` to be appended twice if `activationUrl` already contains it. The string concatenation should be cleaned up.
2. **Implement Caching with Redis:**
   Frequently queried data (like category lists and dashboards) can be cached in a Redis store to reduce database load and improve response times.
3. **Database Pagination:**
   Add pagination support (`Pageable`) to transaction history queries to prevent performance issues when users have thousands of logged transactions.
4. **JWT Refresh Tokens:**
   Use short-lived access tokens alongside refresh tokens stored in HTTP-only cookies to improve security.
5. **Rate Limiting:**
   Add rate-limiting filters (using libraries like Bucket4j) on public endpoints like `/login` and `/register` to protect the application against brute-force attacks.

---

# 23. Common Interview Mistakes

- **Mistake:** Explaining JPA as if it is a database engine.
  *Correction:* Clarify that JPA is a Java mapping specification, while Hibernate is the ORM framework that translates Java commands to SQL statements executed by the database engine (like MySQL).
- **Mistake:** Hardcoding secrets and credentials in configuration files.
  *Correction:* Emphasize using environment variables (`${DATABASE_PASSWORD}`) to load credentials securely at runtime.
- **Mistake:** Not explaining database transaction rollbacks when errors occur during registration email delivery.
  *Correction:* Highlight how wrapping the registration method in a `@Transactional` block ensures user creation is rolled back if email delivery fails.

---

# 24. Resume Explanation

## 30-Second Elevator Pitch
"I designed and developed a personal finance web application using Spring Boot 3, Java 25, and React. The backend implements secure user management with activation emails, stateless JWT authentication, and Spring Security. The project features transaction logging, category classification, dynamic filtering, and scheduled daily transaction reminders and expense digests sent via email."

## 3-Minute Explanation
"I developed a personal finance money manager application using Spring Boot and JPA/Hibernate. The system secures resource access using stateless JWT authentication, with a custom request filter validating tokens on every request.
To ensure user engagement, I used Spring's scheduling capabilities to run daily jobs at 10 PM and 11 PM to email logging reminders and expense digests. 
I designed the API to support dynamic search filtering, sorting, and category classification, keeping the database schema decoupled from the REST contract using DTO mappings. The application is profile-scoped, ensuring users can only view, edit, or delete their own data."

---

# 25. Project Flow Summary

```
[Start App] ---> [Register Profile] ---> [Verify Email] ---> [Login & Get JWT]
                                                                     |
                                                                     v
[Access Secure APIs] <--- [Filter Transactions] <--- [Log Income/Expense]
```

### Complete End-to-End Application Lifecycle:
1. **Bootstrap:** Spring Boot initializes the application context, configures the JPA datasource, starts the embedded Tomcat container, and initiates the daily reminder schedulers.
2. **Registration:** A new user registers, creating an inactive profile record with a hashed password and a unique activation token.
3. **Verification:** The user clicks the activation link sent to their email, activating their account and allowing them to log in.
4. **Authentication:** The user logs in, receives a signed JWT, and includes it in the `Authorization` header of subsequent requests.
5. **Data Management:** The user logs incomes and expenses assigned to custom categories. The server verifies transaction ownership before performing database updates.
6. **Automation:** The daily cron jobs run in the background, emailing logging reminders and daily expense summaries directly to the user.
