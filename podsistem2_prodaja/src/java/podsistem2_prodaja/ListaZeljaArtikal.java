package podsistem2_prodaja;

import javax.persistence.*;

@Entity
@Table(name = "lista_zelja_artikal")
@IdClass(ListaZeljaArtikalId.class)
public class ListaZeljaArtikal {

    @Id
    @Column(name = "lista_zelja_id")
    private int listaZeljaId;

    @Id
    @Column(name = "artikal_id")
    private int artikalId;

    @Column(name = "vreme_dodavanja")
    private java.sql.Timestamp vremeDodavanja;

    @ManyToOne
    @JoinColumn(name = "lista_zelja_id", insertable = false, updatable = false)
    private ListaZelja listaZelja;

    @ManyToOne
    @JoinColumn(name = "artikal_id", insertable = false, updatable = false)
    private Artikal artikal;

    public ListaZeljaArtikal() {}

    public ListaZeljaArtikal(int listaZeljaId, int artikalId) {
        this.listaZeljaId = listaZeljaId;
        this.artikalId = artikalId;
        this.vremeDodavanja = new java.sql.Timestamp(System.currentTimeMillis());
    }

    public int getListaZeljaId() { return listaZeljaId; }
    public void setListaZeljaId(int listaZeljaId) { this.listaZeljaId = listaZeljaId; }
    public int getArtikalId() { return artikalId; }
    public void setArtikalId(int artikalId) { this.artikalId = artikalId; }
    public java.sql.Timestamp getVremeDodavanja() { return vremeDodavanja; }
    public void setVremeDodavanja(java.sql.Timestamp v) { this.vremeDodavanja = v; }
    public ListaZelja getListaZelja() { return listaZelja; }
    public Artikal getArtikal() { return artikal; }
}