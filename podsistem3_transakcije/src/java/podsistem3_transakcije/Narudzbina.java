package podsistem3_transakcije;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "narudzbina")
public class Narudzbina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ukupna_cena")
    private double ukupnaCena;

    @Column(name = "vreme_kreiranja")
    private Timestamp vremeKreiranja;

    @Column(name = "adresa")
    private String adresa;

    @Column(name = "grad_id")
    private int gradId;

    @Column(name = "korisnik_id")
    private int korisnikId;

    @OneToMany(mappedBy = "narudzbina", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Stavka> stavke;

    public Narudzbina() {}

    public int getId() { return id; }
    public double getUkupnaCena() { return ukupnaCena; }
    public void setUkupnaCena(double v) { this.ukupnaCena = v; }
    public Timestamp getVremeKreiranja() { return vremeKreiranja; }
    public void setVremeKreiranja(Timestamp v) { this.vremeKreiranja = v; }
    public String getAdresa() { return adresa; }
    public void setAdresa(String v) { this.adresa = v; }
    public int getGradId() { return gradId; }
    public void setGradId(int v) { this.gradId = v; }
    public int getKorisnikId() { return korisnikId; }
    public void setKorisnikId(int v) { this.korisnikId = v; }
    public List<Stavka> getStavke() { return stavke; }
}