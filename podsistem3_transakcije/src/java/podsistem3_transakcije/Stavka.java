package podsistem3_transakcije;

import javax.persistence.*;

@Entity
@Table(name = "stavka")
public class Stavka {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "narudzbina_id")
    private Narudzbina narudzbina;

    @Column(name = "artikal_id")
    private int artikalId;

    @Column(name = "kolicina")
    private int kolicina;

    @Column(name = "jedinicna_cena")
    private double jedinicnaCena;

    public Stavka() {}

    public int getId() { return id; }
    public Narudzbina getNarudzbina() { return narudzbina; }
    public void setNarudzbina(Narudzbina v) { this.narudzbina = v; }
    public int getArtikalId() { return artikalId; }
    public void setArtikalId(int v) { this.artikalId = v; }
    public int getKolicina() { return kolicina; }
    public void setKolicina(int v) { this.kolicina = v; }
    public double getJedinicnaCena() { return jedinicnaCena; }
    public void setJedinicnaCena(double v) { this.jedinicnaCena = v; }
}