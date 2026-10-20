package podsistem3_transakcije;

import javax.persistence.*;

// Ova klasa postoji samo u podsistem3 da bi citali/menjali novac korisnika
@Entity
@Table(name = "korisnik")
public class Korisnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "novac")
    private double novac;

    public Korisnik() {}

    public int getId() { return id; }
    public double getNovac() { return novac; }
    public void setNovac(double novac) { this.novac = novac; }
}