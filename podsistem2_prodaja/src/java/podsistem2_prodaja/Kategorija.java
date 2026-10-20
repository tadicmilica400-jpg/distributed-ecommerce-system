package podsistem2_prodaja;

import javax.persistence.*;

@Entity
@Table(name = "kategorija")
public class Kategorija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "naziv", nullable = false)
    private String naziv;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Kategorija parent;

    public Kategorija() {}

    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
    public Kategorija getParent() { return parent; }
    public void setParent(Kategorija parent) { this.parent = parent; }
}