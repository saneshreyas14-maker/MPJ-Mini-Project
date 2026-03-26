import java.util.*;

class BankDAO {
    HashMap<Integer, Account> accounts = new HashMap<>();
    ArrayList<Transaction> transactions = new ArrayList<>();

    
    public void deposit(int accId, double amount) {
        Account acc = accounts.get(accId);
        if (acc != null && amount > 0) {
            acc.balance += amount;
            transactions.add(new Transaction(transactions.size()+1, accId, "Deposit", amount));
            System.out.println("Deposit Successful!");
        }
    }

    
    public void withdraw(int accId, double amount) {
        Account acc = accounts.get(accId);
        if (acc != null) {
            if (acc.balance >= amount) {
                acc.balance -= amount;
                transactions.add(new Transaction(transactions.size()+1, accId, "Withdraw", amount));
                System.out.println("Withdrawal Successful!");
            } else {
                System.out.println("Insufficient Balance!");
            }
        }
    }

   
    public void transfer(int from, int to, double amount) {
        Account sender = accounts.get(from);
        Account receiver = accounts.get(to);

        try {
            if (sender.balance >= amount) {
                sender.balance -= amount;
                receiver.balance += amount;

                transactions.add(new Transaction(transactions.size()+1, from, "Transfer Debit", amount));
                transactions.add(new Transaction(transactions.size()+1, to, "Transfer Credit", amount));

                System.out.println("Transfer Successful!");
            } else {
                throw new Exception("Insufficient Funds");
            }
        } catch (Exception e) {
            System.out.println("Transaction Failed: " + e.getMessage());
        }
    }

    public void showTransactions(int accId) {
        for (Transaction t : transactions) {
            if (t.accountId == accId) {
                System.out.println(t.type + " | " + t.amount + " | " + t.date);
            }
        }
    }
}