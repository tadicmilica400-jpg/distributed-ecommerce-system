package podsistem3_transakcije;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "transakcija")
public class Transakcija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "suma")
    private double suma;

    @Column(name = "vreme_placanja")
    private Timestamp vremePlacanja;

    @OneToOne
    @JoinColumn(name = "narudzbina_id")
    private Narudzbina narudzbina;

    public Transakcija() {}

    public int getId() { return id; }
    public double getSuma() { return suma; }
    public void setSuma(double v) { this.suma = v; }
    public Timestamp getVremePlacanja() { return vremePlacanja; }
    public void setVremePlacanja(Timestamp v) { this.vremePlacanja = v; }
    public Narudzbina getNarudzbina() { return narudzbina; }
    public void setNarudzbina(Narudzbina v) { this.narudzbina = v; }
}