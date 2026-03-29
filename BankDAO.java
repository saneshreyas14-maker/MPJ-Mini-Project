import java.sql.*;

class BankDAO {
    // Load MySQL JDBC Driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: MySQL JDBC Driver not found. Please add mysql-connector-java JAR to classpath.");
            e.printStackTrace();
        }
    }

    // Database credentials setup
    private static final String URL = "jdbc:mysql://localhost:3306/BankManagementSystem";
    private static final String USER = "root";
    private static final String PASS = "pass123";

    // Helper method to establish a connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public void createAccount(int accId, int custId, double balance) {
        String query = "INSERT INTO Accounts (accountId, customerId, balance) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, accId);
            pstmt.setInt(2, custId);
            pstmt.setDouble(3, balance);
            pstmt.executeUpdate();
            System.out.println("Account Created Successfully in Database!");
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    public void deposit(int accId, double amount) {
        if (amount <= 0) {
            System.out.println("Invalid deposit amount.");
            return;
        }

        String updateAcc = "UPDATE Accounts SET balance = balance + ? WHERE accountId = ?";
        String insertTrans = "INSERT INTO Transactions (accountId, type, amount) VALUES (?, 'Deposit', ?)";

        try (Connection conn = getConnection();
                PreparedStatement pAcc = conn.prepareStatement(updateAcc);
                PreparedStatement pTrans = conn.prepareStatement(insertTrans)) {

            // Update Balance
            pAcc.setDouble(1, amount);
            pAcc.setInt(2, accId);
            int rowsAffected = pAcc.executeUpdate();

            if (rowsAffected > 0) {
                // Log Transaction
                pTrans.setInt(1, accId);
                pTrans.setDouble(2, amount);
                pTrans.executeUpdate();
                System.out.println("Deposit Successful!");
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error during deposit: " + e.getMessage());
        }
    }

    public void withdraw(int accId, double amount) {
        String checkBal = "SELECT balance FROM Accounts WHERE accountId = ?";
        String updateAcc = "UPDATE Accounts SET balance = balance - ? WHERE accountId = ?";
        String insertTrans = "INSERT INTO Transactions (accountId, type, amount) VALUES (?, 'Withdraw', ?)";

        try (Connection conn = getConnection();
                PreparedStatement pCheck = conn.prepareStatement(checkBal);
                PreparedStatement pAcc = conn.prepareStatement(updateAcc);
                PreparedStatement pTrans = conn.prepareStatement(insertTrans)) {

            // Check Balance
            pCheck.setInt(1, accId);
            ResultSet rs = pCheck.executeQuery();
            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                if (currentBalance >= amount) {
                    // Update Balance
                    pAcc.setDouble(1, amount);
                    pAcc.setInt(2, accId);
                    pAcc.executeUpdate();

                    // Log Transaction
                    pTrans.setInt(1, accId);
                    pTrans.setDouble(2, amount);
                    pTrans.executeUpdate();
                    System.out.println("Withdrawal Successful!");
                } else {
                    System.out.println("Insufficient Balance!");
                }
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error during withdrawal: " + e.getMessage());
        }
    }

    public void transfer(int from, int to, double amount) {
        String checkBal = "SELECT balance FROM Accounts WHERE accountId = ?";
        String updateSender = "UPDATE Accounts SET balance = balance - ? WHERE accountId = ?";
        String updateReceiver = "UPDATE Accounts SET balance = balance + ? WHERE accountId = ?";
        String insertTransSender = "INSERT INTO Transactions (accountId, type, amount) VALUES (?, 'Transfer Debit', ?)";
        String insertTransReceiver = "INSERT INTO Transactions (accountId, type, amount) VALUES (?, 'Transfer Credit', ?)";

        try (Connection conn = getConnection()) {
            // Start Transaction to ensure data integrity
            conn.setAutoCommit(false);

            try (PreparedStatement pCheck = conn.prepareStatement(checkBal);
                    PreparedStatement pSender = conn.prepareStatement(updateSender);
                    PreparedStatement pReceiver = conn.prepareStatement(updateReceiver);
                    PreparedStatement pTransS = conn.prepareStatement(insertTransSender);
                    PreparedStatement pTransR = conn.prepareStatement(insertTransReceiver)) {

                // 1. Check if sender has enough balance
                pCheck.setInt(1, from);
                ResultSet rs = pCheck.executeQuery();
                if (!rs.next() || rs.getDouble("balance") < amount) {
                    throw new SQLException("Insufficient Funds or Sender Account not found.");
                }

                // 2. Debit Sender
                pSender.setDouble(1, amount);
                pSender.setInt(2, from);
                pSender.executeUpdate();

                // 3. Credit Receiver
                pReceiver.setDouble(1, amount);
                pReceiver.setInt(2, to);
                int receiverRows = pReceiver.executeUpdate();
                if (receiverRows == 0) {
                    throw new SQLException("Receiver Account not found.");
                }

                // 4. Log Transactions
                pTransS.setInt(1, from);
                pTransS.setDouble(2, amount);
                pTransS.executeUpdate();

                pTransR.setInt(1, to);
                pTransR.setDouble(2, amount);
                pTransR.executeUpdate();

                // Commit the transaction since everything passed
                conn.commit();
                System.out.println("Transfer Successful!");

            } catch (SQLException e) {
                conn.rollback(); // Undo changes if something failed
                System.out.println("Transaction Failed: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true); // Restore default
            }
        } catch (SQLException e) {
            System.out.println("Database error during transfer: " + e.getMessage());
        }
    }

    public void showTransactions(int accId) {
        String query = "SELECT type, amount, transaction_date FROM Transactions WHERE accountId = ? ORDER BY transaction_date DESC";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, accId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Transaction History for Account ID: " + accId);
            System.out.println("-------------------------------------------------");
            while (rs.next()) {
                String type = rs.getString("type");
                double amt = rs.getDouble("amount");
                String date = rs.getString("transaction_date");
                System.out.println(type + " | " + amt + " | " + date);
            }
            System.out.println("-------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error fetching history: " + e.getMessage());
        }
    }
}