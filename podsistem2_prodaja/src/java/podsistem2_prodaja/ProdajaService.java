package podsistem2_prodaja;

import javax.persistence.*;
import java.util.List;

public class ProdajaService {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("is1ProdajaPU");

    private static EntityManager em() { return emf.createEntityManager(); }

    // ---- Pomocna: dohvati ili kreiraj korpu ----
    private static Korpa dohvatiIliKreirajKorpu(EntityManager em, int korisnikId) {
        List<Korpa> res = em.createQuery(
            "SELECT k FROM Korpa k WHERE k.korisnikId = :kid", Korpa.class)
            .setParameter("kid", korisnikId).getResultList();
        if (!res.isEmpty()) return res.get(0);
        Korpa k = new Korpa();
        k.setKorisnikId(korisnikId);
        k.setUkupnaCena(0);
        em.persist(k);
        em.flush();
        return k;
    }

    // ---- Pomocna: dohvati ili kreiraj listu zelja ----
    private static ListaZelja dohvatiIliKreirajListuZelja(EntityManager em, int korisnikId) {
        List<ListaZelja> res = em.createQuery(
            "SELECT l FROM ListaZelja l WHERE l.korisnikId = :kid", ListaZelja.class)
            .setParameter("kid", korisnikId).getResultList();
        if (!res.isEmpty()) return res.get(0);
        ListaZelja l = new ListaZelja();
        l.setKorisnikId(korisnikId);
        // FIX: datum_kreiranja ne sme biti null
        l.setDatumKreiranja(new java.sql.Date(System.currentTimeMillis()));
        em.persist(l);
        em.flush();
        return l;
    }

    // ---- Kategorije ----

    public static String kreirajKategoriju(String naziv, String parentId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Kategorija k = new Kategorija();
            k.setNaziv(naziv);
            if (parentId != null && !"null".equals(parentId)) {
                Kategorija parent = em.find(Kategorija.class, Integer.parseInt(parentId));
                if (parent == null) return "GRESKA:Parent kategorija ne postoji.";
                k.setParent(parent);
            }
            em.persist(k);
            tx.commit();
            return "OK:Kategorija '" + naziv + "' kreirana.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sveKategorije() {
        EntityManager em = em();
        try {
            List<Kategorija> lista = em.createQuery(
                "SELECT k FROM Kategorija k ORDER BY k.id", Kategorija.class).getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Kategorija k : lista) {
                String parent = k.getParent() != null ? " (parent: " + k.getParent().getNaziv() + ")" : "";
                sb.append(k.getId()).append(" - ").append(k.getNaziv()).append(parent).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    // ---- Artikli ----

    public static String kreirajArtikal(String naziv, String opis, double cena, int kategorijaId, int korisnikId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Kategorija kat = em.find(Kategorija.class, kategorijaId);
            if (kat == null) return "GRESKA:Kategorija ne postoji.";
            tx.begin();
            Artikal a = new Artikal();
            a.setNaziv(naziv);
            a.setOpis(opis);
            a.setCena(cena);
            a.setPopust(0);
            a.setKategorija(kat);
            a.setKorisnikId(korisnikId);
            em.persist(a);
            tx.commit();
            return "OK:Artikal '" + naziv + "' kreiran.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String promeniCenuArtikla(String artikalId, double novaCena, int korisnikId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Artikal a = em.find(Artikal.class, Integer.parseInt(artikalId));
            if (a == null) return "GRESKA:Artikal nije pronadjen.";
            if (a.getKorisnikId() != korisnikId) return "GRESKA:Nemate pravo da menjate ovaj artikal.";
            tx.begin();
            a.setCena(novaCena);
            tx.commit();
            return "OK:Cena promenjena na " + novaCena;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String postaviPopust(int artikalId, int popust, int korisnikId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Artikal a = em.find(Artikal.class, artikalId);
            if (a == null) return "GRESKA:Artikal nije pronadjen.";
            if (a.getKorisnikId() != korisnikId) return "GRESKA:Nemate pravo da menjate ovaj artikal.";
            if (popust < 0 || popust > 100) return "GRESKA:Popust mora biti 0-100.";
            tx.begin();
            a.setPopust(popust);
            tx.commit();
            return "OK:Popust postavljen na " + popust + "%";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String artikliKorisnika(int korisnikId) {
        EntityManager em = em();
        try {
            List<Artikal> lista = em.createQuery(
                "SELECT a FROM Artikal a WHERE a.korisnikId = :kid ORDER BY a.id", Artikal.class)
                .setParameter("kid", korisnikId).getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (Artikal a : lista) {
                sb.append(a.getId()).append(" | ").append(a.getNaziv())
                  .append(" | Cena: ").append(a.getCena())
                  .append(" | Popust: ").append(a.getPopust()).append("%")
                  .append(" | Kat: ").append(a.getKategorija().getNaziv()).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    // ---- Korpa ----

    public static String dodajUKorpu(int korisnikId, int artikalId, int kolicina) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Artikal artikal = em.find(Artikal.class, artikalId);
            if (artikal == null) return "GRESKA:Artikal nije pronadjen.";
            tx.begin();
            Korpa korpa = dohvatiIliKreirajKorpu(em, korisnikId);
            List<KorpaArtikal> existing = em.createQuery(
                "SELECT ka FROM KorpaArtikal ka WHERE ka.korpaId = :kid AND ka.artikalId = :aid",
                KorpaArtikal.class)
                .setParameter("kid", korpa.getId())
                .setParameter("aid", artikalId)
                .getResultList();
            if (!existing.isEmpty()) {
                existing.get(0).setKolicina(existing.get(0).getKolicina() + kolicina);
            } else {
                em.persist(new KorpaArtikal(korpa.getId(), artikalId, kolicina));
            }
            korpa.setUkupnaCena(korpa.getUkupnaCena() + artikal.getCena() * kolicina);
            tx.commit();
            return "OK:Artikal dodat u korpu.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String obrisiIzKorpe(int korisnikId, int artikalId, int kolicina) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            List<Korpa> korpaRes = em.createQuery(
                "SELECT k FROM Korpa k WHERE k.korisnikId = :kid", Korpa.class)
                .setParameter("kid", korisnikId).getResultList();
            if (korpaRes.isEmpty()) return "GRESKA:Korpa je prazna.";
            Korpa korpa = korpaRes.get(0);

            List<KorpaArtikal> existing = em.createQuery(
                "SELECT ka FROM KorpaArtikal ka WHERE ka.korpaId = :kid AND ka.artikalId = :aid",
                KorpaArtikal.class)
                .setParameter("kid", korpa.getId())
                .setParameter("aid", artikalId)
                .getResultList();
            if (existing.isEmpty()) return "GRESKA:Artikal nije u korpi.";

            KorpaArtikal ka = existing.get(0);
            Artikal artikal = em.find(Artikal.class, artikalId);
            tx.begin();
            if (ka.getKolicina() <= kolicina) {
                if (artikal != null)
                    korpa.setUkupnaCena(korpa.getUkupnaCena() - artikal.getCena() * ka.getKolicina());
                // native SQL - pouzdanije za composite PK
                em.createNativeQuery(
                    "DELETE FROM korpa_artikal WHERE korpa_id = ?1 AND artikal_id = ?2")
                    .setParameter(1, korpa.getId())
                    .setParameter(2, artikalId)
                    .executeUpdate();
            } else {
                if (artikal != null)
                    korpa.setUkupnaCena(korpa.getUkupnaCena() - artikal.getCena() * kolicina);
                em.createNativeQuery(
                    "UPDATE korpa_artikal SET kolicina = kolicina - ?1 WHERE korpa_id = ?2 AND artikal_id = ?3")
                    .setParameter(1, kolicina)
                    .setParameter(2, korpa.getId())
                    .setParameter(3, artikalId)
                    .executeUpdate();
            }
            tx.commit();
            return "OK:Artikal uklonjen iz korpe.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sadrzajKorpe(int korisnikId) {
        EntityManager em = em();
        try {
            List<Korpa> res = em.createQuery(
                "SELECT k FROM Korpa k WHERE k.korisnikId = :kid", Korpa.class)
                .setParameter("kid", korisnikId).getResultList();
            if (res.isEmpty()) return "OK:\nKorpa je prazna.\nUKUPNO: 0.0";
            Korpa korpa = res.get(0);
            List<KorpaArtikal> stavke = em.createQuery(
                "SELECT ka FROM KorpaArtikal ka WHERE ka.korpaId = :kid",
                KorpaArtikal.class)
                .setParameter("kid", korpa.getId()).getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (KorpaArtikal ka : stavke) {
                Artikal a = em.find(Artikal.class, ka.getArtikalId());
                if (a != null) {
                    sb.append(a.getId()).append(" | ").append(a.getNaziv())
                      .append(" | Cena: ").append(a.getCena())
                      .append(" | Kol: ").append(ka.getKolicina())
                      .append(" | Ukupno: ").append(a.getCena() * ka.getKolicina()).append("\n");
                }
            }
            sb.append("UKUPNO: ").append(korpa.getUkupnaCena());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    // ---- Lista zelja ----

    public static String dodajUListuZelja(int korisnikId, int artikalId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            Artikal artikal = em.find(Artikal.class, artikalId);
            if (artikal == null) return "GRESKA:Artikal nije pronadjen.";
            tx.begin();
            ListaZelja lista = dohvatiIliKreirajListuZelja(em, korisnikId);
            List<ListaZeljaArtikal> existing = em.createQuery(
                "SELECT la FROM ListaZeljaArtikal la WHERE la.listaZeljaId = :lid AND la.artikalId = :aid",
                ListaZeljaArtikal.class)
                .setParameter("lid", lista.getId())
                .setParameter("aid", artikalId)
                .getResultList();
            if (!existing.isEmpty()) {
                tx.rollback();
                return "GRESKA:Artikal je vec u listi zelja.";
            }
            em.persist(new ListaZeljaArtikal(lista.getId(), artikalId));
            tx.commit();
            return "OK:Artikal dodat u listu zelja.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String obrisiIzListeZelja(int korisnikId, int artikalId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            List<ListaZelja> res = em.createQuery(
                "SELECT l FROM ListaZelja l WHERE l.korisnikId = :kid", ListaZelja.class)
                .setParameter("kid", korisnikId).getResultList();
            if (res.isEmpty()) return "GRESKA:Lista zelja je prazna.";
            ListaZelja lista = res.get(0);
            List<ListaZeljaArtikal> existing = em.createQuery(
                "SELECT la FROM ListaZeljaArtikal la WHERE la.listaZeljaId = :lid AND la.artikalId = :aid",
                ListaZeljaArtikal.class)
                .setParameter("lid", lista.getId())
                .setParameter("aid", artikalId)
                .getResultList();
            if (existing.isEmpty()) return "GRESKA:Artikal nije u listi zelja.";
            tx.begin();
            // native SQL - pouzdanije za composite PK
            em.createNativeQuery(
                "DELETE FROM lista_zelja_artikal WHERE lista_zelja_id = ?1 AND artikal_id = ?2")
                .setParameter(1, lista.getId())
                .setParameter(2, artikalId)
                .executeUpdate();
            tx.commit();
            return "OK:Artikal uklonjen iz liste zelja.";
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }

    public static String sadrzajListeZelja(int korisnikId) {
        EntityManager em = em();
        try {
            List<ListaZelja> res = em.createQuery(
                "SELECT l FROM ListaZelja l WHERE l.korisnikId = :kid", ListaZelja.class)
                .setParameter("kid", korisnikId).getResultList();
            if (res.isEmpty()) return "OK:\nLista zelja je prazna.";
            ListaZelja lista = res.get(0);
            List<ListaZeljaArtikal> stavke = em.createQuery(
                "SELECT la FROM ListaZeljaArtikal la WHERE la.listaZeljaId = :lid",
                ListaZeljaArtikal.class)
                .setParameter("lid", lista.getId()).getResultList();
            StringBuilder sb = new StringBuilder("OK:\n");
            for (ListaZeljaArtikal la : stavke) {
                Artikal a = em.find(Artikal.class, la.getArtikalId());
                if (a != null) {
                    sb.append(a.getId()).append(" | ").append(a.getNaziv())
                      .append(" | Cena: ").append(a.getCena())
                      .append(" | Popust: ").append(a.getPopust()).append("%\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "GRESKA:" + e.getMessage();
        } finally { em.close(); }
    }
}