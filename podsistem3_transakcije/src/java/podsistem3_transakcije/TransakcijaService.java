package podsistem3_transakcije;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

public class TransakcijaService {

    private static final EntityManagerFactory emfTrans =
            Persistence.createEntityManagerFactory("is1TransakcijePU");
    private static final EntityManagerFactory emfKor =
            Persistence.createEntityManagerFactory("is1KorisniciPU");
    private static final EntityManagerFactory emfProd =
            Persistence.createEntityManagerFactory("is1ProdajaPU");

    public static String plati(int korisnikId, String adresa, int gradId) {
        EntityManager emT = emfTrans.createEntityManager();
        EntityManager emK = emfKor.createEntityManager();
        EntityManager emP = emfProd.createEntityManager();
        EntityTransaction txT = emT.getTransaction();
        EntityTransaction txK = emK.getTransaction();
        EntityTransaction txP = emP.getTransaction();

        try {
            // 1. Procitaj sadrzaj korpe
            List<Object[]> korpaRez = emP.createNativeQuery(
                "SELECT ka.artikal_id, a.cena, ka.kolicina " +
                "FROM korpa k " +
                "JOIN korpa_artikal ka ON k.id = ka.korpa_id " +
                "JOIN artikal a ON ka.artikal_id = a.id " +
                "WHERE k.korisnik_id = ?1")
                .setParameter(1, korisnikId)
                .getResultList();

            if (korpaRez.isEmpty()) return "GRESKA:Korpa je prazna.";

            double ukupnaCena = 0;
            for (Object[] red : korpaRez) {
                double cena = ((Number) red[1]).doubleValue();
                int kol = ((Number) red[2]).intValue();
                ukupnaCena += cena * kol;
            }

            // 2. Provjeri novac - native SQL da uvijek cita svjeze iz baze (izbjegavamo JPA kes)
            List<Object[]> novacRez = emK.createNativeQuery(
                "SELECT novac FROM korisnik WHERE id = ?1")
                .setParameter(1, korisnikId)
                .getResultList();

            if (novacRez.isEmpty()) return "GRESKA:Korisnik nije pronadjen.";
            Object val = novacRez.get(0);
            double stanje = val instanceof Object[] 
                ? ((Number)((Object[])val)[0]).doubleValue() 
                : ((Number) val).doubleValue();

            if (stanje < ukupnaCena)
                return "GRESKA:Nedovoljno novca. Stanje: " + stanje + ", potrebno: " + ukupnaCena;

            // 3. Kreiraj narudzbinu i stavke
            txT.begin();
            Narudzbina narudzbina = new Narudzbina();
            narudzbina.setUkupnaCena(ukupnaCena);
            narudzbina.setVremeKreiranja(new Timestamp(System.currentTimeMillis()));
            narudzbina.setAdresa(adresa);
            narudzbina.setGradId(gradId);
            narudzbina.setKorisnikId(korisnikId);
            emT.persist(narudzbina);
            emT.flush();

            for (Object[] red : korpaRez) {
                int artikalId = ((Number) red[0]).intValue();
                double cena = ((Number) red[1]).doubleValue();
                int kol = ((Number) red[2]).intValue();

                Stavka s = new Stavka();
                s.setNarudzbina(narudzbina);
                s.setArtikalId(artikalId);
                s.setKolicina(kol);
                s.setJedinicnaCena(cena);
                emT.persist(s);
            }

            Transakcija t = new Transakcija();
            t.setSuma(ukupnaCena);
            t.setVremePlacanja(new Timestamp(System.currentTimeMillis()));
            t.setNarudzbina(narudzbina);
            emT.persist(t);
            txT.commit();

            // 4. Oduzmi novac - native SQL
            txK.begin();
            emK.createNativeQuery(
                "UPDATE korisnik SET novac = novac - ?1 WHERE id = ?2")
                .setParameter(1, ukupnaCena)
                .setParameter(2, korisnikId)
                .executeUpdate();
            txK.commit();

            // 5. Obrisi korpu
            txP.begin();
            emP.createNativeQuery(
                "DELETE FROM korpa_artikal WHERE korpa_id = (SELECT id FROM korpa WHERE korisnik_id = ?1)")
                .setParameter(1, korisnikId).executeUpdate();
            emP.createNativeQuery(
                "UPDATE korpa SET ukupna_cena = 0 WHERE korisnik_id = ?1")
                .setParameter(1, korisnikId).executeUpdate();
            txP.commit();

            return "OK:Placanje uspesno! Narudzbina ID: " + narudzbina.getId() + ", Suma: " + ukupnaCena;

        } catch (Exception e) {
            if (txT.isActive()) txT.rollback();
            if (txK.isActive()) txK.rollback();
            if (txP.isActive()) txP.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally {
            emT.close(); emK.close(); emP.close();
        }
    }

    public static String narudzbineKorisnika(int korisnikId) {
        EntityManager em = emfTrans.createEntityManager();
        try {
            List<Narudzbina> lista = em.createQuery(
                "SELECT n FROM Narudzbina n WHERE n.korisnikId = :kid ORDER BY n.vremeKreiranja DESC",
                Narudzbina.class)
                .setParameter("kid", korisnikId).getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Narudzbina n : lista) {
                sb.append("Narudzbina ID: ").append(n.getId()).append(" | ")
                  .append("Ukupno: ").append(n.getUkupnaCena()).append(" | ")
                  .append("Vreme: ").append(n.getVremeKreiranja()).append(" | ")
                  .append("Adresa: ").append(n.getAdresa()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sveNarudzbine() {
        EntityManager em = emfTrans.createEntityManager();
        try {
            List<Narudzbina> lista = em.createQuery(
                "SELECT n FROM Narudzbina n ORDER BY n.vremeKreiranja DESC", Narudzbina.class)
                .getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Narudzbina n : lista) {
                sb.append("ID: ").append(n.getId()).append(" | ")
                  .append("Korisnik ID: ").append(n.getKorisnikId()).append(" | ")
                  .append("Ukupno: ").append(n.getUkupnaCena()).append(" | ")
                  .append("Vreme: ").append(n.getVremeKreiranja()).append(" | ")
                  .append("Adresa: ").append(n.getAdresa()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sveTransakcije() {
        EntityManager em = emfTrans.createEntityManager();
        try {
            List<Transakcija> lista = em.createQuery(
                "SELECT t FROM Transakcija t ORDER BY t.vremePlacanja DESC", Transakcija.class)
                .getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Transakcija t : lista) {
                sb.append("Transakcija ID: ").append(t.getId()).append(" | ")
                  .append("Suma: ").append(t.getSuma()).append(" | ")
                  .append("Vreme: ").append(t.getVremePlacanja()).append(" | ")
                  .append("Narudzbina ID: ").append(t.getNarudzbina().getId()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }
}