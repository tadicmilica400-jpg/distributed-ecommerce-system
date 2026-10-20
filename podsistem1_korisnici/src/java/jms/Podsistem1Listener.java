package jms;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import podsistem1_korisnici.KorisnikService;

public class Podsistem1Listener {

    private static final int PROVERA_KORISNIKA = 1;
    private static final int KREIRANJE_GRADA = 2;
    private static final int KREIRANJE_KORISNIKA = 3;
    private static final int DODAVANJE_NOVCA = 4;
    private static final int PROMENA_ADRESE_GRADA = 5;
    private static final int SVI_GRADOVI = 15;
    private static final int SVI_KORISNICI = 16;
    private static final int PRONADJI_KORISNIKA = 24;

    public void start() {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.enterprise.naming.SerialInitContextFactory");
            props.put(Context.PROVIDER_URL, "iiop://localhost:3700");
            props.put(Context.SECURITY_PRINCIPAL, "root");
            props.put(Context.SECURITY_CREDENTIALS, "root");

            Context ctx = new InitialContext(props);
            ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/__defaultConnectionFactory");
            Queue queue = (Queue) ctx.lookup("podsistem1Queue");
            Connection connection = cf.createConnection("admin", "admin");
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            connection.start();

            System.out.println("Podsistem1 slusa poruke...");

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage) {
                        TextMessage tm = (TextMessage) message;
                        String poruka = tm.getText();
                        System.out.println("Primljena poruka: " + poruka);

                        String[] delovi = poruka.split(":");
                        int operacija = Integer.parseInt(delovi[0]);
                        String rezultat = "OK";

                        switch (operacija) {
                            case PROVERA_KORISNIKA:
                                rezultat = KorisnikService.proveriKorisnika(delovi[1], delovi[2]);
                                break;
                            case KREIRANJE_GRADA:
                                rezultat = KorisnikService.kreirajGrad(delovi[1]);
                                break;
                            case KREIRANJE_KORISNIKA:
                                rezultat = KorisnikService.kreirajKorisnika(
                                        delovi[1], delovi[2], delovi[3],
                                        delovi[4], delovi[5], delovi[6],
                                        Integer.parseInt(delovi[7]));
                                break;
                            case DODAVANJE_NOVCA:
                                rezultat = KorisnikService.dodajNovac(
                                        Integer.parseInt(delovi[1]),
                                        Double.parseDouble(delovi[2]));
                                break;
                            case PROMENA_ADRESE_GRADA:
                                rezultat = KorisnikService.promeniAdresuGrad(
                                        Integer.parseInt(delovi[1]),
                                        delovi[2],
                                        Integer.parseInt(delovi[3]));
                                break;
                            case SVI_GRADOVI:
                                rezultat = KorisnikService.sviGradovi();
                                break;
                            case SVI_KORISNICI:
                                rezultat = KorisnikService.sviKorisnici();
                                break;
                            case PRONADJI_KORISNIKA:
                                rezultat = KorisnikService.pronadiKorisnika(delovi[1], delovi[2]);
                                break;
                        }

                        System.out.println("Rezultat: " + rezultat);

                        Destination replyTo = message.getJMSReplyTo();
                        if (replyTo != null) {
                            TextMessage odgovor = session.createTextMessage(rezultat);
                            String reqId = message.getStringProperty("reqId");
                            if (reqId != null) odgovor.setStringProperty("reqId", reqId);
                            MessageProducer producer = session.createProducer(replyTo);
                            producer.send(odgovor);
                            producer.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}