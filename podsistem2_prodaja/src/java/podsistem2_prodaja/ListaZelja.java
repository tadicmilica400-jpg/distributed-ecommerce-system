package podsistem2_prodaja;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "lista_zelja")
public class ListaZelja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "korisnik_id", unique = true)
    private int korisnikId;

    @Column(name = "datum_kreiranja")
    private java.sql.Date datumKreiranja;

    @OneToMany(mappedBy = "listaZelja", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ListaZeljaArtikal> stavke;

    public ListaZelja() {}

    public int getId() { return id; }
    public int getKorisnikId() { return korisnikId; }
    public void setKorisnikId(int korisnikId) { this.korisnikId = korisnikId; }
    public java.sql.Date getDatumKreiranja() { return datumKreiranja; }
    public void setDatumKreiranja(java.sql.Date d) { this.datumKreiranja = d; }
    public List<ListaZeljaArtikal> getStavke() { return stavke; }
}