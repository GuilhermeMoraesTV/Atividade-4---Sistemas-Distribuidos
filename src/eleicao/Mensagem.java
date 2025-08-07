package eleicao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Mensagem implements Serializable {
    private final TipoMensagem tipo;
    private final int idRemetente;
    // Usado no algoritmo de Anel para passar a lista de IDs
    private final List<Integer> pidsAtivos;

    public Mensagem(TipoMensagem tipo, int idRemetente) {
        this.tipo = tipo;
        this.idRemetente = idRemetente;
        this.pidsAtivos = new ArrayList<>();
    }

    public Mensagem(TipoMensagem tipo, int idRemetente, List<Integer> pidsAtivos) {
        this.tipo = tipo;
        this.idRemetente = idRemetente;
        this.pidsAtivos = pidsAtivos;
    }

    public TipoMensagem getTipo() { return tipo; }
    public int getIdRemetente() { return idRemetente; }
    public List<Integer> getPidsAtivos() { return pidsAtivos; }

    @Override
    public String toString() {
        return String.format("Msg[Tipo=%s, Remetente=%d, PIDs=%s]", tipo, idRemetente, pidsAtivos);
    }
}