import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;

public class BankApiServer {
    // Re-use your existing BankService for business logic where possible
    private static BankService service = new BankService();

    // Setup Local Server
    public static void main(String[] args) throws IOException {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        System.out.println("Starting Java REST API Server on port " + port + "...");

        // Define our REST API Endpoints
        server.createContext("/api/deposit", new DepositHandler());
        server.createContext("/api/withdraw", new WithdrawHandler());
        server.createContext("/api/transfer", new TransferHandler());
        server.createContext("/api/history", new HistoryHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
        
        System.out.println("✅ API Server is running!");
        System.out.println("🔌 Connect your Frontend using HTTP fetch requests to: http://localhost:8081");
        System.out.println("Example: fetch('http://localhost:8081/api/deposit?accId=1&amount=500', { method: 'POST' })");
    }

    // -------------------------------------------------------------
    // Helper Methods for HTTP Server (CORS & Query Parsing)
    // -------------------------------------------------------------
    
    // Parses URL parameters like ?accId=1&amount=500
    public static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    // Sends the JSON HTTP Response and automatically allows CORS for your GitHub Pages website
    public static void respond(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // -------------------------------------------------------------
    // API Endpoint Handlers
    // -------------------------------------------------------------

    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { respond(exchange, 204, ""); return; }
            try {
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int accId = Integer.parseInt(q.get("accId"));
                double amount = Double.parseDouble(q.get("amount"));
                
                service.deposit(accId, amount); // Calls your BankDAO deposit logic
                
                respond(exchange, 200, "{\"status\":\"success\", \"message\":\"Deposited $" + amount + " to account " + accId + "\"}");
            } catch (Exception e) {
                respond(exchange, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { respond(exchange, 204, ""); return; }
            try {
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int accId = Integer.parseInt(q.get("accId"));
                double amount = Double.parseDouble(q.get("amount"));
                
                service.withdraw(accId, amount); // Calls your BankDAO withdraw logic
                
                respond(exchange, 200, "{\"status\":\"success\", \"message\":\"Withdrew $" + amount + " from account " + accId + "\"}");
            } catch (Exception e) {
                respond(exchange, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { respond(exchange, 204, ""); return; }
            try {
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int from = Integer.parseInt(q.get("from"));
                int to = Integer.parseInt(q.get("to"));
                double amount = Double.parseDouble(q.get("amount"));
                
                service.transfer(from, to, amount);
                
                respond(exchange, 200, "{\"status\":\"success\", \"message\":\"Transferred $" + amount + " from " + from + " to " + to + "\"}");
            } catch (Exception e) {
                respond(exchange, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    // We build a custom query here because BankDAO.showTransactions() prints directly to console.
    // We need to return the data as JSON instead for the frontend to read it!
    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) { respond(exchange, 204, ""); return; }
            try {
                Map<String, String> q = parseQuery(exchange.getRequestURI().getQuery());
                int accId = Integer.parseInt(q.get("accId"));

                String jdbcUrl = "jdbc:mysql://localhost:3306/BankManagementSystem";
                String user = "root";
                String pass = "pass123";

                String query = "SELECT type, amount, transaction_date FROM Transactions WHERE accountId = ? ORDER BY transaction_date DESC";
                
                try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    
                    pstmt.setInt(1, accId);
                    ResultSet rs = pstmt.executeQuery();
                    
                    StringBuilder jsonArray = new StringBuilder("[");
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) jsonArray.append(",");
                        jsonArray.append("{");
                        jsonArray.append("\"type\":\"").append(rs.getString("type")).append("\",");
                        jsonArray.append("\"amount\":").append(rs.getDouble("amount")).append(",");
                        jsonArray.append("\"date\":\"").append(rs.getString("transaction_date")).append("\"");
                        jsonArray.append("}");
                        first = false;
                    }
                    jsonArray.append("]");
                    
                    respond(exchange, 200, jsonArray.toString());
                }
            } catch (Exception e) {
                respond(exchange, 400, "{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }
}
