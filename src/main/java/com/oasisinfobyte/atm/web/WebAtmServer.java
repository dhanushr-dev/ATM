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
 * Pixel-Perfect Web Server matching the Desktop Swing Interface.
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
        LOGGER.info("Pixel-Perfect Web ATM Application Server running on port " + port);
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
                String maskedAcc = "••••••••••••" + acc.getAccountNumber().substring(Math.max(0, acc.getAccountNumber().length() - 4));
                String json = String.format(Locale.US,
                        "{\"success\":true,\"token\":\"%s\",\"accountNumber\":\"%s\",\"maskedAccount\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"balance\":%.2f,\"accountType\":\"%s\"}",
                        token, acc.getAccountNumber(), maskedAcc, user.getFullName(), user.getEmail(), user.getPhone(), acc.getBalance(), acc.getAccountType());
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
                String maskedAcc = "••••••••••••" + acc.getAccountNumber().substring(Math.max(0, acc.getAccountNumber().length() - 4));
                String json = String.format(Locale.US,
                        "{\"success\":true,\"accountNumber\":\"%s\",\"maskedAccount\":\"%s\",\"fullName\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"balance\":%.2f,\"accountType\":\"%s\",\"status\":\"%s\"}",
                        acc.getAccountNumber(), maskedAcc, user != null ? user.getFullName() : "Valued Customer",
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
                            "{\"ref\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"signedAmount\":\"%s\",\"balanceAfter\":%.2f,\"date\":\"%s\"}",
                            t.getReferenceNumber(), t.getTransactionType().getDisplayName(), t.getAmount(),
                            t.getSignedAmountString(), t.getBalanceAfter(), t.getFormattedDate()));
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
    <title>SecureATM — Desktop Experience</title>
    <link href="https://fonts.googleapis.com/css2?family=Segoe+UI:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        body { background: #081024; color: #E6F2FF; min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        
        /* App Window Frame (Matching 980x660 Swing Desktop Window) */
        .app-window { width: 100vw; height: 100vh; max-width: 1200px; max-height: 720px; background: #081024; display: flex; box-shadow: 0 20px 60px rgba(0,0,0,0.8); overflow: hidden; }
        
        /* LOGIN MODAL FRAME */
        .login-frame { background: #0E1A34; border: 1px solid #19325F; border-radius: 16px; padding: 36px; width: 100%; max-width: 440px; margin: auto; box-shadow: 0 15px 50px rgba(0,0,0,0.7); }
        .login-header { font-size: 26px; font-weight: 700; color: #00C6FF; text-align: center; margin-bottom: 24px; }
        .field-label { font-size: 11px; font-weight: 700; color: #82A5D2; text-transform: uppercase; margin-bottom: 6px; margin-top: 14px; letter-spacing: 0.5px; }
        
        .input-group { position: relative; width: 100%; display: flex; align-items: center; }
        input, select { width: 100%; height: 38px; padding: 0 12px; background: #122241; border: 1px solid #19325F; border-radius: 6px; color: #FFF; font-size: 14px; outline: none; transition: 0.2s; }
        input:focus, select:focus { border-color: #00C6FF; }
        .eye-btn { position: absolute; right: 4px; height: 30px; width: 36px; background: #0E1A34; border: 1px solid #19325F; color: #00C6FF; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 14px; }
        
        .btn-primary { width: 100%; height: 40px; margin-top: 20px; background: #00C6FF; border: none; border-radius: 6px; color: #081024; font-weight: 700; font-size: 13px; cursor: pointer; transition: 0.2s; }
        .btn-primary:hover { background: #00A3D9; }
        .btn-secondary { width: 100%; height: 40px; margin-top: 10px; background: #0E1A34; border: 1px solid #00C6FF; border-radius: 6px; color: #00C6FF; font-weight: 700; font-size: 13px; cursor: pointer; transition: 0.2s; }
        .btn-secondary:hover { background: #00C6FF; color: #081024; }
        .btn-danger { background: #FF4646; color: #FFF; border: none; }
        .btn-danger:hover { background: #D93636; }
        
        /* SIDEBAR (LEFT) */
        .sidebar { width: 220px; background: #0A142A; border-right: 1px solid #19325F; display: flex; flex-direction: column; padding: 20px 14px; }
        .brand { font-size: 18px; font-weight: 700; color: #00C6FF; padding-bottom: 12px; border-bottom: 2px solid #00C6FF; margin-bottom: 20px; }
        
        .user-card { background: #0E1A34; border: 1px solid #19325F; padding: 12px; border-radius: 8px; margin-bottom: 20px; }
        .user-card-name { font-size: 14px; font-weight: 700; color: #FFF; }
        .user-card-acc { font-size: 11px; color: #82A5D2; font-family: monospace; }
        .user-card-bal-title { font-size: 10px; color: #82A5D2; margin-top: 8px; text-transform: uppercase; }
        .user-card-bal { font-size: 16px; font-weight: 700; color: #00C6FF; }
        
        .nav-btn { width: 100%; height: 38px; background: transparent; border: none; border-radius: 6px; color: #82A5D2; font-size: 13px; font-weight: 600; text-align: left; padding: 0 12px; margin-bottom: 4px; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: 0.15s; }
        .nav-btn:hover, .nav-btn.active { background: #001E41; color: #00C6FF; }
        .sidebar-footer { margin-top: auto; border-top: 1px solid #19325F; padding-top: 12px; }
        .clock-lbl { font-size: 11px; color: #466496; margin-bottom: 8px; font-family: monospace; text-align: center; }
        
        /* MAIN WORKSPACE (RIGHT) */
        .workspace { flex: 1; background: #081024; padding: 28px 32px; display: flex; flex-direction: column; overflow-y: auto; }
        .top-hero { background: #0E1A34; border: 1px solid #19325F; border-radius: 12px; padding: 22px 28px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
        .hero-left h1 { font-size: 26px; font-weight: 700; color: #FFF; }
        .hero-left p { font-size: 12px; color: #82A5D2; margin-top: 4px; }
        .hero-left h3 { font-size: 30px; font-weight: 700; color: #00C6FF; margin-top: 6px; }
        .hero-right { background: #0A142A; border: 1px solid #19325F; padding: 14px 20px; border-radius: 8px; text-align: right; }
        .hero-right-label { font-size: 10px; color: #82A5D2; text-transform: uppercase; font-weight: 700; }
        .hero-right-val { font-size: 13px; color: #FFF; font-weight: 600; }
        .hero-right-status { font-size: 12px; color: #00DC6E; font-weight: 700; }
        
        /* 8-GRID QUICK ACTIONS */
        .grid-title { font-size: 14px; font-weight: 700; color: #82A5D2; text-transform: uppercase; margin-bottom: 16px; letter-spacing: 0.5px; }
        .actions-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
        .action-card { background: #0E1A34; border: 1px solid #19325F; border-radius: 10px; padding: 24px 16px; text-align: center; cursor: pointer; transition: 0.2s; position: relative; overflow: hidden; }
        .action-card:hover { transform: translateY(-2px); border-color: #00C6FF; box-shadow: 0 8px 25px rgba(0,198,255,0.2); }
        .action-card-icon { font-size: 26px; margin-bottom: 10px; }
        .action-card-title { font-size: 13px; font-weight: 700; color: #FFF; }
        .action-card-bar { position: absolute; bottom: 0; left: 0; right: 0; height: 3px; }
        
        /* FORM CARDS */
        .form-card { background: #0E1A34; border: 1px solid #19325F; border-radius: 12px; padding: 28px; max-width: 600px; margin: 0 auto; width: 100%; }
        .form-title { font-size: 22px; font-weight: 700; color: #00C6FF; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; }
        .form-sub { font-size: 12px; color: #82A5D2; margin-bottom: 18px; }
        .banner-info { background: #003265; border: 1px solid #00C6FF; color: #00C6FF; font-size: 12px; font-weight: 600; padding: 8px 14px; border-radius: 6px; margin-bottom: 16px; }
        .banner-warn { background: #403000; border: 1px solid #FFC300; color: #FFC300; font-size: 12px; font-weight: 600; padding: 8px 14px; border-radius: 6px; margin-bottom: 16px; }
        
        .quick-row { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
        .chip { padding: 6px 14px; background: #0E1A34; border: 1px solid #00C6FF; color: #00C6FF; font-weight: 600; font-size: 12px; border-radius: 16px; cursor: pointer; transition: 0.15s; }
        .chip:hover { background: #00C6FF; color: #081024; }
        
        .btn-row { display: flex; gap: 10px; margin-top: 24px; }
        .btn-row button { flex: 1; }
        
        /* TABLES */
        table { width: 100%; border-collapse: collapse; margin-top: 12px; background: #0E1A34; border-radius: 8px; overflow: hidden; }
        th, td { padding: 10px 14px; text-align: left; border-bottom: 1px solid #19325F; font-size: 13px; }
        th { color: #00C6FF; background: #0A142A; font-weight: 700; text-transform: uppercase; font-size: 11px; }
        
        .error-lbl { color: #FF4646; font-size: 12px; text-align: center; margin-top: 8px; font-weight: 600; }
        .success-lbl { color: #00DC6E; font-size: 12px; text-align: center; margin-top: 8px; font-weight: 700; }
        
        .modal-overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); align-items: center; justify-content: center; z-index: 1000; }
        .modal-overlay.active { display: flex; }
    </style>
</head>
<body>

    <!-- 1. LOGIN FRAME -->
    <div id="loginFrame" class="login-frame">
        <div class="login-header">🔒 SecureATM</div>
        <div class="field-label">Account Number</div>
        <input type="text" id="loginAcc" placeholder="16-digit account number" maxlength="16" value="1001000000000001">
        
        <div class="field-label">PIN</div>
        <div class="input-group">
            <input type="password" id="loginPin" placeholder="4-digit PIN" maxlength="4" value="1234">
            <button class="eye-btn" onclick="toggleEye('loginPin', this)">👁</button>
        </div>
        
        <div id="loginErr" class="error-lbl"></div>
        <button class="btn-primary" onclick="doLogin()">LOGIN</button>
        <button class="btn-secondary" onclick="openModal('regModal')">➕ OPEN NEW ACCOUNT</button>
    </div>

    <!-- 2. DASHBOARD FRAME -->
    <div id="dashFrame" class="app-window" style="display:none;">
        <!-- SIDEBAR -->
        <div class="sidebar">
            <div class="brand">🔒 SecureATM</div>
            
            <div class="user-card">
                <div id="sideName" class="user-card-name">User Name</div>
                <div id="sideAcc" class="user-card-acc">••••••••••••0001</div>
                <div class="user-card-bal-title">Available Balance</div>
                <div id="sideBal" class="user-card-bal">₹0.00</div>
            </div>
            
            <button class="nav-btn active" onclick="navTo('pnlHome', this)">🏠 Home</button>
            <button class="nav-btn" onclick="navTo('pnlDeposit', this)">💰 Deposit Money</button>
            <button class="nav-btn" onclick="navTo('pnlWithdraw', this)">💵 Withdraw Money</button>
            <button class="nav-btn" onclick="navTo('pnlTransfer', this)">↔ Transfer Money</button>
            <button class="nav-btn" onclick="navTo('pnlBalance', this); loadBalance();">💳 Balance Inquiry</button>
            <button class="nav-btn" onclick="navTo('pnlHistory', this); loadHistory();">📜 Transaction History</button>
            <button class="nav-btn" onclick="navTo('pnlReceipt', this); loadReceipt();">🧾 Mini Statement</button>
            <button class="nav-btn" onclick="navTo('pnlPin', this)">🔑 Change PIN</button>
            
            <div class="sidebar-footer">
                <div id="liveClock" class="clock-lbl">--:--:--</div>
                <button class="btn-secondary" style="margin-top:0; height:34px;" onclick="doLogout()">🔒 Logout</button>
                <button class="btn-secondary btn-danger" style="margin-top:6px; height:34px;" onclick="doLogout()">🚪 Exit ATM</button>
            </div>
        </div>
        
        <!-- WORKSPACE AREA -->
        <div class="workspace">
            <!-- PANEL: HOME DASHBOARD -->
            <div id="pnlHome" class="panel active">
                <div class="top-hero">
                    <div class="hero-left">
                        <p>Welcome back,</p>
                        <h1 id="heroName">Arjun Sharma 👤</h1>
                        <p>Available Balance</p>
                        <h3 id="heroBal">₹25,000.00</h3>
                        <p id="heroDate" style="color:#466496; margin-top:6px; font-size:11px;">Saturday, 22 August 2026</p>
                    </div>
                    <div class="hero-right">
                        <div class="hero-right-label">Account No.</div>
                        <div id="heroAcc" class="hero-right-val">••••••••••••0001</div>
                        <div class="hero-right-label" style="margin-top:8px;">Account Type</div>
                        <div id="heroType" class="hero-right-val">SAVINGS</div>
                        <div class="hero-right-label" style="margin-top:8px;">Status</div>
                        <div class="hero-right-status">● ACTIVE</div>
                    </div>
                </div>
                
                <div class="grid-title">Quick Actions</div>
                <div class="actions-grid">
                    <div class="action-card" onclick="navTo('pnlDeposit', getNavBtn(1))">
                        <div class="action-card-icon">💰</div>
                        <div class="action-card-title">Deposit</div>
                        <div class="action-card-bar" style="background:#00DC6E;"></div>
                    </div>
                    <div class="action-card" onclick="navTo('pnlWithdraw', getNavBtn(2))">
                        <div class="action-card-icon">💵</div>
                        <div class="action-card-title">Withdraw</div>
                        <div class="action-card-bar" style="background:#FF4646;"></div>
                    </div>
                    <div class="action-card" onclick="navTo('pnlTransfer', getNavBtn(3))">
                        <div class="action-card-icon">↔</div>
                        <div class="action-card-title">Transfer</div>
                        <div class="action-card-bar" style="background:#00C6FF;"></div>
                    </div>
                    <div class="action-card" onclick="navTo('pnlBalance', getNavBtn(4)); loadBalance();">
                        <div class="action-card-icon">💳</div>
                        <div class="action-card-title">Balance</div>
                        <div class="action-card-bar" style="background:#A064FF;"></div>
                    </div>
                    <div class="action-card" onclick="navTo('pnlHistory', getNavBtn(5)); loadHistory();">
                        <div class="action-card-icon">📜</div>
                        <div class="action-card-title">History</div>
                        <div class="action-card-bar" style="background:#FFC300;"></div>
                    </div>
                    <div class="action-card" onclick="navTo('pnlReceipt', getNavBtn(6)); loadReceipt();">
                        <div class="action-card-icon">🧾</div>
                        <div class="action-card-title">Mini Stmt</div>
                        <div class="action-card-bar" style="background:#00B4D8;"></div>
                    </div>
                    <div class="action-card" onclick="navTo('pnlPin', getNavBtn(7))">
                        <div class="action-card-icon">🔑</div>
                        <div class="action-card-title">Change PIN</div>
                        <div class="action-card-bar" style="background:#48CAE4;"></div>
                    </div>
                    <div class="action-card" onclick="doLogout()">
                        <div class="action-card-icon">🚪</div>
                        <div class="action-card-title">Logout</div>
                        <div class="action-card-bar" style="background:#82A5D2;"></div>
                    </div>
                </div>
            </div>
            
            <!-- PANEL: DEPOSIT -->
            <div id="pnlDeposit" class="panel">
                <div class="form-card">
                    <div class="form-title">💰 Deposit Money</div>
                    <div class="form-sub">Add funds to your account</div>
                    <div class="banner-info">ℹ Max single deposit: ₹1,00,000 · Min: ₹1</div>
                    
                    <div class="field-label">AMOUNT (₹)</div>
                    <input type="number" id="depAmt" placeholder="Enter deposit amount">
                    
                    <div style="font-size:11px; color:#82A5D2; margin-top:10px;">Quick:</div>
                    <div class="quick-row">
                        <div class="chip" onclick="setAmt('depAmt', 500)">₹500</div>
                        <div class="chip" onclick="setAmt('depAmt', 1000)">₹1000</div>
                        <div class="chip" onclick="setAmt('depAmt', 2000)">₹2000</div>
                        <div class="chip" onclick="setAmt('depAmt', 5000)">₹5000</div>
                        <div class="chip" onclick="setAmt('depAmt', 10000)">₹10000</div>
                        <div class="chip" onclick="setAmt('depAmt', 20000)">₹20000</div>
                    </div>
                    
                    <div id="depMsg"></div>
                    <div class="btn-row">
                        <button class="btn-primary" onclick="doDeposit()">DEPOSIT</button>
                        <button class="btn-secondary" onclick="clearInput('depAmt', 'depMsg')">CLEAR</button>
                        <button class="btn-secondary" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                    </div>
                </div>
            </div>
            
            <!-- PANEL: WITHDRAW -->
            <div id="pnlWithdraw" class="panel">
                <div class="form-card">
                    <div class="form-title">💵 Withdraw Money</div>
                    <div class="form-sub">Withdraw cash from your account</div>
                    <div class="banner-info">ℹ Max per transaction: ₹50,000 · Min: ₹1</div>
                    
                    <div class="field-label">AMOUNT (₹)</div>
                    <input type="number" id="wdAmt" placeholder="Enter withdrawal amount">
                    
                    <div style="font-size:11px; color:#82A5D2; margin-top:10px;">Quick:</div>
                    <div class="quick-row">
                        <div class="chip" onclick="setAmt('wdAmt', 500)">₹500</div>
                        <div class="chip" onclick="setAmt('wdAmt', 1000)">₹1000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 2000)">₹2000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 5000)">₹5000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 10000)">₹10000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 20000)">₹20000</div>
                        <div class="chip" onclick="setAmt('wdAmt', 50000)">₹50000</div>
                    </div>
                    
                    <div id="wdMsg"></div>
                    <div class="btn-row">
                        <button class="btn-primary" onclick="doWithdraw()">WITHDRAW</button>
                        <button class="btn-secondary" onclick="clearInput('wdAmt', 'wdMsg')">CLEAR</button>
                        <button class="btn-secondary" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                    </div>
                </div>
            </div>
            
            <!-- PANEL: TRANSFER -->
            <div id="pnlTransfer" class="panel">
                <div class="form-card">
                    <div class="form-title">↔ Transfer Money</div>
                    <div class="form-sub">Send funds to another account</div>
                    <div class="banner-info">ℹ Transfers are immediate and irreversible · Max: ₹50,000</div>
                    
                    <div class="field-label">DESTINATION ACCOUNT NUMBER (16 DIGITS)</div>
                    <input type="text" id="trAcc" placeholder="16-digit account number" maxlength="16">
                    
                    <div class="field-label">AMOUNT (₹)</div>
                    <input type="number" id="trAmt" placeholder="Enter amount">
                    
                    <div id="trMsg"></div>
                    <div class="btn-row">
                        <button class="btn-primary" onclick="doTransfer()">TRANSFER</button>
                        <button class="btn-secondary" onclick="clearInput('trAcc', 'trMsg'); clearInput('trAmt', null);">CLEAR</button>
                        <button class="btn-secondary" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                    </div>
                </div>
            </div>
            
            <!-- PANEL: BALANCE INQUIRY -->
            <div id="pnlBalance" class="panel">
                <div class="form-card" style="max-width:700px;">
                    <div class="form-title">💳 Balance Inquiry</div>
                    <div class="form-sub">Real-time account balance</div>
                    
                    <div style="background:#0A142A; border:1px solid #19325F; padding:24px; border-radius:10px; margin-bottom:20px;">
                        <div style="font-size:11px; color:#82A5D2; font-weight:700;">AVAILABLE BALANCE</div>
                        <div id="balVal" style="font-size:36px; font-weight:700; color:#00C6FF; margin-top:4px;">₹25,000.00</div>
                    </div>
                    
                    <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:20px;">
                        <div>
                            <div class="field-label">ACCOUNT HOLDER</div>
                            <div id="balHolder" style="font-size:14px; font-weight:700; color:#FFF;">Arjun Sharma</div>
                        </div>
                        <div>
                            <div class="field-label">ACCOUNT NUMBER</div>
                            <div id="balAcc" style="font-size:14px; font-weight:700; color:#82A5D2; font-family:monospace;">••••••••••••0001</div>
                        </div>
                        <div>
                            <div class="field-label">ACCOUNT TYPE</div>
                            <div id="balType" style="font-size:14px; font-weight:700; color:#FFF;">SAVINGS Account</div>
                        </div>
                        <div>
                            <div class="field-label">STATUS</div>
                            <div style="font-size:14px; font-weight:700; color:#00DC6E;">● ACTIVE</div>
                        </div>
                    </div>
                    
                    <div class="btn-row">
                        <button class="btn-primary" onclick="loadBalance()">🔄 REFRESH</button>
                        <button class="btn-secondary" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                    </div>
                </div>
            </div>
            
            <!-- PANEL: HISTORY -->
            <div id="pnlHistory" class="panel">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
                    <div>
                        <div class="form-title" style="margin-bottom:0;">📜 Transaction History</div>
                        <div class="form-sub" style="margin-bottom:0;">All your account transactions</div>
                    </div>
                    <div style="display:flex; gap:8px;">
                        <button class="btn-secondary" style="width:auto; height:34px; padding:0 14px; margin-top:0;" onclick="exportCSV()">📊 Export CSV</button>
                        <button class="btn-primary" style="width:auto; height:34px; padding:0 14px; margin-top:0;" onclick="loadHistory()">🔄 Refresh</button>
                    </div>
                </div>
                
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Date & Time</th>
                            <th>Type</th>
                            <th>Amount</th>
                            <th>Balance After</th>
                            <th>Reference</th>
                        </tr>
                    </thead>
                    <tbody id="txTable">
                        <tr><td colspan="6" style="text-align:center;">Loading history...</td></tr>
                    </tbody>
                </table>
                
                <div style="display:flex; justify-content:space-between; align-items:center; margin-top:16px;">
                    <div id="txCount" style="font-size:12px; color:#82A5D2;">0 transaction(s)</div>
                    <button class="btn-secondary" style="width:auto; height:34px; padding:0 20px; margin-top:0;" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                </div>
            </div>
            
            <!-- PANEL: MINI STATEMENT -->
            <div id="pnlReceipt" class="panel">
                <div class="form-card">
                    <div class="form-title">🧾 Mini Statement</div>
                    <div class="form-sub">Recent transaction receipt</div>
                    
                    <div id="receiptBox" style="background:#0A142A; border:1px dashed #00C6FF; padding:20px; border-radius:8px; font-family:'Consolas', monospace; font-size:13px; color:#E6F2FF; white-space:pre-wrap; margin-bottom:20px;">Loading receipt...</div>
                    
                    <div class="btn-row">
                        <button class="btn-primary" onclick="downloadReceipt()">💾 SAVE RECEIPT</button>
                        <button class="btn-secondary" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                    </div>
                </div>
            </div>
            
            <!-- PANEL: CHANGE PIN -->
            <div id="pnlPin" class="panel">
                <div class="form-card">
                    <div class="form-title">🔑 Change PIN</div>
                    <div class="form-sub">Update your 4-digit security PIN</div>
                    <div class="banner-warn">⚠ Your PIN must be 4 digits. Never share it with anyone.</div>
                    
                    <div class="field-label">CURRENT PIN</div>
                    <div class="input-group">
                        <input type="password" id="pinOld" maxlength="4" placeholder="••••">
                        <button class="eye-btn" onclick="toggleEye('pinOld', this)">👁</button>
                    </div>
                    
                    <div class="field-label">NEW PIN (4 DIGITS)</div>
                    <div class="input-group">
                        <input type="password" id="pinNew" maxlength="4" placeholder="••••">
                        <button class="eye-btn" onclick="toggleEye('pinNew', this)">👁</button>
                    </div>
                    
                    <div class="field-label">CONFIRM NEW PIN</div>
                    <div class="input-group">
                        <input type="password" id="pinConf" maxlength="4" placeholder="••••">
                        <button class="eye-btn" onclick="toggleEye('pinConf', this)">👁</button>
                    </div>
                    
                    <div id="pinMsg"></div>
                    <div class="btn-row">
                        <button class="btn-primary" onclick="doChangePin()">CHANGE PIN</button>
                        <button class="btn-secondary" onclick="clearInput('pinOld', 'pinMsg'); clearInput('pinNew', null); clearInput('pinConf', null);">CLEAR</button>
                        <button class="btn-secondary" onclick="navTo('pnlHome', getNavBtn(0))">← BACK</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- REGISTRATION MODAL -->
    <div id="regModal" class="modal-overlay">
        <div class="login-frame">
            <div class="login-header">🏦 Open New Account</div>
            
            <div class="field-label">Full Name</div>
            <input type="text" id="regName" placeholder="Enter full name">
            
            <div class="field-label">Email Address</div>
            <input type="email" id="regEmail" placeholder="e.g. user@example.com">
            
            <div class="field-label">Mobile Phone (10 Digits)</div>
            <input type="text" id="regPhone" placeholder="10-digit mobile number" maxlength="10">
            
            <div class="field-label">Account Type</div>
            <select id="regType">
                <option value="SAVINGS">SAVINGS</option>
                <option value="CURRENT">CURRENT</option>
            </select>
            
            <div class="field-label">4-Digit PIN</div>
            <div class="input-group">
                <input type="password" id="regPin" placeholder="••••" maxlength="4">
                <button class="eye-btn" onclick="toggleEye('regPin', this)">👁</button>
            </div>
            
            <div class="field-label">Initial Deposit (Min ₹500)</div>
            <input type="number" id="regDep" value="1000">
            
            <div id="regErr" class="error-lbl"></div>
            <button class="btn-primary" onclick="doRegister()">CREATE ACCOUNT</button>
            <button class="btn-secondary" onclick="closeModal('regModal')">CANCEL</button>
        </div>
    </div>

    <script>
        let authToken = '';
        let txData = [];

        function updateClock() {
            const now = new Date();
            const timeStr = now.toTimeString().split(' ')[0];
            const dateStr = now.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
            if (document.getElementById('liveClock')) document.getElementById('liveClock').innerText = dateStr + ' ' + timeStr;
        }
        setInterval(updateClock, 1000);
        updateClock();

        async function apiCall(endpoint, method = 'GET', data = null) {
            const opts = { method, headers: { 'Content-Type': 'application/json' } };
            if (authToken) opts.headers['Authorization'] = 'Bearer ' + authToken;
            if (data) opts.body = JSON.stringify(data);
            const res = await fetch(endpoint, opts);
            return res.json();
        }

        function toggleEye(id, btn) {
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
        function clearInput(id, msgId) {
            if (id) document.getElementById(id).value = '';
            if (msgId) document.getElementById(msgId).innerHTML = '';
        }

        function getNavBtn(index) {
            return document.querySelectorAll('.nav-btn')[index];
        }

        function navTo(panelId, btn) {
            document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
            document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
            document.getElementById(panelId).classList.add('active');
            if (btn) btn.classList.add('active');
        }

        async function doLogin() {
            const acc = document.getElementById('loginAcc').value;
            const pin = document.getElementById('loginPin').value;
            document.getElementById('loginErr').innerText = '';
            const res = await apiCall('/api/login', 'POST', { accountNumber: acc, pin: pin });
            if (res.success) {
                authToken = res.token;
                document.getElementById('sideName').innerText = res.fullName;
                document.getElementById('sideAcc').innerText = res.maskedAccount;
                document.getElementById('sideBal').innerText = '₹' + res.balance.toLocaleString('en-IN', {minimumFractionDigits:2});
                
                document.getElementById('heroName').innerText = res.fullName + ' 👤';
                document.getElementById('heroBal').innerText = '₹' + res.balance.toLocaleString('en-IN', {minimumFractionDigits:2});
                document.getElementById('heroAcc').innerText = res.maskedAccount;
                document.getElementById('heroType').innerText = res.accountType;
                
                document.getElementById('loginFrame').style.display = 'none';
                document.getElementById('dashFrame').style.display = 'flex';
            } else {
                document.getElementById('loginErr').innerText = res.error || 'Login failed';
            }
        }

        async function loadBalance() {
            const res = await apiCall('/api/account');
            if (res.success) {
                const balStr = '₹' + res.balance.toLocaleString('en-IN', {minimumFractionDigits:2});
                document.getElementById('sideBal').innerText = balStr;
                document.getElementById('heroBal').innerText = balStr;
                document.getElementById('balVal').innerText = balStr;
                document.getElementById('balHolder').innerText = res.fullName;
                document.getElementById('balAcc').innerText = res.maskedAccount;
                document.getElementById('balType').innerText = res.accountType + ' Account';
            }
        }

        async function doDeposit() {
            const amt = document.getElementById('depAmt').value;
            const res = await apiCall('/api/deposit', 'POST', { amount: amt });
            if (res.success) {
                loadBalance();
                document.getElementById('depMsg').innerHTML = `<div class="success-lbl">✓ Deposit Successful! Ref: ${res.txRef}</div>`;
                document.getElementById('depAmt').value = '';
            } else {
                document.getElementById('depMsg').innerHTML = `<div class="error-lbl">${res.error}</div>`;
            }
        }

        async function doWithdraw() {
            const amt = document.getElementById('wdAmt').value;
            const res = await apiCall('/api/withdraw', 'POST', { amount: amt });
            if (res.success) {
                loadBalance();
                document.getElementById('wdMsg').innerHTML = `<div class="success-lbl">✓ Withdrawal Successful! Ref: ${res.txRef}</div>`;
                document.getElementById('wdAmt').value = '';
            } else {
                document.getElementById('wdMsg').innerHTML = `<div class="error-lbl">${res.error}</div>`;
            }
        }

        async function doTransfer() {
            const target = document.getElementById('trAcc').value;
            const amt = document.getElementById('trAmt').value;
            const res = await apiCall('/api/transfer', 'POST', { targetAccount: target, amount: amt });
            if (res.success) {
                loadBalance();
                document.getElementById('trMsg').innerHTML = `<div class="success-lbl">✓ Transfer Successful! Ref: ${res.txRef}</div>`;
                document.getElementById('trAmt').value = '';
            } else {
                document.getElementById('trMsg').innerHTML = `<div class="error-lbl">${res.error}</div>`;
            }
        }

        async function loadHistory() {
            const res = await apiCall('/api/history');
            if (res.success) {
                txData = res.transactions;
                document.getElementById('txCount').innerText = txData.length + ' transaction(s)';
                const tbody = document.getElementById('txTable');
                if (txData.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No transactions found.</td></tr>';
                    return;
                }
                tbody.innerHTML = txData.map((t, idx) => `
                    <tr>
                        <td>${idx + 1}</td>
                        <td>${t.date}</td>
                        <td>${t.type}</td>
                        <td style="color:${t.signedAmount.startsWith('+') ? '#00DC6E' : '#FF4646'}; font-weight:700;">${t.signedAmount}</td>
                        <td>₹${t.balanceAfter.toLocaleString('en-IN', {minimumFractionDigits:2})}</td>
                        <td style="font-family:monospace; color:#82A5D2;">${t.ref}</td>
                    </tr>
                `).join('');
            }
        }

        async function loadReceipt() {
            const res = await apiCall('/api/history');
            if (res.success) {
                const list = res.transactions.slice(0, 5);
                let txt = `=================================================\n`;
                txt += `               SECURE ATM RECEIPT                \n`;
                txt += `=================================================\n`;
                txt += `Holder:  ${document.getElementById('sideName').innerText}\n`;
                txt += `Account: ${document.getElementById('sideAcc').innerText}\n`;
                txt += `Balance: ${document.getElementById('sideBal').innerText}\n`;
                txt += `-------------------------------------------------\n`;
                txt += `RECENT TRANSACTIONS:\n`;
                list.forEach(t => {
                    txt += `${t.date} | ${t.type.padEnd(14)} | ${t.signedAmount}\n`;
                });
                txt += `=================================================\n`;
                txt += `        THANK YOU FOR BANKING WITH SECUREATM     \n`;
                txt += `=================================================\n`;
                document.getElementById('receiptBox').innerText = txt;
            }
        }

        function downloadReceipt() {
            const txt = document.getElementById('receiptBox').innerText;
            const blob = new Blob([txt], { type: 'text/plain' });
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'ATM_Receipt.txt';
            a.click();
        }

        function exportCSV() {
            if (txData.length === 0) return alert('No transaction data to export!');
            let csv = 'Index,Date,Type,Amount,BalanceAfter,Reference\\n';
            txData.forEach((t, i) => {
                csv += `${i+1},"${t.date}","${t.type}",${t.amount},${t.balanceAfter},"${t.ref}"\\n`;
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
                document.getElementById('pinMsg').innerHTML = '<div class="success-lbl">✓ PIN changed successfully! Please log in again.</div>';
                setTimeout(doLogout, 2000);
            } else {
                document.getElementById('pinMsg').innerHTML = `<div class="error-lbl">${res.error}</div>`;
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
            document.getElementById('dashFrame').style.display = 'none';
            document.getElementById('loginFrame').style.display = 'block';
        }

        function openModal(id) { document.getElementById(id).classList.add('active'); }
        function closeModal(id) { document.getElementById(id).classList.remove('active'); }
    </script>
</body>
</html>
        """;
    }
}
