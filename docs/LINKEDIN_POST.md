# LinkedIn Post — ATM Interface Project

---

🏧 **Excited to share my latest project: ATM Interface — built during my Java Development Internship at Oasis Infobyte!**

Over the past few weeks, I designed and built a fully functional, enterprise-grade ATM simulation from scratch using:

🔷 **Java 17** | **Java Swing** | **MySQL** | **JDBC** | **Maven**

---

### 💡 What the application does:

✅ Secure Login with BCrypt PIN hashing & account lockout after 3 failed attempts
✅ Deposit, Withdrawal, and Balance Inquiry with real-time validation
✅ Atomic Fund Transfer — both debit & credit in a single JDBC transaction
✅ Full Transaction History in a sortable table
✅ Mini Statement in receipt format
✅ Change PIN with current-PIN verification & auto logout
✅ Professional dark-themed UI with confirmation dialogs

---

### 🏗️ Architecture Highlights:

📦 MVC + Layered Architecture (Controller → Service → DAO → DB)
🔐 BCrypt for PIN security — never store plain-text credentials
🛡️ PreparedStatements everywhere — zero SQL injection risk
♻️ Try-with-resources for zero connection leaks
⚡ SwingWorker for non-blocking DB operations
🧪 JUnit 5 + Mockito unit tests

---

### 📚 What I learned:

- How to design a proper relational database schema with FK constraints & indexes
- JDBC transaction management (commit/rollback) for atomicity
- Why BCrypt is superior to SHA/MD5 for password/PIN storage
- Building maintainable code with SOLID principles
- Separating UI concerns from business logic in Swing applications

---

This project pushed me to think like an architect, not just a coder.  
Every design decision had a reason behind it — from the exception hierarchy to the CardLayout navigation.

🚀 Check it out on GitHub: [link]

---

**#Java #JDBC #MySQL #Swing #MVC #OasisInfobyte #JavaInternship #SoftwareDevelopment #OpenToWork #BackendDevelopment #100DaysOfCode**
