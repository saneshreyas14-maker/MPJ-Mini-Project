class BankService {
    BankDAO dao = new BankDAO();

    public void createAccount(int accId, int custId, double balance) {
        dao.createAccount(accId, custId, balance);
    }

    public void deposit(int accId, double amount) {
        dao.deposit(accId, amount);
    }

    public void withdraw(int accId, double amount) {
        dao.withdraw(accId, amount);
    }

    public void transfer(int from, int to, double amount) {
        dao.transfer(from, to, amount);
    }

    public void showHistory(int accId) {
        dao.showTransactions(accId);
    }
}