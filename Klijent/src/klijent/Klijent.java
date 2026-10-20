package klijent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class Klijent {

    private static final String BASE_URL = "http://localhost:8080/CentralniServer/api";

    private static int ulogovaniKorisnikId = -1;
    private static String ulogovanoKorisnickoIme = null;
    private static boolean jeAdmin = false;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   SISTEM ZA PRODAJU - Konzolni klijent  ");
        System.out.println("==========================================");

        while (true) {
            if (ulogovaniKorisnikId == -1) {
                menuPrijava();
            } else {
                menuGlavni();
            }
        }
    }

    // ===================== MENIJI =====================

    private static void menuPrijava() {
        System.out.println("\n--- PRIJAVA ---");
        System.out.println("1. Prijavi se");
        System.out.println("0. Izlaz");
        System.out.print("Izbor: ");
        String izbor = scanner.nextLine().trim();
        switch (izbor) {
            case "1": prijaviSe(); break;
            case "0": System.out.println("Dovidjenja!"); System.exit(0); break;
            default: System.out.println("Nepoznata opcija.");
        }
    }

    private static void menuGlavni() {
        String tip = jeAdmin ? "ADMINISTRATOR" : "KORISNIK";
        System.out.println("\n--- " + tip + ": " + ulogovanoKorisnickoIme + " ---");
        System.out.println("--- KORISNICI ---");
        System.out.println("1.  Svi gradovi           [admin]");
        System.out.println("2.  Svi korisnici         [admin]");
        System.out.println("3.  Kreiraj grad          [admin]");
        System.out.println("4.  Dodaj novac korisniku [admin]");
        System.out.println("5.  Promeni adresu/grad   [admin]");
        System.out.println("22. Kreiraj korisnika     [admin]");
        System.out.println("23. Proveri korisnika     [admin]");
        System.out.println("--- PRODAJA ---");
        System.out.println("6.  Sve kategorije");
        System.out.println("7.  Moji artikli");
        System.out.println("8.  Kreiraj kategoriju");
        System.out.println("9.  Kreiraj artikal");
        System.out.println("10. Promeni cenu artikla");
        System.out.println("11. Postavi popust");
        System.out.println("12. Moja korpa");
        System.out.println("13. Dodaj u korpu");
        System.out.println("14. Obrisi iz korpe");
        System.out.println("15. Lista zelja");
        System.out.println("16. Dodaj u listu zelja");
        System.out.println("17. Obrisi iz liste zelja");
        System.out.println("--- TRANSAKCIJE ---");
        System.out.println("18. Plati (korpa)");
        System.out.println("19. Moje narudzbine");
        System.out.println("20. Sve narudzbine        [admin]");
        System.out.println("21. Sve transakcije       [admin]");
        System.out.println("0.  Odjavi se");
        System.out.print("Izbor: ");
        obradiIzbor(scanner.nextLine().trim());
    }

    private static void obradiIzbor(String izbor) {
        switch (izbor) {
            case "0":
                ulogovaniKorisnikId = -1;
                ulogovanoKorisnickoIme = null;
                jeAdmin = false;
                System.out.println("Odjavili ste se.");
                break;
            case "1":
                if (jeAdmin) ispisi(get("/korisnici/gradovi"));
                else System.out.println("Nemate pristup.");
                break;
            case "2":
                if (jeAdmin) ispisi(get("/korisnici"));
                else System.out.println("Nemate pristup.");
                break;
            case "3":
                if (jeAdmin) kreirajGrad();
                else System.out.println("Nemate pristup.");
                break;
            case "4":
                if (jeAdmin) dodajNovac();
                else System.out.println("Nemate pristup.");
                break;
            case "5":
                if (jeAdmin) promeniAdresu();
                else System.out.println("Nemate pristup.");
                break;
            case "6":
                ispisi(get("/prodaja/kategorije"));
                break;
            case "7":
                ispisi(get("/prodaja/artikli?korisnikId=" + ulogovaniKorisnikId));
                break;
            case "8":
                kreirajKategoriju();
                break;
            case "9":
                kreirajArtikal();
                break;
            case "10":
                promeniCenu();
                break;
            case "11":
                postaviPopust();
                break;
            case "12":
                ispisi(get("/prodaja/korpa?korisnikId=" + ulogovaniKorisnikId));
                break;
            case "13":
                dodajUKorpu();
                break;
            case "14":
                obrisiIzKorpe();
                break;
            case "15":
                ispisi(get("/prodaja/lista-zelja?korisnikId=" + ulogovaniKorisnikId));
                break;
            case "16":
                dodajUListuZelja();
                break;
            case "17":
                obrisiIzListeZelja();
                break;
            case "18":
                plati();
                break;
            case "19":
                ispisi(get("/transakcije/narudzbine?korisnikId=" + ulogovaniKorisnikId));
                break;
            case "20":
                if (jeAdmin) ispisi(get("/transakcije/narudzbine/sve"));
                else System.out.println("Nemate pristup.");
                break;
            case "21":
                if (jeAdmin) ispisi(get("/transakcije/sve"));
                else System.out.println("Nemate pristup.");
                break;
            case "22":
                if (jeAdmin) registrujSe();
                else System.out.println("Nemate pristup.");
                break;
            case "23":
                if (jeAdmin) provjeriKorisnika();
                else System.out.println("Nemate pristup.");
                break;
            default:
                System.out.println("Nepoznata opcija.");
        }
    }

    // ===================== OPERACIJE =====================

    private static void prijaviSe() {
        System.out.print("Korisnicko ime: ");
        String km = scanner.nextLine().trim();
        System.out.print("Sifra: ");
        String sifra = scanner.nextLine().trim();
        String odgovor = get("/korisnici/provera?korisnickoIme=" + enc(km) + "&sifra=" + enc(sifra));
        if (odgovor != null && odgovor.trim().startsWith("OK:")) {
            String[] delovi = odgovor.trim().split(":");
            try {
                ulogovaniKorisnikId = Integer.parseInt(delovi[1].trim());
                ulogovanoKorisnickoIme = km;
                jeAdmin = delovi.length >= 4 && "admin".equals(delovi[3].trim());
                System.out.println("Dobrodosli, " + ulogovanoKorisnickoIme + (jeAdmin ? " [ADMINISTRATOR]" : "") + "!");
            } catch (Exception e) {
                System.out.println("Greska pri parsiranju odgovora: " + odgovor);
            }
        } else {
            System.out.println("Pogresno korisnicko ime ili sifra!");
        }
    }

    private static void registrujSe() {
        System.out.print("Ime: ");
        String ime = scanner.nextLine().trim();
        System.out.print("Prezime: ");
        String prezime = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Korisnicko ime: ");
        String km = scanner.nextLine().trim();
        System.out.print("Sifra: ");
        String sifra = scanner.nextLine().trim();
        System.out.print("Adresa: ");
        String adresa = scanner.nextLine().trim();
        System.out.print("Grad ID: ");
        String gradId = scanner.nextLine().trim();
        String url = "/korisnici?ime=" + enc(ime) + "&prezime=" + enc(prezime)
                + "&email=" + enc(email) + "&korisnickoIme=" + enc(km)
                + "&sifra=" + enc(sifra) + "&adresa=" + enc(adresa) + "&gradId=" + gradId;
        ispisi(post(url));
    }

    // Opcija 23 - admin provjerava korisnika po korisnickom imenu i sifri
    private static void provjeriKorisnika() {
        System.out.print("Korisnicko ime: ");
        String km = scanner.nextLine().trim();
        System.out.print("Sifra: ");
        String sifra = scanner.nextLine().trim();
        ispisi(get("/korisnici/pretraga?korisnickoIme=" + enc(km) + "&sifra=" + enc(sifra)));
    }

    private static void kreirajGrad() {
        System.out.print("Naziv grada: ");
        String naziv = scanner.nextLine().trim();
        ispisi(post("/korisnici/grad?naziv=" + enc(naziv)));
    }

    private static void dodajNovac() {
        System.out.print("Korisnik ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Iznos: ");
        String iznos = scanner.nextLine().trim();
        ispisi(put("/korisnici/novac?id=" + id + "&iznos=" + iznos));
    }

    private static void promeniAdresu() {
        System.out.print("Korisnik ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Nova adresa: ");
        String adresa = scanner.nextLine().trim();
        System.out.print("Grad ID: ");
        String gradId = scanner.nextLine().trim();
        ispisi(put("/korisnici/adresa?id=" + id + "&novaAdresa=" + enc(adresa) + "&gradId=" + gradId));
    }

    private static void kreirajKategoriju() {
        System.out.print("Naziv kategorije: ");
        String naziv = scanner.nextLine().trim();
        System.out.print("Parent ID (Enter za null): ");
        String parentId = scanner.nextLine().trim();
        if (parentId.isEmpty()) parentId = "null";
        ispisi(post("/prodaja/kategorija?naziv=" + enc(naziv) + "&parentId=" + parentId));
    }

    private static void kreirajArtikal() {
        System.out.print("Naziv: ");
        String naziv = scanner.nextLine().trim();
        System.out.print("Opis: ");
        String opis = scanner.nextLine().trim();
        System.out.print("Cena: ");
        String cena = scanner.nextLine().trim();
        System.out.print("Kategorija ID: ");
        String katId = scanner.nextLine().trim();
        ispisi(post("/prodaja/artikal?naziv=" + enc(naziv) + "&opis=" + enc(opis)
                + "&cena=" + cena + "&kategorijaId=" + katId + "&korisnikId=" + ulogovaniKorisnikId));
    }

    private static void promeniCenu() {
        System.out.print("Artikal ID: ");
        String artId = scanner.nextLine().trim();
        System.out.print("Nova cena: ");
        String cena = scanner.nextLine().trim();
        ispisi(put("/prodaja/artikal/cena?artikalId=" + artId + "&novaCena=" + cena + "&korisnikId=" + ulogovaniKorisnikId));
    }

    private static void postaviPopust() {
        System.out.print("Artikal ID: ");
        String artId = scanner.nextLine().trim();
        System.out.print("Popust (0-100): ");
        String popust = scanner.nextLine().trim();
        ispisi(put("/prodaja/artikal/popust?artikalId=" + artId + "&popust=" + popust + "&korisnikId=" + ulogovaniKorisnikId));
    }

    private static void dodajUKorpu() {
        System.out.print("Artikal ID: ");
        String artId = scanner.nextLine().trim();
        System.out.print("Kolicina: ");
        String kol = scanner.nextLine().trim();
        ispisi(post("/prodaja/korpa?korisnikId=" + ulogovaniKorisnikId + "&artikalId=" + artId + "&kolicina=" + kol));
    }

    private static void obrisiIzKorpe() {
        System.out.print("Artikal ID: ");
        String artId = scanner.nextLine().trim();
        System.out.print("Kolicina: ");
        String kol = scanner.nextLine().trim();
        ispisi(delete("/prodaja/korpa?korisnikId=" + ulogovaniKorisnikId + "&artikalId=" + artId + "&kolicina=" + kol));
    }

    private static void dodajUListuZelja() {
        System.out.print("Artikal ID: ");
        String artId = scanner.nextLine().trim();
        ispisi(post("/prodaja/lista-zelja?korisnikId=" + ulogovaniKorisnikId + "&artikalId=" + artId));
    }

    private static void obrisiIzListeZelja() {
        System.out.print("Artikal ID: ");
        String artId = scanner.nextLine().trim();
        ispisi(delete("/prodaja/lista-zelja?korisnikId=" + ulogovaniKorisnikId + "&artikalId=" + artId));
    }

    private static void plati() {
        System.out.print("Adresa dostave: ");
        String adresa = scanner.nextLine().trim();
        System.out.print("Grad ID: ");
        String gradId = scanner.nextLine().trim();
        ispisi(post("/transakcije/placanje?korisnikId=" + ulogovaniKorisnikId + "&adresa=" + enc(adresa) + "&gradId=" + gradId));
    }

    // ===================== HTTP METODE =====================

    private static String get(String putanja) {
        try {
            URL url = new URL(BASE_URL + putanja);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            return citajOdgovor(conn);
        } catch (Exception e) {
            return "GRESKA: " + e.getMessage();
        }
    }

    private static String post(String putanja) {
        try {
            URL url = new URL(BASE_URL + putanja);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            return citajOdgovor(conn);
        } catch (Exception e) {
            return "GRESKA: " + e.getMessage();
        }
    }

    private static String put(String putanja) {
        try {
            URL url = new URL(BASE_URL + putanja);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            return citajOdgovor(conn);
        } catch (Exception e) {
            return "GRESKA: " + e.getMessage();
        }
    }

    private static String delete(String putanja) {
        try {
            URL url = new URL(BASE_URL + putanja);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            return citajOdgovor(conn);
        } catch (Exception e) {
            return "GRESKA: " + e.getMessage();
        }
    }

    private static String citajOdgovor(HttpURLConnection conn) {
        try {
            InputStream is;
            try {
                is = conn.getInputStream();
            } catch (IOException e) {
                is = conn.getErrorStream();
            }
            if (is == null) return "GRESKA: Nema odgovora";
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String linija;
            while ((linija = br.readLine()) != null) {
                sb.append(linija).append("\n");
            }
            br.close();
            return sb.toString().trim();
        } catch (Exception e) {
            return "GRESKA pri citanju odgovora: " + e.getMessage();
        }
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    private static void ispisi(String odgovor) {
        System.out.println("--- Odgovor ---");
        System.out.println(odgovor);
        System.out.println("---------------");
    }
}