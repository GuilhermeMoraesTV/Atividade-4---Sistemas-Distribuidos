package eleicao;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceProcessoRMI extends Remote {

    //Entrega uma mensagem a este processo para que ele faça o processamento adequado

    void receberMensagem(Mensagem mensagem) throws RemoteException;
}