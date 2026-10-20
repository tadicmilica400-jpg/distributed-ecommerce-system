package jms;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;
import podsistem2_prodaja.ProdajaService;

public class Podsistem2Listener {

    private static final int KREIRANJE_KATEGORIJE = 6;
    private static final int KREIRANJE_ARTIKLA = 7;
    private static final int MENJANJE_CENE_ARTIKLA = 8;
    private static final int POSTAVLJANJE_POPUSTA = 9;
    private static final int DODAVANJE_U_KORPU = 10;
    private static final int BRISANJE_IZ_KORPE = 11;
    private static final int DODAVANJE_U_LISTU_ZELJA = 12;
    private static final int BRISANJE_IZ_LISTE_ZELJA = 13;
    private static final int SVE_KATEGORIJE = 17;
    private static final int ARTIKLI_KORISNIKA = 18;
    private static final int SADRZAJ_KORPE = 19;
    private static final int SADRZAJ_LISTE_ZELJA = 20;

    public void start() {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.enterprise.naming.SerialInitContextFactory");
            props.put(Context.PROVIDER_URL, "iiop://localhost:3700");

            Context ctx = new InitialContext(props);
            ConnectionFactory cf = (ConnectionFactory) ctx.lookup("jms/__defaultConnectionFactory");
            Queue queue = (Queue) ctx.lookup("podsistem2Queue");

            Connection connection = cf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            connection.start();

            System.out.println("Podsistem2 slusa poruke...");

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
                            case KREIRANJE_KATEGORIJE:
                                rezultat = ProdajaService.kreirajKategoriju(delovi[1], delovi[2]);
                                break;
                            case KREIRANJE_ARTIKLA:
                                rezultat = ProdajaService.kreirajArtikal(
                                        delovi[1], delovi[2],
                                        Double.parseDouble(delovi[3]),
                                        Integer.parseInt(delovi[4]),
                                        Integer.parseInt(delovi[5]));
                                break;
                            case MENJANJE_CENE_ARTIKLA:
                                rezultat = ProdajaService.promeniCenuArtikla(
                                        delovi[1],
                                        Double.parseDouble(delovi[2]),
                                        Integer.parseInt(delovi[3]));
                                break;
                            case POSTAVLJANJE_POPUSTA:
                                rezultat = ProdajaService.postaviPopust(
                                        Integer.parseInt(delovi[1]),
                                        Integer.parseInt(delovi[2]),
                                        Integer.parseInt(delovi[3]));
                                break;
                            case DODAVANJE_U_KORPU:
                                rezultat = ProdajaService.dodajUKorpu(
                                        Integer.parseInt(delovi[1]),
                                        Integer.parseInt(delovi[2]),
                                        Integer.parseInt(delovi[3]));
                                break;
                            case BRISANJE_IZ_KORPE:
                                rezultat = ProdajaService.obrisiIzKorpe(
                                        Integer.parseInt(delovi[1]),
                                        Integer.parseInt(delovi[2]),
                                        Integer.parseInt(delovi[3]));
                                break;
                            case DODAVANJE_U_LISTU_ZELJA:
                                rezultat = ProdajaService.dodajUListuZelja(
                                        Integer.parseInt(delovi[1]),
                                        Integer.parseInt(delovi[2]));
                                break;
                            case BRISANJE_IZ_LISTE_ZELJA:
                                rezultat = ProdajaService.obrisiIzListeZelja(
                                        Integer.parseInt(delovi[1]),
                                        Integer.parseInt(delovi[2]));
                                break;
                            case SVE_KATEGORIJE:
                                rezultat = ProdajaService.sveKategorije();
                                break;
                            case ARTIKLI_KORISNIKA:
                                rezultat = ProdajaService.artikliKorisnika(Integer.parseInt(delovi[1]));
                                break;
                            case SADRZAJ_KORPE:
                                rezultat = ProdajaService.sadrzajKorpe(Integer.parseInt(delovi[1]));
                                break;
                            case SADRZAJ_LISTE_ZELJA:
                                rezultat = ProdajaService.sadrzajListeZelja(Integer.parseInt(delovi[1]));
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