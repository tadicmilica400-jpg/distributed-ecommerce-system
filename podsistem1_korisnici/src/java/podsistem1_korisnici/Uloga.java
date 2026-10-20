package podsistem1_korisnici;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "uloga")
public class Uloga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "naziv", nullable = false)
    private String naziv;

    @ManyToMany(mappedBy = "uloge")
    private List<Korisnik> korisnici;

    public Uloga() {}

    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
}