# Resume Project Description — ATM Interface

---

## Option 1: Concise (One-liner for experience section)

**ATM Interface** | Java 17, Swing, MySQL, JDBC, Maven | *Oasis Infobyte Java Internship*
> Built an enterprise-grade ATM simulation with secure BCrypt PIN authentication, JDBC-transactional fund transfers, full transaction history, and a professional dark-themed MVC Swing UI.

---

## Option 2: Standard (2–3 bullet points)

**ATM Interface Application** — *Oasis Infobyte Java Development Internship*
- Developed a full-featured ATM simulation in Java 17 using MVC architecture, Swing GUI, MySQL, and JDBC with PreparedStatements for SQL injection prevention.
- Implemented BCrypt-hashed PIN storage, 3-attempt lockout, atomic inter-account fund transfers (JDBC commit/rollback), and real-time balance updates.
- Applied SOLID principles, custom exception hierarchy, input validation layer, and JUnit 5 / Mockito unit tests achieving comprehensive coverage of service and validation logic.

---

## Option 3: Detailed (For portfolio/project section)

**ATM Interface** | Java 17 · Swing · MySQL · JDBC · Maven · JUnit 5
*Oasis Infobyte Java Development Internship — 2024*

Designed and developed a production-quality ATM banking simulation desktop application:
- **Security**: BCrypt PIN hashing (cost factor 12), automatic account lockout after 3 failed attempts, forced logout after PIN change.
- **Banking Operations**: Deposit, withdrawal, fund transfer (atomic JDBC transaction), balance inquiry, transaction history, mini statement.
- **Architecture**: Strict MVC layering — UI → Controller → Service → DAO → MySQL. Zero business logic in UI; zero SQL in service layer.
- **Database**: Designed relational schema (users, accounts, transactions) with FK constraints, CHECK constraints, and optimized indexes.
- **Code Quality**: SOLID principles, custom exception hierarchy with error codes, centralised input validation, try-with-resources throughout.
- **Testing**: JUnit 5 unit tests with Mockito mocks for service and validation layers; 30-case manual test suite covering all edge cases.

---

## ATS-Optimized Resume Bullet Points

• Architected Java 17 desktop ATM application using MVC pattern with Controller, Service, DAO, and Repository layers separating presentation, business, and data concerns

• Secured user authentication with BCrypt password hashing algorithm, PIN validation, and automated account lockout mechanism preventing brute-force attacks

• Implemented ACID-compliant fund transfer feature using JDBC transaction management (setAutoCommit/commit/rollback) ensuring data integrity across concurrent operations

• Designed MySQL relational database schema with normalized tables, foreign key constraints, check constraints, and performance indexes for account and transaction data

• Prevented SQL injection vulnerabilities by exclusively using PreparedStatement with parameterized queries across all database interactions

• Built responsive Java Swing UI with CardLayout navigation, custom dark theme, SwingWorker background threads preventing EDT blocking, and confirmation dialogs

• Applied SOLID principles and clean code standards: single-responsibility classes, dependency injection via constructors, meaningful naming, and JavaDoc documentation

• Developed JUnit 5 unit test suite with Mockito mocking framework covering input validation, PIN hashing, and service-layer business logic

• Managed project build lifecycle, dependency resolution, and JAR packaging using Apache Maven with fat-JAR assembly plugin for single-file deployment

• Implemented comprehensive exception handling hierarchy with custom ErrorCode enumeration providing structured, user-friendly error messages throughout the application
