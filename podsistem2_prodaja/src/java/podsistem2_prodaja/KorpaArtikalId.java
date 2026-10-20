package podsistem2_prodaja;

import java.io.Serializable;
import java.util.Objects;

public class KorpaArtikalId implements Serializable {
    private int korpaId;
    private int artikalId;

    public KorpaArtikalId() {}
    public KorpaArtikalId(int korpaId, int artikalId) {
        this.korpaId = korpaId;
        this.artikalId = artikalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KorpaArtikalId)) return false;
        KorpaArtikalId that = (KorpaArtikalId) o;
        return korpaId == that.korpaId && artikalId == that.artikalId;
    }

    @Override
    public int hashCode() { return Objects.hash(korpaId, artikalId); }
}