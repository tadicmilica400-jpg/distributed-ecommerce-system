package podsistem1_korisnici;

import javax.persistence.*;
import java.util.List;

public class KorisnikService {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("is1KorisniciPU");

    private static EntityManager em() { return emf.createEntityManager(); }

    public static String proveriKorisnika(String korisnickoIme, String sifra) {
        EntityManager em = em();
        try {
            List<Korisnik> res = em.createQuery(
                "SELECT k FROM Korisnik k WHERE k.korisnickoIme = :kim AND k.sifra = :sif",
                Korisnik.class)
                .setParameter("kim", korisnickoIme)
                .setParameter("sif", sifra)
                .getResultList();
            if (!res.isEmpty()) {
                Korisnik k = res.get(0);
                String flag = jeAdministrator(k) ? "admin" : "korisnik";
                return "OK:" + k.getId() + ":" + k.getIme() + " " + k.getPrezime() + ":" + flag;
            }
            return "GRESKA:Pogresno korisnicko ime ili sifra.";
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String kreirajGrad(String naziv) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new Grad(naziv));
            tx.commit();
            return "OK:Grad '" + naziv + "' kreiran.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String kreirajKorisnika(String ime, String prezime, String email,
            String korisnickoIme, String sifra, String adresa, int gradId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Grad grad = em.find(Grad.class, gradId);
            if (grad == null) return "GRESKA:Grad sa ID " + gradId + " ne postoji.";

            List res = em.createQuery("SELECT k FROM Korisnik k WHERE k.korisnickoIme = :kim")
                .setParameter("kim", korisnickoIme).getResultList();
            if (!res.isEmpty()) return "GRESKA:Korisnicko ime '" + korisnickoIme + "' vec postoji.";

            tx.begin();
            Korisnik k = new Korisnik();
            k.setKorisnickoIme(korisnickoIme);
            k.setSifra(sifra);
            k.setIme(ime);
            k.setPrezime(prezime);
            k.setEmail(email);
            k.setAdresa(adresa);
            k.setNovac(0);
            k.setGradId(gradId);
            em.persist(k);
            tx.commit();
            return "OK:Korisnik '" + korisnickoIme + "' kreiran.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String dodajNovac(int korisnikId, double iznos) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Korisnik k = em.find(Korisnik.class, korisnikId);
            if (k == null) return "GRESKA:Korisnik nije pronadjen.";
            tx.begin();
            k.setNovac(k.getNovac() + iznos);
            tx.commit();
            return "OK:Novac dodat. Iznos: " + iznos;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String promeniAdresuGrad(int korisnikId, String novaAdresa, int gradId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Korisnik k = em.find(Korisnik.class, korisnikId);
            if (k == null) return "GRESKA:Korisnik nije pronadjen.";
            Grad g = em.find(Grad.class, gradId);
            if (g == null) return "GRESKA:Grad nije pronadjen.";
            tx.begin();
            k.setAdresa(novaAdresa);
            k.setGradId(gradId);
            tx.commit();
            return "OK:Adresa promenjena.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sviGradovi() {
        EntityManager em = em();
        try {
            List<Grad> gradovi = em.createQuery("SELECT g FROM Grad g ORDER BY g.id", Grad.class)
                    .getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Grad g : gradovi)
                sb.append(g.getId()).append(" - ").append(g.getNaziv()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sviKorisnici() {
        EntityManager em = em();
        try {
            List<Korisnik> lista = em.createQuery("SELECT k FROM Korisnik k ORDER BY k.id", Korisnik.class)
                    .getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Korisnik k : lista) {
                String grad = String.valueOf(k.getGradId());
                sb.append(k.getId()).append(" | ")
                  .append(k.getKorisnickoIme()).append(" | ")
                  .append(k.getIme()).append(" ").append(k.getPrezime()).append(" | ")
                  .append(k.getEmail()).append(" | ")
                  .append(k.getAdresa()).append(", ").append(grad).append(" | ")
                  .append("Novac: ").append(k.getNovac()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    // Opcija 23 - admin provjerava korisnika po korisnickom imenu I sifri
    public static String pronadiKorisnika(String korisnickoIme, String sifra) {
        EntityManager em = em();
        try {
            List<Korisnik> res = em.createQuery(
                "SELECT k FROM Korisnik k WHERE k.korisnickoIme = :kim AND k.sifra = :sif",
                Korisnik.class)
                .setParameter("kim", korisnickoIme)
                .setParameter("sif", sifra)
                .getResultList();
            if (!res.isEmpty()) {
                Korisnik k = res.get(0);
                String grad = String.valueOf(k.getGradId());
                String tip = jeAdministrator(k) ? "administrator" : "korisnik";
                return "OK:Korisnik postoji!\n"
                     + "ID: " + k.getId() + "\n"
                     + "Ime: " + k.getIme() + " " + k.getPrezime() + "\n"
                     + "Email: " + k.getEmail() + "\n"
                     + "Adresa: " + k.getAdresa() + ", " + grad + "\n"
                     + "Novac: " + k.getNovac() + "\n"
                     + "Tip: " + tip;
            }
            return "GRESKA:Korisnik sa tim kredencijalima ne postoji.";
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    private static boolean jeAdministrator(Korisnik k) {
        if (k.getUloge() == null) return false;
        for (Uloga u : k.getUloge())
            if ("administrator".equals(u.getNaziv())) return true;
        return false;
    }
}