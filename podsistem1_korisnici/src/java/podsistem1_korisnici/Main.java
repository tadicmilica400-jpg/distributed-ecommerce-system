package podsistem1_korisnici;
import jms.Podsistem1Listener;

/**
 *
 * @author kicam
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Podsistem1Listener listener = new Podsistem1Listener();
        listener.start();

        while(true) {
            try {
                Thread.sleep(1000);
            } catch(Exception e) {}
        }
    }
    
}
