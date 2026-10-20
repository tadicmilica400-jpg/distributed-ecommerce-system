package jms;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import podsistem3_transakcije.TransakcijaService;

public class Podsistem3Listener {

    private static final int PLACANJE = 14;
    private static final int NARUDZBINE_KORISNIKA = 21;
    private static final int SVE_NARUDZBINE = 22;
    private static final int SVE_TRANSAKCIJE = 23;

    public void start() {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.enterprise.naming.SerialInitContextFactory");
            props.put(Context.PROVIDER_URL, "iiop://localhost:3700");

            Context ctx = new InitialContext(props);
            ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/__defaultConnectionFactory");
            Queue queue = (Queue) ctx.lookup("podsistem3Queue");

            Connection connection = cf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            connection.start();

            System.out.println("Podsistem3 slusa poruke...");

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
                            case PLACANJE:
                                rezultat = TransakcijaService.plati(
                                        Integer.parseInt(delovi[1]),
                                        delovi[2],
                                        Integer.parseInt(delovi[3]));
                                break;
                            case NARUDZBINE_KORISNIKA:
                                rezultat = TransakcijaService.narudzbineKorisnika(
                                        Integer.parseInt(delovi[1]));
                                break;
                            case SVE_NARUDZBINE:
                                rezultat = TransakcijaService.sveNarudzbine();
                                break;
                            case SVE_TRANSAKCIJE:
                                rezultat = TransakcijaService.sveTransakcije();
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