package centralniserver;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Stateless
@Path("transakcije")
public class TransakcijaResource {

    private static final int PLACANJE = 14;
    private static final int NARUDZBINE_KORISNIKA = 21;
    private static final int SVE_NARUDZBINE = 22;
    private static final int SVE_TRANSAKCIJE = 23;

    private static final long TIMEOUT = 5000;

    @Resource(lookup = "jms/__defaultConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "podsistem3Queue")
    private Queue podsistem3Queue;
    @Resource(lookup = "podsistem3ReplyQueue")
    private Queue podsistem3ReplyQueue;

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
            msg.setJMSReplyTo(session.createQueue("podsistem3ReplyQueue"));
            msg.setStringProperty("reqId", reqId);
            session.createSender(session.createQueue("podsistem3Queue")).send(msg);
            System.out.println("Poslato podsistem3: " + poruka);

            javax.jms.MessageConsumer consumer = session.createConsumer(
                session.createQueue("podsistem3ReplyQueue"));
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
    @Path("placanje")
    @Produces(MediaType.TEXT_PLAIN)
    public Response plati(
            @QueryParam("korisnikId") int korisnikId,
            @QueryParam("adresa") String adresa,
            @QueryParam("gradId") int gradId) {
        return Response.ok(posaljiIcekaj(PLACANJE + ":" + korisnikId + ":" + adresa + ":" + gradId)).build();
    }

    @GET
    @Path("narudzbine")
    @Produces(MediaType.TEXT_PLAIN)
    public Response narudzbineKorisnika(@QueryParam("korisnikId") int korisnikId) {
        return Response.ok(posaljiIcekaj(NARUDZBINE_KORISNIKA + ":" + korisnikId)).build();
    }

    @GET
    @Path("narudzbine/sve")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sveNarudzbine() {
        return Response.ok(posaljiIcekaj(String.valueOf(SVE_NARUDZBINE))).build();
    }

    @GET
    @Path("sve")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sveTransakcije() {
        return Response.ok(posaljiIcekaj(String.valueOf(SVE_TRANSAKCIJE))).build();
    }
}
