package podsistem2_prodaja;

import javax.persistence.*;

@Entity
@Table(name = "korpa_artikal")
@IdClass(KorpaArtikalId.class)
public class KorpaArtikal {

    @Id
    @Column(name = "korpa_id")
    private int korpaId;

    @Id
    @Column(name = "artikal_id")
    private int artikalId;

    @Column(name = "kolicina")
    private int kolicina;

    @ManyToOne
    @JoinColumn(name = "korpa_id", insertable = false, updatable = false)
    private Korpa korpa;

    @ManyToOne
    @JoinColumn(name = "artikal_id", insertable = false, updatable = false)
    private Artikal artikal;

    public KorpaArtikal() {}

    public KorpaArtikal(int korpaId, int artikalId, int kolicina) {
        this.korpaId = korpaId;
        this.artikalId = artikalId;
        this.kolicina = kolicina;
    }

    public int getKorpaId() { return korpaId; }
    public void setKorpaId(int korpaId) { this.korpaId = korpaId; }
    public int getArtikalId() { return artikalId; }
    public void setArtikalId(int artikalId) { this.artikalId = artikalId; }
    public int getKolicina() { return kolicina; }
    public void setKolicina(int kolicina) { this.kolicina = kolicina; }
    public Korpa getKorpa() { return korpa; }
    public Artikal getArtikal() { return artikal; }
}