package podsistem2_prodaja;

import javax.persistence.*;

@Entity
@Table(name = "artikal")
public class Artikal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "naziv", nullable = false)
    private String naziv;

    @Column(name = "opis")
    private String opis;

    @Column(name = "cena")
    private double cena;

    @Column(name = "popust")
    private double popust;

    @ManyToOne
    @JoinColumn(name = "kategorija_id")
    private Kategorija kategorija;

    @Column(name = "korisnik_id")
    private int korisnikId;

    public Artikal() {}

    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
    public String getOpis() { return opis; }
    public void setOpis(String opis) { this.opis = opis; }
    public double getCena() { return cena; }
    public void setCena(double cena) { this.cena = cena; }
    public double getPopust() { return popust; }
    public void setPopust(double popust) { this.popust = popust; }
    public Kategorija getKategorija() { return kategorija; }
    public void setKategorija(Kategorija kategorija) { this.kategorija = kategorija; }
    public int getKorisnikId() { return korisnikId; }
    public void setKorisnikId(int korisnikId) { this.korisnikId = korisnikId; }
}