package podsistem1_korisnici;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "korisnik")
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // ⚠ Login je korisnicko_ime + sifra, NE email + lozinka!
    @Column(name = "korisnicko_ime", nullable = false, unique = true, length = 50)
    private String korisnickoIme;

    @Column(name = "sifra", nullable = false, length = 100)
    private String sifra;

    @Column(name = "ime", nullable = false, length = 100)
    private String ime;

    @Column(name = "prezime", nullable = false, length = 100)
    private String prezime;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "adresa", length = 255)
    private String adresa;

    @Column(name = "novac")
    private double novac = 0;

    @Column(name = "grad_id")
    private int gradId;

    @ManyToMany
    @JoinTable(
        name = "korisnik_uloga",
        joinColumns        = @JoinColumn(name = "korisnik_id"),
        inverseJoinColumns = @JoinColumn(name = "uloga_id")
    )
    private List<Uloga> uloge;

    public Korisnik() {}

    // ─── Getteri i setteri ────────────────────────────────────────

    public int    getId()                          { return id; }
    public void   setId(int id)                    { this.id = id; }

    public String getKorisnickoIme()                               { return korisnickoIme; }
    public void   setKorisnickoIme(String korisnickoIme)           { this.korisnickoIme = korisnickoIme; }

    public String getSifra()                       { return sifra; }
    public void   setSifra(String sifra)           { this.sifra = sifra; }

    public String getIme()                         { return ime; }
    public void   setIme(String ime)               { this.ime = ime; }

    public String getPrezime()                     { return prezime; }
    public void   setPrezime(String prezime)       { this.prezime = prezime; }

    public String getEmail()                       { return email; }
    public void   setEmail(String email)           { this.email = email; }

    public String getAdresa()                      { return adresa; }
    public void   setAdresa(String adresa)         { this.adresa = adresa; }

    public double getNovac()                       { return novac; }
    public void   setNovac(double novac)           { this.novac = novac; }

    public int    getGradId()                      { return gradId; }
    public void   setGradId(int gradId)            { this.gradId = gradId; }
   

    public List<Uloga> getUloge()                  { return uloge; }
    public void        setUloge(List<Uloga> uloge) { this.uloge = uloge; }

    @Override
    public String toString() {
        return "Korisnik{id=" + id
                + ", korisnickoIme='" + korisnickoIme + "'"
                + ", ime='" + ime + "'"
                + ", prezime='" + prezime + "'"
                + ", email='" + email + "'"
                + ", novac=" + novac
                + ", gradId=" + gradId + "}";
    }
}