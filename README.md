# 🏧 ATM Interface — Oasis Infobyte Java Development Internship

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?logo=java" />
  <img src="https://img.shields.io/badge/Maven-3.9-blue?logo=apachemaven" />
  <img src="https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql" />
  <img src="https://img.shields.io/badge/Swing-UI-green" />
  <img src="https://img.shields.io/badge/Architecture-MVC-purple" />
  <img src="https://img.shields.io/badge/License-MIT-lightgrey" />
</p>

> **A fully functional, enterprise-grade ATM Interface application built with Java 17, Swing, MySQL, JDBC, and Maven — following clean MVC architecture with SOLID principles.**

---

## 📋 Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Database Design](#database-design)
- [Prerequisites](#prerequisites)
- [Installation Guide](#installation-guide)
- [Running the Application](#running-the-application)
- [Test Credentials](#test-credentials)
- [Screenshots Guide](#screenshots-guide)
- [Future Enhancements](#future-enhancements)

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 Secure Login | BCrypt-hashed PIN, 3-attempt lockout |
| 💰 Deposit | Deposit cash with quick-select amounts |
| 💸 Withdrawal | Withdraw with validation and balance check |
| ↔ Transfer | Atomic inter-account transfer (JDBC transaction) |
| 💳 Balance Inquiry | Real-time balance with inquiry logging |
| 📋 Transaction History | Full sortable history table |
| 📄 Mini Statement | Last 5 transactions in receipt format |
| 🔑 Change PIN | Secure PIN change with current-PIN verification |
| ✅ Input Validation | Regex, range, and format checks on every input |
| 💬 Confirmation Dialogs | Confirm before every destructive operation |
| 🎨 Professional UI | Dark theme, card layout, emoji icons, hover effects |

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 (LTS) | Core language |
| Maven | 3.9+ | Build & dependency management |
| Java Swing | JDK built-in | Desktop GUI |
| MySQL | 8.x | Relational database |
| JDBC | JDK built-in | Database connectivity |
| BCrypt (jBCrypt) | 0.4 | PIN hashing |
| JUnit 5 | 5.10 | Unit testing |
| Mockito | 5.11 | Mock objects for testing |

---

## 🏗 Architecture

```
MVC + Layered Architecture
────────────────────────────────────────────────
  UI Layer      : LoginFrame, DashboardFrame, *Panel
       ↕
  Controller    : ATMController (single facade)
       ↕
  Service Layer : AuthService, AccountService
       ↕
  DAO Layer     : UserDAO, AccountDAO, TransactionDAO
       ↕
  Database Layer: DatabaseConnection (JDBC)
       ↕
  MySQL Database: atm_db
────────────────────────────────────────────────
```

---

## 📂 Project Structure

```
ATM Interface/
├── pom.xml
├── README.md
├── MANUAL_TEST_CASES.md
├── INTERVIEW_PREP.md
├── docs/
│   ├── LINKEDIN_POST.md
│   └── DEMO_VIDEO_SCRIPT.md
└── src/
    ├── main/
    │   ├── java/com/oasisinfobyte/atm/
    │   │   ├── Main.java
    │   │   ├── controller/
    │   │   │   └── ATMController.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   └── AccountService.java
    │   │   ├── dao/
    │   │   │   ├── UserDAO.java
    │   │   │   ├── AccountDAO.java
    │   │   │   └── TransactionDAO.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Account.java
    │   │   │   └── Transaction.java
    │   │   ├── database/
    │   │   │   └── DatabaseConnection.java
    │   │   ├── exception/
    │   │   │   ├── ATMException.java
    │   │   │   ├── InsufficientFundsException.java
    │   │   │   ├── AccountNotFoundException.java
    │   │   │   └── DatabaseException.java
    │   │   ├── validation/
    │   │   │   └── InputValidator.java
    │   │   ├── utility/
    │   │   │   ├── PasswordUtil.java
    │   │   │   ├── FormatUtil.java
    │   │   │   └── ReferenceGenerator.java
    │   │   └── ui/
    │   │       ├── LoginFrame.java
    │   │       ├── DashboardFrame.java
    │   │       ├── theme/
    │   │       │   └── ATMTheme.java
    │   │       └── panels/
    │   │           ├── HomePanel.java
    │   │           ├── DepositPanel.java
    │   │           ├── WithdrawPanel.java
    │   │           ├── TransferPanel.java
    │   │           ├── BalancePanel.java
    │   │           ├── HistoryPanel.java
    │   │           ├── MiniStatementPanel.java
    │   │           └── ChangePinPanel.java
    │   └── resources/
    │       ├── database.properties
    │       └── sql/
    │           ├── schema.sql
    │           └── sample_data.sql
    └── test/
        └── java/com/oasisinfobyte/atm/
            ├── validation/InputValidatorTest.java
            ├── utility/PasswordUtilTest.java
            └── service/AccountServiceTest.java
```

---

## 🗄 Database Design

### ER Diagram
```
[users] 1 ──────< [accounts] 1 ──────< [transactions]
  user_id (PK)      account_number (PK)   transaction_id (PK)
  full_name         user_id (FK)          account_number (FK)
  email             pin_hash              transaction_type
  phone             balance               amount
  created_at        account_type          balance_after
                    status                description
                    failed_attempts       reference_number
                    last_login            created_at
```

### Tables
| Table | Description |
|---|---|
| `users` | Personal information per customer |
| `accounts` | ATM account details, PIN hash, balance |
| `transactions` | Immutable ledger of all operations |

---

## 📋 Prerequisites

- Java JDK 17 or higher ([Download](https://adoptium.net/))
- Apache Maven 3.9+ ([Download](https://maven.apache.org/))
- MySQL 8.x ([Download](https://dev.mysql.com/downloads/))
- Git ([Download](https://git-scm.com/))
- IDE: Eclipse / IntelliJ IDEA

---

## 🚀 Installation Guide

### Step 1 — Clone the repository
```bash
git clone https://github.com/your-username/atm-interface.git
cd atm-interface
```

### Step 2 — Set up the database
```bash
# Open MySQL CLI or MySQL Workbench
mysql -u root -p

# Run the schema script
source src/main/resources/sql/schema.sql

# Load sample data
source src/main/resources/sql/sample_data.sql
```

### Step 3 — Configure database connection
Edit `src/main/resources/database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/atm_db?useSSL=false&serverTimezone=UTC
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

### Step 4 — Build with Maven
```bash
mvn clean package
```

### Step 5 — Run the application
```bash
java -jar target/ATM-Interface.jar
```

---

## ▶ Running the Application

### From IDE (Eclipse/IntelliJ)
1. Import as Maven project
2. Run `com.oasisinfobyte.atm.Main`

### From command line
```bash
mvn exec:java -Dexec.mainClass="com.oasisinfobyte.atm.Main"
```

### Run tests
```bash
mvn test
```

---

## 🔑 Test Credentials

| Account Number | PIN | Balance | Type |
|---|---|---|---|
| `1001000000000001` | `1234` | ₹25,000 | Savings |
| `1001000000000002` | `5678` | ₹50,000 | Current |
| `1001000000000003` | `9999` | ₹10,000 | Savings |

> ⚠️ PINs in `sample_data.sql` are BCrypt-hashed. The values above are the plain-text PINs.

---

## 📸 Screenshots Guide

| Screen | Description |
|---|---|
| Login | Dark themed login with account number + PIN fields |
| Dashboard | Sidebar nav + quick-action grid + balance card |
| Deposit | Amount field with quick-select buttons |
| Withdraw | Amount field with confirmation dialog |
| Transfer | Destination account + amount fields |
| Balance | Large balance display + account info |
| History | Sortable table with color-coded amounts |
| Mini Statement | Receipt-style printout |
| Change PIN | Secure PIN change with 3 fields |

---

## 🔮 Future Enhancements

- [ ] Cardless Cash Withdrawal (QR code)
- [ ] SMS/Email OTP verification
- [ ] Account statement PDF export
- [ ] Admin dashboard for account management
- [ ] Multi-language support (i18n)
- [ ] Connection pooling (HikariCP)
- [ ] Audit log table
- [ ] Biometric PIN (fingerprint API)
- [ ] Mobile-responsive WebSocket variant
- [ ] Docker container + GitHub Actions CI/CD

---

## 👨‍💻 Author

**Dhanush R**  
Oasis Infobyte Java Development Intern  
Project: ATM Interface | Task 3  

- 📧 **Email:** [dhanushrmdy@gmail.com](mailto:dhanushrmdy@gmail.com)  
- 🔗 **LinkedIn:** [Dhanush R](https://www.linkedin.com/in/dhanushr-dev)  
- 🐙 **GitHub:** [dhanushr-dev](https://github.com/dhanushr-dev)

---

## 📄 License

This project is licensed under the MIT License.
