package centralniserver;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("korisnici")
public class KorisnikResource {

    private static final int PROVERA_KORISNIKA = 1;
    private static final int KREIRANJE_GRADA = 2;
    private static final int KREIRANJE_KORISNIKA = 3;
    private static final int DODAVANJE_NOVCA = 4;
    private static final int PROMENA_ADRESE_GRADA = 5;
    private static final int SVI_GRADOVI = 15;
    private static final int SVI_KORISNICI = 16;
    private static final int PRONADJI_KORISNIKA = 24;

    private static final long TIMEOUT = 5000;

    @Resource(lookup = "jms/__defaultConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "podsistem1Queue")
    private Queue podsistem1Queue;

    @Resource(lookup = "podsistem1ReplyQueue")
    private Queue podsistem1ReplyQueue;

    private String posaljiIcekaj(String poruka) {
        javax.jms.QueueConnection conn = null;
        try {
            com.sun.messaging.QueueConnectionFactory tcpCF = new com.sun.messaging.QueueConnectionFactory();
            tcpCF.setProperty("imqAddressList", "mq://localhost:7676");
            String jmsUser = System.getenv().getOrDefault("JMS_USER", "admin");
            String jmsPassword = System.getenv().getOrDefault("JMS_PASSWORD", "");
            conn = tcpCF.createQueueConnection(jmsUser, jmsPassword);
            conn.start();
            javax.jms.QueueSession session = conn.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);

            String reqId = java.util.UUID.randomUUID().toString().replace("-", "");
            javax.jms.TextMessage msg = session.createTextMessage(poruka);
            msg.setJMSReplyTo(session.createQueue("podsistem1ReplyQueue"));
            msg.setStringProperty("reqId", reqId);
            session.createSender(session.createQueue("podsistem1Queue")).send(msg);

            javax.jms.MessageConsumer consumer = session.createConsumer(session.createQueue("podsistem1ReplyQueue"));
            long deadline = System.currentTimeMillis() + TIMEOUT;
            while (System.currentTimeMillis() < deadline) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) break;
                javax.jms.Message reply = consumer.receive(Math.min(remaining, 500));
                if (reply == null) continue;
                if (reply instanceof javax.jms.TextMessage) {
                    String replyId = reply.getStringProperty("reqId");
                    if (reqId.equals(replyId)) return ((javax.jms.TextMessage) reply).getText();
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

    @GET
    @Path("provera")
    @Produces(MediaType.TEXT_PLAIN)
    public Response proveriKorisnika(
            @QueryParam("korisnickoIme") String korisnickoIme,
            @QueryParam("sifra") String sifra) {
        return Response.ok(posaljiIcekaj(PROVERA_KORISNIKA + ":" + korisnickoIme + ":" + sifra)).build();
    }

    @POST
    @Path("grad")
    @Produces(MediaType.TEXT_PLAIN)
    public Response kreirajGrad(@QueryParam("naziv") String naziv) {
        return Response.ok(posaljiIcekaj(KREIRANJE_GRADA + ":" + naziv)).build();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response kreirajKorisnika(
            @QueryParam("ime") String ime,
            @QueryParam("prezime") String prezime,
            @QueryParam("email") String email,
            @QueryParam("korisnickoIme") String korisnickoIme,
            @QueryParam("sifra") String sifra,
            @QueryParam("adresa") String adresa,
            @QueryParam("gradId") int gradId) {
        return Response.ok(posaljiIcekaj(KREIRANJE_KORISNIKA + ":" + ime + ":" + prezime + ":"
                + email + ":" + korisnickoIme + ":" + sifra + ":" + adresa + ":" + gradId)).build();
    }

    @PUT
    @Path("novac")
    @Produces(MediaType.TEXT_PLAIN)
    public Response dodajNovac(@QueryParam("id") int id, @QueryParam("iznos") double iznos) {
        return Response.ok(posaljiIcekaj(DODAVANJE_NOVCA + ":" + id + ":" + iznos)).build();
    }

    @PUT
    @Path("adresa")
    @Produces(MediaType.TEXT_PLAIN)
    public Response promeniAdresu(
            @QueryParam("id") int id,
            @QueryParam("novaAdresa") String novaAdresa,
            @QueryParam("gradId") int gradId) {
        return Response.ok(posaljiIcekaj(PROMENA_ADRESE_GRADA + ":" + id + ":" + novaAdresa + ":" + gradId)).build();
    }

    @GET
    @Path("gradovi")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sviGradovi() {
        return Response.ok(posaljiIcekaj(String.valueOf(SVI_GRADOVI))).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response sviKorisnici() {
        return Response.ok(posaljiIcekaj(String.valueOf(SVI_KORISNICI))).build();
    }

    // Opcija 23 - admin provjerava korisnika po korisnickom imenu i sifri
    @GET
    @Path("pretraga")
    @Produces(MediaType.TEXT_PLAIN)
    public Response pronadiKorisnika(
            @QueryParam("korisnickoIme") String korisnickoIme,
            @QueryParam("sifra") String sifra) {
        return Response.ok(posaljiIcekaj(PRONADJI_KORISNIKA + ":" + korisnickoIme + ":" + sifra)).build();
    }
}
