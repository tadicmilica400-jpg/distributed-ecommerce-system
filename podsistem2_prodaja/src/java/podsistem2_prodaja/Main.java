package podsistem2_prodaja;

import jms.Podsistem2Listener;

public class Main {

    public static void main(String[] args) {

        Podsistem2Listener listener = new Podsistem2Listener();
        listener.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

    }

}