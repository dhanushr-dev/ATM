# 🎬 Demo Video Script — ATM Interface

**Duration:** ~4–5 minutes  
**Tone:** Professional, clear, confident

---

## [0:00 – 0:20] Opening

> "Hi, I'm [Your Name], a Java Development Intern at Oasis Infobyte.
> In this video, I'll walk you through my ATM Interface project —
> a fully functional desktop banking application built with Java 17,
> Swing, MySQL, and JDBC."

*[Show the project folder structure briefly in IDE]*

---

## [0:20 – 0:50] Architecture Overview

> "Before I demo the features, let me quickly explain the architecture.
> I used MVC — the UI layer talks to an ATMController,
> which delegates to Service classes, which call DAO classes,
> which execute PreparedStatements against a MySQL database.
> Every PIN is BCrypt-hashed. No plain-text credentials anywhere."

*[Show the package tree in the IDE]*

---

## [0:50 – 1:30] Login Screen

> "This is the login screen. I'll enter the account number — 1001000000000001 —
> and the PIN — 1234. Notice the field only accepts digits, and the PIN field
> is masked. I'll hit Login."

*[Enter credentials, click Login]*

> "Authentication goes through BCrypt PIN verification.
> If I enter the wrong PIN 3 times, the account gets locked automatically —
> a real ATM security feature."

*[Show wrong PIN attempt, display error message]*

---

## [1:30 – 1:50] Dashboard

> "After login, we're on the Dashboard. On the left is the navigation sidebar
> showing my name, masked account number, and live balance.
> The main area shows quick-action buttons."

---

## [1:50 – 2:20] Deposit

> "Let me deposit money. I'll click Deposit, enter 5000, or use
> one of these quick-select buttons. I hit Deposit — a confirmation dialog
> appears. I confirm, and the transaction is committed to the database.
> The balance updates immediately in the sidebar."

*[Perform deposit, show confirmation, show updated balance]*

---

## [2:20 – 2:50] Withdrawal

> "Now I'll withdraw 2000. I enter the amount, confirm, and it succeeds.
> Watch what happens if I try to withdraw more than my balance..."

*[Try to withdraw amount exceeding balance]*

> "I get a clear error: 'Insufficient funds. Available: X, Requested: Y.'
> The balance is unchanged."

---

## [2:50 – 3:20] Transfer

> "The Transfer feature is the most technically interesting.
> I'll transfer 1000 to account 1001000000000002.
> A warning dialog appears because transfers are irreversible."

*[Perform transfer, show warning dialog, confirm]*

> "Under the hood, this executes four SQL statements — debit my account,
> credit the destination, record two transaction entries —
> all in a single JDBC transaction. If any step fails, everything rolls back."

---

## [3:20 – 3:40] Transaction History & Mini Statement

> "The Transaction History shows all my operations in a sortable table.
> Credits are in green, debits in red."

*[Show history table]*

> "The Mini Statement gives me the last 5 transactions in a receipt-style format."

*[Show mini statement]*

---

## [3:40 – 4:00] Change PIN

> "I can change my PIN by entering my current PIN, then setting a new one.
> After a successful change, the app logs me out automatically — a security requirement."

---

## [4:00 – 4:20] Closing

> "That's the ATM Interface project — fully functional, secure, and production-ready.
> All source code, SQL scripts, and documentation are available on GitHub.
> Thanks for watching!"

*[Show GitHub link / project folder]*

---

## Recording Tips
- Use 1920×1080 screen resolution
- Zoom browser/IDE to 125% for readability
- Use OBS Studio or Loom for recording
- Add background music at -20dB (subtle)
- Add title card at start: "ATM Interface | Oasis Infobyte Java Internship"
- Add chapter markers matching this script's timestamps
