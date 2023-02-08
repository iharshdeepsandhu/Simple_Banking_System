package banking;

import java.util.Random;

public class Password {

    public int passGenerator() {
        Random rnd = new Random();
        int pass = rnd.nextInt(10000);
        if (pass <= 99) {
            pass *= 100;
        } else if(pass <= 999) {
            pass *= 10;
        }
        return pass;
    }

}
