import java.time.LocalDateTime;

class Transaction {
    int transactionId;
    int accountId;
    String type;
    double amount;
    LocalDateTime date;

    public Transaction(int id, int accId, String type, double amount) {
        this.transactionId = id;
        this.accountId = accId;
        this.type = type;
        this.amount = amount;
        this.date = LocalDateTime.now();
    }
}