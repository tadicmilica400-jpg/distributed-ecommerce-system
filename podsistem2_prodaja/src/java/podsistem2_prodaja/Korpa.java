package podsistem2_prodaja;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "korpa")
public class Korpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "korisnik_id", unique = true)
    private int korisnikId;

    @Column(name = "ukupna_cena")
    private double ukupnaCena;

    @OneToMany(mappedBy = "korpa", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<KorpaArtikal> stavke;

    public Korpa() {}

    public int getId() { return id; }
    public int getKorisnikId() { return korisnikId; }
    public void setKorisnikId(int korisnikId) { this.korisnikId = korisnikId; }
    public double getUkupnaCena() { return ukupnaCena; }
    public void setUkupnaCena(double ukupnaCena) { this.ukupnaCena = ukupnaCena; }
    public List<KorpaArtikal> getStavke() { return stavke; }
    public void setStavke(List<KorpaArtikal> stavke) { this.stavke = stavke; }
}