package banking;

import java.util.Random;

public class AccountNumber {
    private String BIN = "400000";
    private String accNumber;
    private String checkDigit;


     public String generateCustomerAccNum (){
         Random random = new Random();
         int customerAccNumber = random.nextInt(1000000000);
         if (customerAccNumber < 100000000) {
             customerAccNumber *= 10;
         }
         setAccNumber(Integer.toString(customerAccNumber));
         return getAccNumber();
     }
    public String generateCheckDigit() {
         String digitsWithoutChecksum = getBIN() + getAccNumber();
         String[] arr = digitsWithoutChecksum.split("");
         int [] intArr = new int[arr.length];
         int sum = 0;
         int checkDigit;
         for (int i = 0; i <arr.length; i++) {
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
         }else
         checkDigit = 10 - sum % 10;
         return Integer.toString(checkDigit);
     }
    public String generateCardNumber(){
         String sNumber = getBIN() + generateCustomerAccNum() + generateCheckDigit();
         return sNumber;
    }

    public String getBIN() {
        return BIN;
    }

    public String getAccNumber() {
        return accNumber;
    }

    public void setAccNumber(String accNumber) {
        this.accNumber = accNumber;
    }

    public String getCheckDigit() {
        return checkDigit;
    }

    public void setCheckDigit(String checkDigit) {
        this.checkDigit = checkDigit;
    }
}
