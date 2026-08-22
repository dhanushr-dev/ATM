package com.oasisinfobyte.atm.web;

import com.oasisinfobyte.atm.controller.ATMController;
import com.oasisinfobyte.atm.dao.AccountDAO;
import com.oasisinfobyte.atm.dao.TransactionDAO;
import com.oasisinfobyte.atm.dao.UserDAO;
import com.oasisinfobyte.atm.model.Account;
import com.oasisinfobyte.atm.model.Transaction;
import com.oasisinfobyte.atm.model.User;
import com.oasisinfobyte.atm.service.AccountService;
import com.oasisinfobyte.atm.service.AuthService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Premium Web Server for the ATM Interface.
 * Renders a high-end web app matching the Desktop Swing UI.
 */
public class WebAtmServer {

    private static final Logger LOGGER = Logger.getLogger(WebAtmServer.class.getName());

    private final ATMController controller;
    private final Map<String, Account> sessions = new ConcurrentHashMap<>();

    public WebAtmServer() {
        UserDAO userDAO = new UserDAO();
        AccountDAO accountDAO = new AccountDAO();
        TransactionDAO transactionDAO = new TransactionDAO();

        AuthService authService = new AuthService(accountDAO, userDAO);
        AccountService accountService = new AccountService(accountDAO, transactionDAO, authService);
        this.controller = new ATMController(authService, accountService);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/account", new AccountHandler());
        server.createContext("/api/deposit", new DepositHandler());
        server.createContext("/api/withdraw", new WithdrawHandler());
        server.createContext("/api/transfer", new TransferHandler());
        server.createContext("/api/history", new HistoryHandler());
        server.createContext("/api/change-pin", new ChangePinHandler());
        server.createContext("/api/logout", new LogoutHandler());

        server.setExecutor(null);
        server.start();
        LOGGER.info("Premium Web ATM Application Server running on port " + port);
    }

    private class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String html = getWebUIHtml();
                byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Map<String, String> body = parseJson(exchange);
            String accNum = body.get("accountNumber");
            String pin = body.get("pin");

            try {
                Account acc = controller.login(accNum, pin);
                String token = UUID.randomUUID().toString();
                sessions.put(token, acc);

                User user = controller.getCurrentUser();
                String json = String.format(Locale.US,
                        "{\"success\":true,\"token\":\"%s\",\"accountNumber\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"balance\":%.2f,\"accountType\":\"%s\"}",
                        token, acc.getAccountNumber(), user.getFullName(), user.getEmail(), user.getPhone(), acc.getBalance(), acc.getAccountType());
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Map<String, String> body = parseJson(exchange);
            try {
                String name = body.get("name");
                String email = body.get("email");
                String phone = body.get("phone");
                String pin = body.get("pin");
                BigDecimal deposit = new BigDecimal(body.get("deposit"));
                Account.AccountType type = Account.AccountType.valueOf(body.get("accountType").toUpperCase());

                Account newAcc = controller.registerAccount(name, email, phone, pin, deposit, type);
                String json = String.format(Locale.US,
                        "{\"success\":true,\"accountNumber\":\"%s\"}", newAcc.getAccountNumber());
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Account acc = getSessionAccount(exchange);
            if (acc == null) {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Unauthorized\"}");
                return;
            }
            try {
                BigDecimal balance = controller.getBalance();
                User user = controller.getCurrentUser();
                String json = String.format(Locale.US,
                        "{\"success\":true,\"accountNumber\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"balance\":%.2f,\"accountType\":\"%s\",\"status\":\"%s\"}",
                        acc.getAccountNumber(), user != null ? user.getFullName() : "Valued Customer",
                        user != null ? user.getEmail() : "", user != null ? user.getPhone() : "",
                        balance, acc.getAccountType(), acc.getStatus());
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Account acc = getSessionAccount(exchange);
            if (acc == null) {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Unauthorized\"}");
                return;
            }
            Map<String, String> body = parseJson(exchange);
            try {
                Transaction tx = controller.deposit(body.get("amount"));
                String json = String.format(Locale.US,
                        "{\"success\":true,\"newBalance\":%.2f,\"txRef\":\"%s\"}",
                        controller.getBalance(), tx.getReferenceNumber());
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Account acc = getSessionAccount(exchange);
            if (acc == null) {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Unauthorized\"}");
                return;
            }
            Map<String, String> body = parseJson(exchange);
            try {
                Transaction tx = controller.withdraw(body.get("amount"));
                String json = String.format(Locale.US,
                        "{\"success\":true,\"newBalance\":%.2f,\"txRef\":\"%s\"}",
                        controller.getBalance(), tx.getReferenceNumber());
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Account acc = getSessionAccount(exchange);
            if (acc == null) {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Unauthorized\"}");
                return;
            }
            Map<String, String> body = parseJson(exchange);
            try {
                Transaction tx = controller.transfer(body.get("targetAccount"), body.get("amount"));
                String json = String.format(Locale.US,
                        "{\"success\":true,\"newBalance\":%.2f,\"txRef\":\"%s\"}",
                        controller.getBalance(), tx.getReferenceNumber());
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Account acc = getSessionAccount(exchange);
            if (acc == null) {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Unauthorized\"}");
                return;
            }
            try {
                List<Transaction> list = controller.getTransactionHistory();
                StringBuilder sb = new StringBuilder("{\"success\":true,\"transactions\":[");
                for (int i = 0; i < list.size(); i++) {
                    Transaction t = list.get(i);
                    if (i > 0) sb.append(",");
                    sb.append(String.format(Locale.US,
                            "{\"ref\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"balanceAfter\":%.2f,\"date\":\"%s\"}",
                            t.getReferenceNumber(), t.getTransactionType().getDisplayName(), t.getAmount(), t.getBalanceAfter(), t.getFormattedDate()));
                }
                sb.append("]}");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class ChangePinHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Account acc = getSessionAccount(exchange);
            if (acc == null) {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Unauthorized\"}");
                return;
            }
            Map<String, String> body = parseJson(exchange);
            try {
                controller.changePin(body.get("currentPin"), body.get("newPin"), body.get("confirmPin"));
                sendJson(exchange, 200, "{\"success\":true}");
            } catch (Exception e) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = getBearerToken(exchange);
            if (token != null) sessions.remove(token);
            try { controller.logout(); } catch (Exception ignored) {}
            sendJson(exchange, 200, "{\"success\":true}");
        }
    }

    private Account getSessionAccount(HttpExchange exchange) {
        String token = getBearerToken(exchange);
        return token != null ? sessions.get(token) : null;
    }

    private String getBearerToken(HttpExchange exchange) {
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private Map<String, String> parseJson(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
            String[] pairs = json.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replaceAll("^\"|\"$", "");
                    String v = kv[1].trim().replaceAll("^\"|\"$", "");
                    map.put(k, v);
                }
            }
        }
        return map;
    }

    private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }

    private String getWebUIHtml() {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SecureATM — Enterprise Web Banking</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', sans-serif; }
        body { background: #081024; color: #E6F2FF; min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 20px; }
        
        .card { background: #0E1A34; border: 1px solid #19325F; border-radius: 16px; padding: 32px; width: 100%; max-width: 480px; box-shadow: 0 12px 40px rgba(0,0,0,0.6); }
        .title { font-size: 26px; font-weight: 700; color: #00C6FF; text-align: center; margin-bottom: 24px; display: flex; align-items: center; justify-content: center; gap: 10px; }
        .label { font-size: 11px; font-weight: 700; color: #82A5D2; text-transform: uppercase; margin-bottom: 6px; margin-top: 14px; letter-spacing: 0.5px; }
        
        .input-group { position: relative; width: 100%; }
        input, select { width: 100%; padding: 12px 14px; background: #122241; border: 1px solid #19325F; border-radius: 8px; color: #FFF; font-size: 14px; outline: none; transition: 0.2s; }
        input:focus, select:focus { border-color: #00C6FF; box-shadow: 0 0 10px rgba(0,198,255,0.3); }
        .toggle-btn { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #00C6FF; cursor: pointer; font-size: 16px; padding: 4px; }
        
        .btn { width: 100%; padding: 13px; margin-top: 20px; background: linear-gradient(135deg, #00C6FF, #008CC8); border: none; border-radius: 8px; color: #081024; font-weight: 700; font-size: 14px; cursor: pointer; transition: 0.2s; letter-spacing: 0.5px; }
        .btn:hover { opacity: 0.95; transform: translateY(-1px); box-shadow: 0 4px 15px rgba(0,198,255,0.4); }
        .btn-sec { background: transparent; border: 1px solid #00C6FF; color: #00C6FF; margin-top: 12px; }
        .btn-sec:hover { background: rgba(0,198,255,0.1); }
        .btn-danger { background: #FF4646; color: #FFF; }
        .btn-danger:hover { background: #E03535; }
        
        .quick-row { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
        .chip { flex: 1; min-width: 70px; padding: 8px; background: #122241; border: 1px solid #19325F; color: #00C6FF; font-weight: 600; font-size: 13px; border-radius: 6px; text-align: center; cursor: pointer; transition: 0.2s; }
        .chip:hover { background: #001E41; border-color: #00C6FF; }
        
        .error { color: #FF4646; font-size: 13px; margin-top: 10px; text-align: center; font-weight: 500; }
        .success { color: #00DC6E; font-size: 13px; margin-top: 10px; text-align: center; font-weight: 600; }
        
        /* Dashboard Container */
        .dash-container { display: none; width: 100%; max-width: 980px; background: #0E1A34; border: 1px solid #19325F; border-radius: 16px; overflow: hidden; box-shadow: 0 15px 50px rgba(0,0,0,0.7); }
        .dash-header { background: #0A142A; padding: 22px 32px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #19325F; }
        .user-info h2 { color: #FFF; font-size: 20px; font-weight: 700; }
        .user-info p { color: #82A5D2; font-size: 13px; margin-top: 2px; }
        .bal-badge { background: #122241; border: 1px solid #00C6FF; padding: 12px 22px; border-radius: 12px; text-align: right; box-shadow: 0 0 15px rgba(0,198,255,0.15); }
        .bal-badge span { font-size: 11px; color: #82A5D2; font-weight: 600; letter-spacing: 0.5px; display: block; }
        .bal-badge strong { font-size: 22px; color: #00DC6E; font-weight: 700; }
        
        .dash-body { display: flex; min-height: 520px; }
        .sidebar { width: 230px; background: #0A142A; border-right: 1px solid #19325F; padding: 18px 12px; display: flex; flex-direction: column; }
        .nav-item { display: flex; align-items: center; gap: 10px; width: 100%; text-align: left; padding: 13px 16px; margin-bottom: 6px; background: transparent; border: none; color: #82A5D2; font-size: 14px; font-weight: 500; border-radius: 8px; cursor: pointer; transition: 0.2s; }
        .nav-item:hover, .nav-item.active { background: #001E41; color: #00C6FF; font-weight: 600; }
        .content { flex: 1; padding: 32px; background: #0E1A34; }
        .panel { display: none; }
        .panel.active { display: block; }
        
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #19325F; font-size: 13px; }
        th { color: #00C6FF; background: #0A142A; font-weight: 600; }
        
        .receipt-box { background: #122241; border: 1px dashed #00C6FF; padding: 24px; border-radius: 12px; font-family: 'Consolas', monospace; color: #E6F2FF; margin-top: 15px; }
        
        .modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.8); align-items: center; justify-content: center; padding: 20px; z-index: 100; }
        .modal.active { display: flex; }
    </style>
</head>
<body>

    <!-- LOGIN FORM -->
    <div id="loginCard" class="card">
        <div class="title">🏦 SecureATM Login</div>
        <div class="label">16-Digit Account Number</div>
        <input type="text" id="loginAcc" placeholder="e.g. 1001000000000001" maxlength="16">
        
        <div class="label">4-Digit PIN</div>
        <div class="input-group">
            <input type="password" id="loginPin" placeholder="••••" maxlength="4">
            <button class="toggle-btn" onclick="togglePin('loginPin', this)">👁</button>
        </div>
        
        <div id="loginErr" class="error"></div>
        <button class="btn" onclick="doLogin()">LOGIN TO ATM</button>
        <button class="btn btn-sec" onclick="openModal('regModal')">➕ OPEN NEW ACCOUNT</button>
    </div>

    <!-- DASHBOARD -->
    <div id="dashContainer" class="dash-container">
        <div class="dash-header">
            <div class="user-info">
                <h2 id="userName">User</h2>
                <p id="userAcc">Acc: --------</p>
            </div>
            <div class="bal-badge">
                <span>AVAILABLE BALANCE</span>
                <strong id="userBal">₹0.00</strong>
            </div>
        </div>
        <div class="dash-body">
            <div class="sidebar">
                <button class="nav-item active" onclick="showTab('tabSummary', this)">🏠 Account Summary</button>
                <button class="nav-item" onclick="showTab('tabDeposit', this)">💰 Deposit Cash</button>
                <button class="nav-item" onclick="showTab('tabWithdraw', this)">💵 Withdraw Cash</button>
                <button class="nav-item" onclick="showTab('tabTransfer', this)">💸 Fund Transfer</button>
                <button class="nav-item" onclick="showTab('tabHistory', this); loadHistory();">📜 Transaction History</button>
                <button class="nav-item" onclick="showTab('tabReceipt', this); loadReceipt();">🧾 Mini Statement</button>
                <button class="nav-item" onclick="showTab('tabPin', this)">🔑 Change PIN</button>
                <button class="nav-item btn-danger" style="margin-top:auto;" onclick="doLogout()">🚪 Logout</button>
            </div>
            <div class="content">
                <!-- ACCOUNT SUMMARY -->
                <div id="tabSummary" class="panel active">
                    <h3 style="color:#00C6FF; margin-bottom:15px;">Welcome to SecureATM</h3>
                    <div style="background:#122241; border:1px solid #19325F; padding:20px; border-radius:12px;">
                        <p style="color:#82A5D2; font-size:13px; margin-bottom:8px;">Account Holder: <b style="color:#FFF;" id="sumName">-</b></p>
                        <p style="color:#82A5D2; font-size:13px; margin-bottom:8px;">Account Number: <b style="color:#00C6FF;" id="sumAcc">-</b></p>
                        <p style="color:#82A5D2; font-size:13px; margin-bottom:8px;">Account Type: <b style="color:#FFF;" id="sumType">-</b></p>
                        <p style="color:#82A5D2; font-size:13px; margin-bottom:8px;">Account Status: <b style="color:#00DC6E;" id="sumStatus">ACTIVE</b></p>
                        <p style="color:#82A5D2; font-size:13px;">Current Balance: <b style="color:#00DC6E; font-size:18px;" id="sumBal">₹0.00</b></p>
                    </div>
                </div>

                <!-- DEPOSIT -->
                <div id="tabDeposit" class="panel">
                    <h3 style="color:#00C6FF;">Deposit Cash</h3>
                    <div class="label">Amount (₹)</div>
                    <input type="number" id="depAmt" placeholder="Enter deposit amount">
                    <div class="quick-row">
                        <div class="chip" onclick="setAmt('depAmt', 500)">+ ₹500</div>
                        <div class="chip" onclick="setAmt('depAmt', 1000)">+ ₹1,000</div>
                        <div class="chip" onclick="setAmt('depAmt', 2000)">+ ₹2,000</div>
                        <div class="chip" onclick="setAmt('depAmt', 5000)">+ ₹5,000</div>
                    </div>
                    <div id="depMsg"></div>
                    <button class="btn" onclick="doDeposit()">CONFIRM DEPOSIT</button>
                </div>

                <!-- WITHDRAW -->
                <div id="tabWithdraw" class="panel">
                    <h3 style="color:#00C6FF;">Withdraw Cash</h3>
                    <div class="label">Amount (₹)</div>
                    <input type="number" id="wdAmt" placeholder="Enter withdrawal amount">
                    <div class="quick-row">
                        <div class="chip" onclick="setAmt('wdAmt', 500)">₹500</div>
                        <div class="chip" onclick="setAmt('wdAmt', 1000)">₹1,000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 2000)">₹2,000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 5000)">₹5,000</div>
                    </div>
                    <div id="wdMsg"></div>
                    <button class="btn" onclick="doWithdraw()">CONFIRM WITHDRAWAL</button>
                </div>

                <!-- TRANSFER -->
                <div id="tabTransfer" class="panel">
                    <h3 style="color:#00C6FF;">Fund Transfer</h3>
                    <div class="label">Target Account Number (16 Digits)</div>
                    <input type="text" id="trAcc" placeholder="e.g. 1001000000000002" maxlength="16">
                    <div class="label">Transfer Amount (₹)</div>
                    <input type="number" id="trAmt" placeholder="Enter amount">
                    <div id="trMsg"></div>
                    <button class="btn" onclick="doTransfer()">CONFIRM TRANSFER</button>
                </div>

                <!-- HISTORY -->
                <div id="tabHistory" class="panel">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <h3 style="color:#00C6FF;">Transaction History</h3>
                        <button class="chip" style="width:auto; padding:6px 14px;" onclick="exportCSV()">📊 Export CSV</button>
                    </div>
                    <table>
                        <thead>
                            <tr><th>Date</th><th>Ref</th><th>Type</th><th>Amount</th><th>Balance</th></tr>
                        </thead>
                        <tbody id="txTable">
                            <tr><td colspan="5">Loading history...</td></tr>
                        </tbody>
                    </table>
                </div>

                <!-- MINI STATEMENT -->
                <div id="tabReceipt" class="panel">
                    <h3 style="color:#00C6FF;">Mini Statement Receipt</h3>
                    <div id="receiptBox" class="receipt-box">Loading statement...</div>
                </div>

                <!-- CHANGE PIN -->
                <div id="tabPin" class="panel">
                    <h3 style="color:#00C6FF;">Change 4-Digit PIN</h3>
                    <div class="label">Current PIN</div>
                    <div class="input-group">
                        <input type="password" id="pinOld" maxlength="4">
                        <button class="toggle-btn" onclick="togglePin('pinOld', this)">👁</button>
                    </div>
                    
                    <div class="label">New PIN</div>
                    <div class="input-group">
                        <input type="password" id="pinNew" maxlength="4">
                        <button class="toggle-btn" onclick="togglePin('pinNew', this)">👁</button>
                    </div>
                    
                    <div class="label">Confirm New PIN</div>
                    <div class="input-group">
                        <input type="password" id="pinConf" maxlength="4">
                        <button class="toggle-btn" onclick="togglePin('pinConf', this)">👁</button>
                    </div>
                    
                    <div id="pinMsg"></div>
                    <button class="btn" onclick="doChangePin()">UPDATE PIN</button>
                </div>
            </div>
        </div>
    </div>

    <!-- REGISTRATION MODAL -->
    <div id="regModal" class="modal">
        <div class="card">
            <div class="title">🏦 Open New Account</div>
            <div class="label">Full Name</div>
            <input type="text" id="regName">
            <div class="label">Email Address</div>
            <input type="email" id="regEmail">
            <div class="label">Mobile Phone (10 Digits)</div>
            <input type="text" id="regPhone" maxlength="10">
            <div class="label">Account Type</div>
            <select id="regType">
                <option value="SAVINGS">SAVINGS</option>
                <option value="CURRENT">CURRENT</option>
            </select>
            <div class="label">4-Digit PIN</div>
            <div class="input-group">
                <input type="password" id="regPin" maxlength="4">
                <button class="toggle-btn" onclick="togglePin('regPin', this)">👁</button>
            </div>
            
            <div class="label">Initial Deposit (Min ₹500)</div>
            <input type="number" id="regDep" value="1000">
            <div id="regErr" class="error"></div>
            <button class="btn" onclick="doRegister()">CREATE ACCOUNT</button>
            <button class="btn btn-sec" onclick="closeModal('regModal')">CANCEL</button>
        </div>
    </div>

    <script>
        let authToken = '';
        let txData = [];

        async function apiCall(endpoint, method = 'GET', data = null) {
            const opts = { method, headers: { 'Content-Type': 'application/json' } };
            if (authToken) opts.headers['Authorization'] = 'Bearer ' + authToken;
            if (data) opts.body = JSON.stringify(data);
            const res = await fetch(endpoint, opts);
            return res.json();
        }

        function togglePin(id, btn) {
            const input = document.getElementById(id);
            if (input.type === 'password') {
                input.type = 'text';
                btn.innerText = '🙈';
            } else {
                input.type = 'password';
                btn.innerText = '👁';
            }
        }

        function setAmt(id, val) { document.getElementById(id).value = val; }

        async function doLogin() {
            const acc = document.getElementById('loginAcc').value;
            const pin = document.getElementById('loginPin').value;
            document.getElementById('loginErr').innerText = '';
            const res = await apiCall('/api/login', 'POST', { accountNumber: acc, pin: pin });
            if (res.success) {
                authToken = res.token;
                document.getElementById('userName').innerText = res.fullName;
                document.getElementById('userAcc').innerText = 'Acc: ' + res.accountNumber;
                document.getElementById('userBal').innerText = '₹' + res.balance.toFixed(2);
                
                document.getElementById('sumName').innerText = res.fullName;
                document.getElementById('sumAcc').innerText = res.accountNumber;
                document.getElementById('sumType').innerText = res.accountType;
                document.getElementById('sumBal').innerText = '₹' + res.balance.toFixed(2);

                document.getElementById('loginCard').style.display = 'none';
                document.getElementById('dashContainer').style.display = 'block';
            } else {
                document.getElementById('loginErr').innerText = res.error || 'Login failed';
            }
        }

        async function doDeposit() {
            const amt = document.getElementById('depAmt').value;
            const res = await apiCall('/api/deposit', 'POST', { amount: amt });
            if (res.success) {
                document.getElementById('userBal').innerText = '₹' + res.newBalance.toFixed(2);
                document.getElementById('sumBal').innerText = '₹' + res.newBalance.toFixed(2);
                document.getElementById('depMsg').innerHTML = `<div class="success">✓ Deposit Successful! Ref: ${res.txRef}</div>`;
                document.getElementById('depAmt').value = '';
            } else {
                document.getElementById('depMsg').innerHTML = `<div class="error">${res.error}</div>`;
            }
        }

        async function doWithdraw() {
            const amt = document.getElementById('wdAmt').value;
            const res = await apiCall('/api/withdraw', 'POST', { amount: amt });
            if (res.success) {
                document.getElementById('userBal').innerText = '₹' + res.newBalance.toFixed(2);
                document.getElementById('sumBal').innerText = '₹' + res.newBalance.toFixed(2);
                document.getElementById('wdMsg').innerHTML = `<div class="success">✓ Withdrawal Successful! Ref: ${res.txRef}</div>`;
                document.getElementById('wdAmt').value = '';
            } else {
                document.getElementById('wdMsg').innerHTML = `<div class="error">${res.error}</div>`;
            }
        }

        async function doTransfer() {
            const target = document.getElementById('trAcc').value;
            const amt = document.getElementById('trAmt').value;
            const res = await apiCall('/api/transfer', 'POST', { targetAccount: target, amount: amt });
            if (res.success) {
                document.getElementById('userBal').innerText = '₹' + res.newBalance.toFixed(2);
                document.getElementById('sumBal').innerText = '₹' + res.newBalance.toFixed(2);
                document.getElementById('trMsg').innerHTML = `<div class="success">✓ Transfer Successful! Ref: ${res.txRef}</div>`;
                document.getElementById('trAmt').value = '';
            } else {
                document.getElementById('trMsg').innerHTML = `<div class="error">${res.error}</div>`;
            }
        }

        async function loadHistory() {
            const res = await apiCall('/api/history');
            if (res.success) {
                txData = res.transactions;
                const tbody = document.getElementById('txTable');
                if (txData.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="5">No transactions found.</td></tr>';
                    return;
                }
                tbody.innerHTML = txData.map(t => `
                    <tr>
                        <td>${t.date}</td>
                        <td>${t.ref}</td>
                        <td style="color:${t.type.includes('Deposit') || t.type.includes('In') ? '#00DC6E' : '#FF4646'};">${t.type}</td>
                        <td>₹${t.amount.toFixed(2)}</td>
                        <td>₹${t.balanceAfter.toFixed(2)}</td>
                    </tr>
                `).join('');
            }
        }

        async function loadReceipt() {
            const res = await apiCall('/api/history');
            if (res.success) {
                const list = res.transactions.slice(0, 5);
                let txt = `========================================\n`;
                txt += `           SECURE ATM RECEIPT           \n`;
                txt += `========================================\n`;
                txt += `Account: ${document.getElementById('sumAcc').innerText}\n`;
                txt += `Holder:  ${document.getElementById('sumName').innerText}\n`;
                txt += `Balance: ${document.getElementById('sumBal').innerText}\n`;
                txt += `----------------------------------------\n`;
                txt += `RECENT TRANSACTIONS:\n`;
                list.forEach(t => {
                    txt += `${t.date} | ${t.type.padEnd(12)} | ₹${t.amount.toFixed(2)}\n`;
                });
                txt += `========================================\n`;
                txt += `     THANK YOU FOR BANKING WITH US      \n`;
                txt += `========================================\n`;
                document.getElementById('receiptBox').innerText = txt;
            }
        }

        function exportCSV() {
            if (txData.length === 0) return alert('No transaction data to export!');
            let csv = 'Date,Reference,Type,Amount,BalanceAfter\\n';
            txData.forEach(t => {
                csv += `"${t.date}","${t.ref}","${t.type}",${t.amount},${t.balanceAfter}\\n`;
            });
            const blob = new Blob([csv], { type: 'text/csv' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'ATM_Transaction_History.csv';
            a.click();
        }

        async function doChangePin() {
            const oldP = document.getElementById('pinOld').value;
            const newP = document.getElementById('pinNew').value;
            const confP = document.getElementById('pinConf').value;
            const res = await apiCall('/api/change-pin', 'POST', { currentPin: oldP, newPin: newP, confirmPin: confP });
            if (res.success) {
                document.getElementById('pinMsg').innerHTML = '<div class="success">✓ PIN changed successfully! Please log in again.</div>';
                setTimeout(doLogout, 2000);
            } else {
                document.getElementById('pinMsg').innerHTML = `<div class="error">${res.error}</div>`;
            }
        }

        async function doRegister() {
            const data = {
                name: document.getElementById('regName').value,
                email: document.getElementById('regEmail').value,
                phone: document.getElementById('regPhone').value,
                accountType: document.getElementById('regType').value,
                pin: document.getElementById('regPin').value,
                deposit: document.getElementById('regDep').value
            };
            const res = await apiCall('/api/register', 'POST', data);
            if (res.success) {
                alert(`✓ Account Created Successfully!\nYour New Account Number: ${res.accountNumber}\nUse this account number and your PIN to login.`);
                closeModal('regModal');
                document.getElementById('loginAcc').value = res.accountNumber;
            } else {
                document.getElementById('regErr').innerText = res.error || 'Registration failed';
            }
        }

        function doLogout() {
            apiCall('/api/logout', 'POST');
            authToken = '';
            document.getElementById('dashContainer').style.display = 'none';
            document.getElementById('loginCard').style.display = 'block';
        }

        function showTab(tabId, btn) {
            document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
            document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
            document.getElementById(tabId).classList.add('active');
            btn.classList.add('active');
        }

        function openModal(id) { document.getElementById(id).classList.add('active'); }
        function closeModal(id) { document.getElementById(id).classList.remove('active'); }
    </script>
</body>
</html>
        """;
    }
}
