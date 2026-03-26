import java.util.Scanner;

public class BankApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankService service = new BankService();

        while (true) {
            System.out.println("\n1.Create Account 2.Deposit 3.Withdraw 4.Transfer 5.History 6.Exit");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Account ID: ");
                    int accId = sc.nextInt();
                    System.out.print("Enter Customer ID: ");
                    int custId = sc.nextInt();
                    System.out.print("Enter Balance: ");
                    double bal = sc.nextDouble();
                    service.createAccount(accId, custId, bal);
                    break;

                case 2:
                    System.out.print("Account ID: ");
                    service.deposit(sc.nextInt(), sc.nextDouble());
                    break;

                case 3:
                    System.out.print("Account ID: ");
                    service.withdraw(sc.nextInt(), sc.nextDouble());
                    break;

                case 4:
                    System.out.print("From Account: ");
                    int from = sc.nextInt();
                    System.out.print("To Account: ");
                    int to = sc.nextInt();
                    System.out.print("Amount: ");
                    double amt = sc.nextDouble();
                    service.transfer(from, to, amt);
                    break;

                case 5:
                    System.out.print("Account ID: ");
                    service.showHistory(sc.nextInt());
                    break;

                case 6:
                    System.exit(0);
            }
        }
    }
}