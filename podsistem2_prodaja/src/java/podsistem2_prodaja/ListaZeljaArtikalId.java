package podsistem2_prodaja;

import java.io.Serializable;
import java.util.Objects;

public class ListaZeljaArtikalId implements Serializable {
    private int listaZeljaId;
    private int artikalId;

    public ListaZeljaArtikalId() {}
    public ListaZeljaArtikalId(int listaZeljaId, int artikalId) {
        this.listaZeljaId = listaZeljaId;
        this.artikalId = artikalId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListaZeljaArtikalId)) return false;
        ListaZeljaArtikalId that = (ListaZeljaArtikalId) o;
        return listaZeljaId == that.listaZeljaId && artikalId == that.artikalId;
    }

    @Override
    public int hashCode() { return Objects.hash(listaZeljaId, artikalId); }
}