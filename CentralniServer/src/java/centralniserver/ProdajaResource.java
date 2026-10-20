package centralniserver;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("prodaja")
public class ProdajaResource {

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

    private static final long TIMEOUT = 5000;

    //@Resource(lookup = "jms/__defaultConnectionFactory")
    //private ConnectionFactory connectionFactory;

    //@Resource(lookup = "podsistem2Queue")
    //private Queue podsistem2Queue;
    
    //@Resource(lookup = "podsistem2ReplyQueue")
    //private Queue podsistem2ReplyQueue;

    private String posaljiIcekaj(String poruka) {
        com.sun.messaging.QueueConnectionFactory tcpCF = null;
        javax.jms.QueueConnection conn = null;
        try {
            tcpCF = new com.sun.messaging.QueueConnectionFactory();
            tcpCF.setProperty("imqAddressList", "mq://localhost:7676");
            String jmsUser = System.getenv().getOrDefault("JMS_USER", "admin");
            String jmsPassword = System.getenv().getOrDefault("JMS_PASSWORD", "");
            conn = tcpCF.createQueueConnection(jmsUser, jmsPassword);
            conn.start();
            javax.jms.QueueSession session = conn.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

            String reqId = java.util.UUID.randomUUID().toString().replace("-", "");
            javax.jms.TextMessage msg = session.createTextMessage(poruka);
            msg.setJMSReplyTo(session.createQueue("podsistem2ReplyQueue"));
            msg.setStringProperty("reqId", reqId);
            session.createSender(session.createQueue("podsistem2Queue")).send(msg);
            System.out.println("Poslato podsistem2: " + poruka);

            javax.jms.MessageConsumer consumer = session.createConsumer(
                session.createQueue("podsistem2ReplyQueue"));
            long deadline = System.currentTimeMillis() + TIMEOUT;
            while (System.currentTimeMillis() < deadline) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) break;
                javax.jms.Message reply = consumer.receive(Math.min(remaining, 500));
                if (reply == null) continue;
                if (reply instanceof javax.jms.TextMessage) {
                    String replyId = reply.getStringProperty("reqId");
                    if (reqId.equals(replyId)) {
                        return ((javax.jms.TextMessage) reply).getText();
                    }
                    System.out.println("Odbacena stale poruka za: " + replyId);
                }
            }
            return "Nema odgovora od podsistema.";
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA: " + e.getMessage();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    @POST
    @Path("kategorija")
    @Produces(MediaType.TEXT_PLAIN)
    public Response kreirajKategoriju(
            @QueryParam("naziv") String naziv,
            @QueryParam("parentId") String parentId) {
        return Response.ok(posaljiIcekaj(KREIRANJE_KATEGORIJE + ":" + naziv + ":" + (parentId != null ? parentId : "null"))).build();
    }

    @POST
    @Path("artikal")
    @Produces(MediaType.TEXT_PLAIN)
    public Response kreirajArtikal(
            @QueryParam("naziv") String naziv,
            @QueryParam("opis") String opis,
            @QueryParam("cena") double cena,
            @QueryParam("kategorijaId") int kategorijaId,
            @QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(KREIRANJE_ARTIKLA + ":" + naziv + ":" + opis + ":" + cena + ":" + kategorijaId + ":" + korisnikId)).build();
    }

    @PUT
    @Path("artikal/cena")
    @Produces(MediaType.TEXT_PLAIN)
    public Response promeniCenu(
            @QueryParam("artikalId") int artikalId,
            @QueryParam("novaCena") double novaCena,
            @QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(MENJANJE_CENE_ARTIKLA + ":" + artikalId + ":" + novaCena + ":" + korisnikId)).build();
    }

    @PUT
    @Path("artikal/popust")
    @Produces(MediaType.TEXT_PLAIN)
    public Response postaviPopust(
            @QueryParam("artikalId") int artikalId,
            @QueryParam("popust") int popust,
            @QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(POSTAVLJANJE_POPUSTA + ":" + artikalId + ":" + popust + ":" + korisnikId)).build();
    }

    @POST
    @Path("korpa")
    @Produces(MediaType.TEXT_PLAIN)
    public Response dodajUKorpu(
            @QueryParam("korisnikId") int korisnikId,
            @QueryParam("artikalId") int artikalId,
            @QueryParam("kolicina") int kolicina) {
        return Response.ok(posaljiIcekaj(DODAVANJE_U_KORPU + ":" + korisnikId + ":" + artikalId + ":" + kolicina)).build();
    }

    @DELETE
    @Path("korpa")
    @Produces(MediaType.TEXT_PLAIN)
    public Response obrisiIzKorpe(
            @QueryParam("korisnikId") int korisnikId,
            @QueryParam("artikalId") int artikalId,
            @QueryParam("kolicina") int kolicina) {
        return Response.ok(posaljiIcekaj(BRISANJE_IZ_KORPE + ":" + korisnikId + ":" + artikalId + ":" + kolicina)).build();
    }

    @POST
    @Path("lista-zelja")
    @Produces(MediaType.TEXT_PLAIN)
    public Response dodajUListuZelja(
            @QueryParam("korisnikId") int korisnikId,
            @QueryParam("artikalId") int artikalId) {
        return Response.ok(posaljiIcekaj(DODAVANJE_U_LISTU_ZELJA + ":" + korisnikId + ":" + artikalId)).build();
    }

    @DELETE
    @Path("lista-zelja")
    @Produces(MediaType.TEXT_PLAIN)
    public Response obrisiIzListeZelja(
            @QueryParam("korisnikId") int korisnikId,
            @QueryParam("artikalId") int artikalId) {
        return Response.ok(posaljiIcekaj(BRISANJE_IZ_LISTE_ZELJA + ":" + korisnikId + ":" + artikalId)).build();
    }

    @GET
    @Path("kategorije")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sveKategorije() {
        return Response.ok(posaljiIcekaj(String.valueOf(SVE_KATEGORIJE))).build();
    }

    @GET
    @Path("artikli")
    @Produces(MediaType.TEXT_PLAIN)
    public Response artikliKorisnika(@QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(ARTIKLI_KORISNIKA + ":" + korisnikId)).build();
    }

    @GET
    @Path("korpa")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sadrzajKorpe(@QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(SADRZAJ_KORPE + ":" + korisnikId)).build();
    }

    @GET
    @Path("lista-zelja")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sadrzajListeZelja(@QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(SADRZAJ_LISTE_ZELJA + ":" + korisnikId)).build();
    }
}
