package banking;
import org.sqlite.SQLiteDataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;




public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String url = "jdbc:sqlite:" + args[1];
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        menu1();
        String input1 = null;
        createTable(dataSource);
        String input2 = null;
        outerLoop:
        while (scan.hasNext() && !(input1 = scan.next()).equals("0")){

            switch (input1){

                case "1":
                    AccountNumber accountNumber = new AccountNumber();
                    String sAccountNumber = accountNumber.generateCardNumber();
                    Password password = new Password();
                    String sPassword = String.valueOf(password.passGenerator());
                    displayCardAndPin(sAccountNumber, sPassword);
                    insertCardToDatabase(dataSource,sAccountNumber, sPassword);
                    System.out.println();
                    menu1();
                    break ;
                case "2":
                    System.out.println("Enter your card number:");
                    String accNumber = scan.next();
                    System.out.println("Enter your PIN:");
                    String accPass = scan.next();
                    if(authentication(dataSource, accNumber,accPass)){
                        System.out.println();
                        System.out.println("You have successfully logged in!");
                        System.out.println();
                        menu2();

                        innerLoop:
                        while(scan.hasNext() && !(input2 = scan.next()).equals("0")) {

                            switch (input2) {

                                case "1":
                                    System.out.println("Balance: " + balance(dataSource,accNumber));
                                    menu2();
                                    break;
                                case "2":
                                    System.out.println();
                                    System.out.println("Enter income:");
                                    int income = scan.nextInt();
                                    addIncome(dataSource, accNumber,income);
                                    System.out.println("Income was added!");
                                    menu2();
                                    break;
                                case "3":
                                    System.out.println();
                                    System.out.println("Enter card number:");
                                    String cardNumber = scan.next();
                                    if (authenticateCardNumber(dataSource,cardNumber,accNumber)) {
                                        System.out.println("Enter how much money you want to transfer:");
                                        int transferAmount = scan.nextInt();
                                        dotransfer(dataSource,cardNumber,accNumber,transferAmount);

                                    }
                                    menu2();
                                    break;

                                case "4":
                                    closeAccount(dataSource,accNumber);
                                    System.out.println();
                                    System.out.println("The account has been closed!");
                                    System.out.println();
                                    menu1();
                                    break innerLoop;
                                case "5":
                                    menu1();
                                    break innerLoop;

                                default:
                                    break;

                            }

                        }if (input2.equals("0")) {
                            input1 = "0";
                            break outerLoop;
                        }

                    }else {
                        System.out.println("Wrong card number or PIN!");
                        menu1();
                    }
                    //break outerLoop;
                    break;

            }

        }
        if (input1.equals("0")) {
            System.out.println("Bye!");
        }



    }

    public static void createTable(SQLiteDataSource dataSource) {

        try (Connection con = dataSource.getConnection()) {

            //System.out.println("connection is valid");
            try (Statement statement = con.createStatement()){
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card("+
                        "id INTEGER PRIMARY KEY,"+
                        "number VARCHAR(16),"+
                        "pin VARCHAR(4)," +
                        "balance INTEGER)");
                //con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }



    public static void menu1() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }
    public static void menu2() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }



    public static void displayCardAndPin(String accountNumber,String pin) {
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(accountNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
    }


    public static void insertCardToDatabase(SQLiteDataSource dataSource,String accountNumber, String pin) {
        int balance = 0;
        int id = generateSerialNumber(dataSource);

        String insertAccountIntoTable = "INSERT INTO card (id, number, pin, balance) VALUES(?,?,?,?)";

        try (Connection con = dataSource.getConnection()){
            try (PreparedStatement statement = con.prepareStatement(insertAccountIntoTable)){
                statement.setInt(1, id);
                statement.setString(2, accountNumber);
                statement.setString(3, pin);
                statement.setInt(4, balance);

                statement.executeUpdate();

            }catch (SQLException e) {
                e.printStackTrace();
            }

        }catch(SQLException e) {
            e.printStackTrace();
        }

    }


    //
    public static int generateSerialNumber(SQLiteDataSource dataSource) {
        int index = 1;
        try (Connection con = dataSource.getConnection()){
            try (Statement statement = con.createStatement()){
                try (ResultSet result = statement.executeQuery("SELECT * FROM card")){
                    while (result.next()) {
                        index++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return index;
    }

    //Account Number and Pin authentication method
    public static boolean authentication(SQLiteDataSource dataSource, String accNumber, String password) {
        boolean isvalid = false;
        try (Connection con = dataSource.getConnection()){
            try (Statement statement = con.createStatement()){
                try (ResultSet result = statement.executeQuery("SELECT * FROM card")){
                    while (result.next()){

                        String checkAccountNumber = result.getString("number");
                        String checkPassword = result.getString("pin");
                        if(checkAccountNumber.equals(accNumber) && checkPassword.equals(password)){
                            isvalid = true;
                        }

                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isvalid;
    }

    //authentication of card number
    public static boolean authenticateCardNumber(SQLiteDataSource dataSource, String cardNumber,String loggedCardNumber) {

        boolean isCardValid = false;
        if (cardNumber.equals(loggedCardNumber)){
            isCardValid = false;
            System.out.println("You can't transfer money to the same account!");
        }else if(!luhnAlgoChecker(cardNumber)){
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            isCardValid = false;
        } else{
        try (Connection con = dataSource.getConnection()){
            try (Statement statement = con.createStatement()){
                try (ResultSet result = statement.executeQuery("SELECT * FROM card")){
                    while (result.next()){
                        String checkAccountNumber = result.getString("number");

                        if(checkAccountNumber.equals(cardNumber) ){
                            isCardValid = true;

                            return true;



                        }

                    }if(!result.next() && !isCardValid){
                        System.out.println("Such a card does not exist.");
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
        return isCardValid;
    }

    public static boolean luhnAlgoChecker(String transferAccNumber) {
        boolean validNumber = false;

        String[] arr = new String[16];
        arr = transferAccNumber.split("");
        int lastdigit = Integer.parseInt(arr[15]);

        int [] intArr = new int[arr.length];
        int checkDigit;
        int sum =0;

        for (int i = 0; i <15; i++) {
            intArr[i] = Integer.parseInt(arr[i]);

            if (i == 0) {
                intArr[i] *= 2;
            }
            else if (i % 2 == 0) {
                intArr[i] *= 2;
                if (intArr[i] >= 10) {
                    intArr[i] -= 9;
                }
            }

            sum += intArr[i] ;

        }


        if (sum % 10 == 0) {
            checkDigit = 0;
            if (checkDigit == lastdigit){
                validNumber = true;
            }

        }else{
            checkDigit = 10 - sum % 10;
            if (checkDigit == lastdigit){
                validNumber = true;
            }
        }

        return validNumber;
    }


    //check balance method
    public static int balance(SQLiteDataSource dataSource, String accountNumber) {
        int balance = 0;
        try (Connection con = dataSource.getConnection()){
            try (Statement statement = con.createStatement()){
                try (ResultSet result = statement.executeQuery("SELECT * FROM card WHERE number = " + accountNumber)) {
                   balance = result.getInt("balance");
                    //System.out.println(balance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }


    public static void dotransfer(SQLiteDataSource dataSource, String recieverNumber, String senderNumber, int amount) {
        int balance = balance(dataSource,senderNumber);
        if (balance >= amount) {
            subtractIncome(dataSource,senderNumber,amount);
            addIncome(dataSource,recieverNumber,amount);
            System.out.println("Success!");
        }else {
            System.out.println("Not enough money!");
        }
    }


    //adding balance to account
    public static void addIncome(SQLiteDataSource dataSource, String number, int income) {
        int balance = balance(dataSource,number);
        String insert = "UPDATE card SET balance = ? WHERE number = ?";
        try (Connection con = dataSource.getConnection()){
            try(PreparedStatement preparedStatement = con.prepareStatement(insert)){
                preparedStatement.setInt(1, income + balance);
                preparedStatement.setString(2,number);


                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void subtractIncome(SQLiteDataSource dataSource, String number, int income) {
        int balance = balance(dataSource,number);
        String insert = "UPDATE card SET balance = ? WHERE number = ?";
        try (Connection con = dataSource.getConnection()){
            try(PreparedStatement preparedStatement = con.prepareStatement(insert)){
                preparedStatement.setInt(1, balance - income);
                preparedStatement.setString(2,number);


                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void closeAccount(SQLiteDataSource dataSource, String accNumber){
        String insert = "DELETE FROM card WHERE number = ?";
        try (Connection con = dataSource.getConnection()){
            try(PreparedStatement preparedStatement = con.prepareStatement(insert)){
                preparedStatement.setString(1, accNumber);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

