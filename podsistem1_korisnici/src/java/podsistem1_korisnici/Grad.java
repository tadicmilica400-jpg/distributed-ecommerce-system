package podsistem1_korisnici;

import javax.persistence.*;

@Entity
@Table(name = "grad")
public class Grad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "naziv", nullable = false)
    private String naziv;

    public Grad() {}
    public Grad(String naziv) { this.naziv = naziv; }

    public int getId() { return id; }
    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
}