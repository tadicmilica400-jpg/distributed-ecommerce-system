package podsistem3_transakcije;

import jms.Podsistem3Listener;

public class Main {

    public static void main(String[] args) {
        Podsistem3Listener listener = new Podsistem3Listener();
        listener.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {}
        }
    }
}