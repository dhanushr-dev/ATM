# 🎯 Interview Preparation — ATM Interface Project

---

## 🟦 SECTION 1: Java Core Questions (30 Questions)

**Q1. What is the difference between `==` and `.equals()` in Java?**
> `==` checks reference equality (whether two variables point to the same object in heap memory). `.equals()` checks logical/value equality as defined by the class. For `String`, `.equals()` compares character sequences. For custom classes, always override `.equals()` alongside `hashCode()`.

**Q2. Explain the four pillars of OOP with examples from this project.**
> - **Encapsulation**: `Account` class — balance field is private, only accessible via validated methods.
> - **Inheritance**: `InsufficientFundsException extends ATMException extends RuntimeException`.
> - **Abstraction**: `ATMController` hides all service/DAO complexity from the UI layer.
> - **Polymorphism**: `TransactionType.getSign()` returns different values per enum constant.

**Q3. What is the difference between checked and unchecked exceptions?**
> Checked exceptions (extend `Exception`) must be declared or caught at compile time. Unchecked exceptions (extend `RuntimeException`) do not. This project uses unchecked exceptions (`ATMException`) so services don't pollute method signatures with checked-exception declarations while still providing structured error codes.

**Q4. What is a `PreparedStatement` and why use it over `Statement`?**
> A `PreparedStatement` is a pre-compiled SQL statement with parameter placeholders (`?`). Benefits: (1) Prevents SQL injection — user input is never concatenated into SQL strings. (2) Better performance when executed repeatedly — query compiled once. This project uses `PreparedStatement` for every single SQL call.

**Q5. What is try-with-resources and why is it important?**
> `try (Resource r = ...) { }` automatically calls `r.close()` when the block exits, even if an exception is thrown. This prevents resource leaks — critical for JDBC `Connection`, `PreparedStatement`, and `ResultSet` objects which hold database resources.

**Q6. What is BCrypt and why use it for PIN hashing?**
> BCrypt is an adaptive hashing algorithm. Unlike MD5/SHA, it is deliberately slow (cost factor) making brute-force attacks impractical. It embeds a random salt, so identical PINs produce different hashes. This project uses jBCrypt with cost factor 12.

**Q7. What is the Singleton pattern? Is it used here?**
> Singleton ensures a class has only one instance. `DatabaseConnection` is a utility class (all static methods) rather than a true Singleton, but effectively serves the same purpose — a single point for obtaining DB connections. A true Singleton would use a private constructor and a static `getInstance()` method.

**Q8. What is the MVC pattern?**
> Model-View-Controller separates concerns: Model (`User`, `Account`, `Transaction`) holds data; View (`LoginFrame`, `DashboardFrame`, panels) renders UI; Controller (`ATMController`) mediates between them. The View never talks to the DAO layer directly.

**Q9. What are `BigDecimal` and why use it for money instead of `double`?**
> `double` is a floating-point type with binary representation errors (e.g., `0.1 + 0.2 ≠ 0.3`). `BigDecimal` provides arbitrary-precision arithmetic with exact decimal representation, making it mandatory for financial calculations to avoid rounding errors.

**Q10. What is `SwingWorker` and why is it used in `LoginFrame`?**
> Swing is single-threaded — all UI operations run on the Event Dispatch Thread (EDT). `SwingWorker` offloads long-running work (like a DB call) to a background thread, preventing the UI from freezing. The `done()` callback runs on the EDT so Swing components can be safely updated.

**Q11. What is the difference between `ArrayList` and `LinkedList`?**
> `ArrayList` uses a dynamic array — O(1) random access, O(n) insert/delete in the middle. `LinkedList` uses doubly-linked nodes — O(n) random access, O(1) insert/delete at ends. This project uses `ArrayList` for transaction lists since random access is more common than insertion.

**Q12. Explain the SOLID principles with examples from this project.**
> - **S** (Single Responsibility): `PasswordUtil` only hashes/verifies PINs.
> - **O** (Open/Closed): New account types can be added to the enum without changing existing logic.
> - **L** (Liskov Substitution): Any ATMException subclass can be used where ATMException is expected.
> - **I** (Interface Segregation): DAO classes are focused on single entities.
> - **D** (Dependency Inversion): `AccountService` depends on abstractions (DAO objects passed by constructor), not concrete implementations.

**Q13. What is the difference between `final`, `finally`, and `finalize()`?**
> `final`: prevents inheritance/overriding/reassignment. `finally`: block always runs after try-catch. `finalize()`: deprecated JVM garbage-collection hook (do not use).

**Q14. What is `volatile` in Java?**
> `volatile` ensures visibility of changes to a variable across threads — reads/writes bypass the CPU cache and go directly to main memory. Used in multi-threaded contexts. Not used in this project as it is a single-user desktop app.

**Q15. Explain Java's memory model: Stack vs Heap.**
> Stack stores method call frames, local variables, and references. Heap stores objects and class instances. When you do `Account acc = new Account()`, the reference `acc` is on the stack but the `Account` object is on the heap.

**Q16. What are Generics in Java?**
> Generics provide type-safe containers. `Optional<Account>` in DAO methods ensures the caller knows they may get an Account or nothing, with compile-time type checking. `List<Transaction>` ensures the list only contains Transaction objects.

**Q17. What is the difference between `Optional.get()` and `Optional.orElseThrow()`?**
> `.get()` throws `NoSuchElementException` if empty — dangerous. `.orElseThrow()` throws a specified exception with a meaningful message, e.g., `orElseThrow(() -> new AccountNotFoundException(id))`.

**Q18. What is method overloading vs overriding?**
> Overloading: same method name, different parameters (compile-time polymorphism). Overriding: subclass provides its own implementation of a superclass method (runtime polymorphism). Example: `mapRow(ResultSet rs)` — not overloaded/overridden here, but `toString()` is overridden in all model classes.

**Q19. What is a `CardLayout` and why is it used here?**
> `CardLayout` manages a stack of panels where only one is visible at a time — like "cards" in a deck. The Dashboard uses it to switch between operation panels (Deposit, Withdraw, etc.) without opening new windows, providing a smooth single-window experience.

**Q20. What is `SwingUtilities.invokeLater()`?**
> Schedules a `Runnable` to run on the Swing Event Dispatch Thread. All Swing component creation and manipulation must happen on the EDT to avoid race conditions. `Main.java` uses it to launch the `LoginFrame` safely.

**Q21. What is the difference between `HashMap` and `Hashtable`?**
> `Hashtable` is synchronized (thread-safe) but slower; `HashMap` is not synchronized but faster. `HashMap` allows one `null` key; `Hashtable` does not. In modern Java, use `ConcurrentHashMap` for thread-safe maps.

**Q22. What is an `enum` and how is it used here?**
> An `enum` is a type with a fixed set of named constants. `TransactionType`, `AccountStatus`, `AccountType`, and `ErrorCode` are all enums in this project. Enums can have fields and methods — `TransactionType` stores `displayName` and `sign`.

**Q23. What is the difference between `interface` and `abstract class`?**
> An interface defines a contract (what to do) with no instance state. An abstract class can have state, constructors, and concrete methods. In Java 8+, interfaces can have default/static methods. This project uses neither, but the DAO layer could easily be refactored to use interfaces for testability.

**Q24. What is autoboxing/unboxing?**
> Automatic conversion between primitives (`int`, `double`) and their wrapper types (`Integer`, `Double`). Example: `int userId = rs.getInt(...)` — `getInt` returns `int`, stored in an `int` field — no autoboxing here, which is correct (avoids `NullPointerException`).

**Q25. What is the `transient` keyword?**
> `transient` marks a field to be excluded from Java serialization. Not directly used in this project, but relevant if models were serialized to disk or sent over a network — `pinHash` would be `transient` to prevent leaking hashed credentials.

**Q26. What is lambda syntax in Java 8+? Give an example.**
> Lambdas are anonymous functions. Example from project:  
> `backBtn.addActionListener(e -> dashboard.showPanel(CARD_HOME));`  
> This is equivalent to `new ActionListener() { public void actionPerformed(ActionEvent e) { ... } }`.

**Q27. What is the `Optional` class?**
> `Optional<T>` is a container that may or may not hold a non-null value. Used in DAO `findBy*` methods to express "account may not exist" without returning `null`. Better than null checks — forces the caller to explicitly handle the empty case.

**Q28. Difference between `String`, `StringBuilder`, and `StringBuffer`?**
> `String` is immutable — concatenation creates new objects. `StringBuilder` is mutable, not thread-safe, fast. `StringBuffer` is mutable and thread-safe (synchronized), slower. Use `StringBuilder` for dynamic string building in a single thread.

**Q29. What is the Java `instanceof` keyword (and pattern matching in Java 16+)?**
> `instanceof` checks if an object is of a given type. Pattern matching: `if (obj instanceof Account acc)` — casts in one step. Used in `equals()` overrides: `if (!(o instanceof Account account)) return false;`

**Q30. Explain exception chaining.**
> Wrapping a caught exception in a custom exception while preserving the original cause. Example: `throw new DatabaseException("Error", e)` — the `SQLException` `e` is the cause, visible in stack traces for debugging. All DAO exceptions in this project use chaining.

---

## 🟨 SECTION 2: ATM Project-Specific Questions (20 Questions)

**Q1. How does the login process work end-to-end?**
> 1. User enters account number and PIN in `LoginFrame`.
> 2. `ATMController.login()` → `AuthService.login()`.
> 3. `AccountDAO.findByAccountNumber()` fetches the account (or throws `AccountNotFoundException`).
> 4. `PasswordUtil.verifyPin(enteredPin, storedHash)` checks BCrypt match.
> 5. On failure: `incrementFailedAttempts()` — if ≥3, `updateStatus(BLOCKED)`.
> 6. On success: `resetFailedAttempts()`, set session, return Account.
> 7. `LoginFrame` opens `DashboardFrame`.

**Q2. How does the transfer achieve atomicity?**
> A single JDBC `Connection` is obtained, `setAutoCommit(false)` is called, then four operations execute: debit source, credit destination, save TRANSFER_OUT record, save TRANSFER_IN record. `commit()` is called only if all four succeed. Any exception triggers `rollback()` in the catch block.

**Q3. Why is BCrypt better than SHA-256 for PIN storage?**
> SHA-256 is fast — an attacker with GPU can compute billions of hashes/second. BCrypt's cost factor (12 here) makes each hash computation take ~250ms — impractical for bulk brute-force. BCrypt also embeds a random salt per hash, preventing rainbow-table attacks.

**Q4. What happens if the database is down when the user tries to transfer?**
> `DatabaseConnection.getConnection()` throws `DatabaseException` (wrapping `SQLException`). The service layer's `try` block exits, `rollback()` is called (harmless if connection wasn't acquired), and the `DatabaseException` propagates to the controller, which propagates to the UI panel where it's caught and displayed as an error message.

**Q5. How is SQL injection prevented in this project?**
> Exclusively through `PreparedStatement` with `?` placeholders. User input is set via `ps.setString(1, userInput)` — the JDBC driver escapes all special characters before sending to MySQL. String concatenation in SQL is never done.

**Q6. What is the purpose of `failed_attempts` column?**
> Tracks consecutive unsuccessful PIN entries. After `MAX_FAILED_ATTEMPTS` (3), the account status is set to BLOCKED, preventing further login attempts. Reset to 0 on every successful login.

**Q7. Why does `AccountService.recordBalanceInquiry()` use a dummy amount of ₹1?**
> The `transactions` table has a `CHECK (amount > 0)` constraint — it cannot store zero amounts. Balance inquiries have no real monetary value, so a nominal ₹1 amount is stored. In a real system, balance inquiries would be logged to a separate audit table.

**Q8. How would you add a daily withdrawal limit feature?**
> Add a `daily_withdrawn` column to `accounts` and a reset timestamp. In `AccountService.withdraw()`, query today's total withdrawals from the `transactions` table using `SUM(amount) WHERE transaction_type='WITHDRAWAL' AND DATE(created_at)=CURDATE()`. Compare against the daily limit before proceeding.

**Q9. What design pattern is `ATMController`?**
> It is a Facade pattern — provides a simplified interface to the more complex subsystem of services and DAOs. The UI layer only knows about `ATMController`, not about `AuthService`, `AccountService`, `AccountDAO`, etc.

**Q10. Why are model classes not annotated with `@Entity`?**
> This project uses raw JDBC, not JPA/Hibernate. `@Entity` is a JPA annotation requiring the EntityManager infrastructure. Plain JDBC gives us full control over SQL and better educational visibility into how queries work.

**Q11. What would happen if `conn.rollback()` itself throws an exception?**
> The `rollback()` call is inside a `catch` block. If it throws, the exception would propagate up, potentially masking the original exception. A more robust implementation would wrap `rollback()` in its own try-catch and log the error without rethrowing.

**Q12. How does the `DashboardFrame` refresh the balance sidebar after every operation?**
> Each panel calls `dashboard.refreshBalanceDisplay()` after a successful transaction. This method calls `controller.getCurrentAccount()` (which calls `authService.getCurrentAccount()`) and updates the `balanceLabel` text. The `AuthService` has a `refreshCurrentAccount()` method that re-fetches from DB.

**Q13. Why is `ReferenceGenerator` thread-safe?**
> It uses `AtomicInteger` for the sequence counter — `AtomicInteger.getAndIncrement()` is an atomic operation that prevents two threads from getting the same sequence number even in concurrent scenarios.

**Q14. What happens if the user opens two windows (duplicate login)?**
> In the current design, each `LoginFrame`/`DashboardFrame` pair has its own `ATMController` instance. However, since the controller is passed to `LoginFrame` in `Main.java` and reused, both windows share the same session. A more robust solution would use server-side session tokens.

**Q15. Why does `ChangePinPanel` force a logout after PIN change?**
> Security best practice — after a PIN change, the session is invalidated and the user must re-authenticate with the new PIN. This prevents session hijacking if the user stepped away while the PIN was being changed.

**Q16. How is the `CardLayout` different from `JTabbedPane`?**
> `JTabbedPane` shows tabs at the top — the user can see and click all tabs. `CardLayout` with a custom sidebar provides full control over navigation, allows for custom styling, and hides operations from casual view — more appropriate for a security-sensitive banking application.

**Q17. What is the purpose of `FONT_MONO` in `ATMTheme`?**
> Monospace fonts (`Consolas`) align characters in columns, making the Mini Statement receipt-style printout properly aligned — amounts and labels line up visually, resembling a real ATM receipt printout.

**Q18. How would you add JPA/Hibernate to this project?**
> Add `spring-data-jpa` or `hibernate-core` dependency, annotate models with `@Entity`, `@Table`, `@Id`, `@Column`. Replace DAO implementations with Spring Data repositories or Hibernate SessionFactory. The service layer would barely change since it already goes through DAO abstractions.

**Q19. What's the significance of `ON DELETE CASCADE` on `fk_account_user`?**
> If a `users` record is deleted, all associated `accounts` records are automatically deleted. This prevents orphaned accounts with no owner. Similarly, `ON DELETE RESTRICT` on `fk_txn_account` prevents deleting an account that has transaction history, preserving the audit trail.

**Q20. How would you make this application multi-user concurrent?**
> For server-side concurrency: use a connection pool (HikariCP), convert to a REST API (Spring Boot), handle concurrent transfers with database-level row locking (`SELECT ... FOR UPDATE`). The JDBC transaction in `AccountService.transfer()` already provides serialization for a single transfer, but concurrent transfers to/from the same account need row-level locking.

---

## 🟩 SECTION 3: JDBC Questions

**Q1. What are the steps to execute a query with JDBC?**
> 1. Load driver class: `Class.forName("com.mysql.cj.jdbc.Driver")`
> 2. Get connection: `DriverManager.getConnection(url, user, pass)`
> 3. Create statement: `conn.prepareStatement(sql)`
> 4. Set parameters: `ps.setString(1, value)`
> 5. Execute: `ps.executeQuery()` or `ps.executeUpdate()`
> 6. Process `ResultSet`
> 7. Close resources (try-with-resources)

**Q2. Difference between `executeQuery()`, `executeUpdate()`, and `execute()`?**
> `executeQuery()` — returns `ResultSet`, used for SELECT. `executeUpdate()` — returns rows affected count, used for INSERT/UPDATE/DELETE. `execute()` — returns `boolean`, used when result type is unknown.

**Q3. What is `ResultSet.next()` and why must you call it first?**
> `ResultSet` starts before the first row. `next()` advances the cursor to the next row and returns `false` when no more rows exist. You must call `next()` at least once before reading column values.

**Q4. How do JDBC transactions work?**
> By default, JDBC uses auto-commit mode (each statement is committed immediately). For multi-statement transactions: `conn.setAutoCommit(false)`, execute statements, then `conn.commit()` or `conn.rollback()`.

**Q5. What is `Statement.RETURN_GENERATED_KEYS`?**
> When inserting a row with an auto-increment primary key, passing this flag to `prepareStatement()` tells JDBC to capture the generated key. Retrieved via `ps.getGeneratedKeys()` after execution.

---

## 🟥 SECTION 4: MySQL Questions

**Q1. What is the difference between `CHAR` and `VARCHAR`?**
> `CHAR(n)` is fixed-length — always uses n bytes. `VARCHAR(n)` is variable-length — uses only what's needed + 1-2 bytes overhead. `CHAR` is faster for fixed-size data like status codes; `VARCHAR` for variable data like names.

**Q2. What are MySQL indexes and when should you add them?**
> Indexes speed up SELECT queries on indexed columns at the cost of INSERT/UPDATE/DELETE performance. Add indexes on columns used in WHERE, JOIN, and ORDER BY clauses. This project indexes `account_number`, `created_at`, and `user_id`.

**Q3. What is `ENGINE=InnoDB` and why use it?**
> InnoDB is MySQL's default storage engine supporting ACID transactions, foreign keys, and row-level locking. MyISAM does not support transactions or foreign keys. InnoDB is required for the `ON DELETE CASCADE` constraints in this schema.

**Q4. What is the difference between `DATETIME` and `TIMESTAMP` in MySQL?**
> `DATETIME` stores date and time without timezone conversion (range: 1000–9999). `TIMESTAMP` stores UTC and converts on retrieval based on session timezone (range: 1970–2038). `DATETIME` is used here for portability.

**Q5. What does `ON UPDATE CURRENT_TIMESTAMP` do?**
> Automatically sets the column to the current timestamp whenever the row is updated — used for `updated_at` columns to track modification time without requiring application-level code.

---

## 🟪 SECTION 5: OOP Questions

**Q1. Explain encapsulation with an example from this project.**
> `Account.balance` is `private`. Direct modification is impossible from outside the class. All changes go through `AccountService.deposit()`, `withdraw()`, `transfer()` — which validate, check constraints, and record transactions before updating the database. The field is protected from inconsistent state.

**Q2. What is the difference between composition and inheritance?**
> Inheritance: "is-a" relationship (`InsufficientFundsException` IS an `ATMException`). Composition: "has-a" relationship (`AccountService` HAS an `AccountDAO`). Composition is generally preferred for flexibility — `ATMController` uses composition to hold references to services.

**Q3. What is method chaining? Is it used here?**
> Method chaining returns `this` from methods to allow `obj.method1().method2()`. The `Optional` API uses it: `accountDAO.findByAccountNumber(id).orElseThrow(() -> ...)`. Not implemented in model setters here (returns void) but could be added (Builder pattern).

---

## 🟫 SECTION 6: HR Questions

**Q1. Tell me about this project.**
> "I built a full-stack ATM Interface application as part of my Oasis Infobyte Java Development Internship. It supports secure login with BCrypt-hashed PINs, deposits, withdrawals, inter-account transfers with atomic JDBC transactions, transaction history, mini statements, and PIN change. I followed MVC architecture with SOLID principles, used PreparedStatements throughout to prevent SQL injection, and built a professional dark-themed Swing UI."

**Q2. What was the most challenging part?**
> "The transfer feature — ensuring atomicity so both the debit and credit either complete together or roll back together. I had to manage the JDBC transaction explicitly: disable auto-commit, run all four SQL statements (debit, credit, two transaction inserts) on the same connection, then commit or rollback based on success or failure."

**Q3. What would you improve with more time?**
> "I'd add HikariCP connection pooling for production performance, export statements to PDF using iText, replace manual DI with Spring's IoC container, and add a CI/CD pipeline with GitHub Actions to run tests on every push."

**Q4. How do you ensure code quality?**
> "I follow SOLID principles, write meaningful method names, keep methods small and focused (single responsibility), add JavaDoc for public APIs, use custom exceptions with error codes instead of bare string messages, and write unit tests for validators and services."

**Q5. What did you learn from this internship project?**
> "I deepened my understanding of JDBC transaction management, secure credential storage with BCrypt, Swing's event-driven model and the importance of using SwingWorker for database calls, and layered MVC architecture that makes code maintainable and testable."
