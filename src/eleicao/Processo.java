package eleicao;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Processo implements InterfaceProcessoRMI {

    private final int id;
    private int coordenadorId;
    private final List<Integer> todosPids;
    private AtomicBoolean ativo = new AtomicBoolean(true);
    private AtomicBoolean emEleicao = new AtomicBoolean(false);

    // Atributos específicos do Bully
    private AtomicBoolean respondeuOk = new AtomicBoolean(false);

    // Identificador do algoritmo a ser usado
    private final String algoritmo;

    // Construtor
    public Processo(int id, List<Integer> todosPids, String algoritmo) {
        this.id = id;
        this.todosPids = todosPids;
        this.coordenadorId = todosPids.stream().max(Integer::compareTo).orElse(this.id);
        this.algoritmo = algoritmo.toUpperCase();
    }

    // MÉTODOS DE CONTROLE
    public void setAtivo(boolean status) {
        this.ativo.set(status);
        if (!status) {
            System.out.printf("[P%d] Simulação de FALHA.%n", id);
        } else {
            System.out.printf("[P%d] Simulação de RECUPERAÇÃO.%n", id);
            iniciarEleicao(); // Um processo recuperado inicia uma eleição
        }
    }

    public boolean isAtivo() {
        return this.ativo.get();
    }

    public int getId() {
        return this.id;
    }

    // LÓGICAA DE COMUNICAÇÃO
    private void enviarMensagem(int idDestino, Mensagem mensagem) {
        if (!this.isAtivo() || idDestino == this.id) return;

        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
            InterfaceProcessoRMI stub = (InterfaceProcessoRMI) registry.lookup("Processo" + idDestino);
            stub.receberMensagem(mensagem);
        } catch (Exception e) {
            System.out.printf("[P%d] Falha ao contatar P%d. Considerado como FALHO.%n", id, idDestino);
            if (idDestino == this.coordenadorId) {
                iniciarEleicao();
            }
        }
    }

    //  MÉTODO CENTRAL PARA INICIAR ELEIÇÃO
    public void iniciarEleicao() {
        if ("BULLY".equals(algoritmo)) {
            iniciarEleicaoBully();
        } else if ("ANEL".equals(algoritmo)) {
            iniciarEleicaoAnel();
        }
    }


    @Override
    public void receberMensagem(Mensagem mensagem) {
        if (!this.isAtivo()) return;

        System.out.printf("[P%d] Recebeu: %s%n", id, mensagem);

        // LÓGICA DE PROCESSAMENTO DE MENSAGENS
        switch (mensagem.getTipo()) {
            case ELEICAO:
                if ("BULLY".equals(algoritmo)) {
                    processarEleicaoBully(mensagem);
                } else if ("ANEL".equals(algoritmo)) {
                    processarEleicaoAnel(mensagem);
                }
                break;
            case OK:
                if ("BULLY".equals(algoritmo)) {
                    this.respondeuOk.set(true);
                }
                break;
            case COORDENADOR:
                System.out.printf("[P%d] Novo coordenador definido: P%d%n", id, mensagem.getIdRemetente());
                this.coordenadorId = mensagem.getIdRemetente();
                this.emEleicao.set(false);
                break;
            case PING:
                break;
        }
    }

    // =================================================================
    //               LÓGICA DO ALGORITMO BULLY
    // =================================================================
    private void iniciarEleicaoBully() {
        if (!emEleicao.compareAndSet(false, true)) {
            System.out.printf("[P%d] Já está em um processo de eleição (Bully).%n", id);
            return;
        }
        System.out.printf("[P%d] Iniciou uma ELEIÇÃO (Bully).%n", id);
        this.respondeuOk.set(false);

        List<Integer> pidsMaiores = todosPids.stream().filter(p -> p > this.id).collect(Collectors.toList());
        if (pidsMaiores.isEmpty()) {
            anunciarCoordenadorBully();
            return;
        }
        for (int pidMaior : pidsMaiores) {
            enviarMensagem(pidMaior, new Mensagem(TipoMensagem.ELEICAO, this.id));
        }

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                if (!this.respondeuOk.get()) {
                    anunciarCoordenadorBully();
                } else {
                    System.out.printf("[P%d] Eleição Bully encerrada. Aguardando anúncio.%n", id);
                    this.emEleicao.set(false);
                }
            } catch (InterruptedException e) {}
        }).start();
    }

    private void processarEleicaoBully(Mensagem mensagem) {
        if (mensagem.getIdRemetente() < this.id) {
            enviarMensagem(mensagem.getIdRemetente(), new Mensagem(TipoMensagem.OK, this.id));
            iniciarEleicaoBully();
        }
    }

    private void anunciarCoordenadorBully() {
        System.out.printf("[P%d] Se declarou o novo COORDENADOR (Bully).%n", id);
        this.coordenadorId = this.id;
        this.emEleicao.set(false);
        this.respondeuOk.set(false);

        for (int pid : todosPids) {
            if (pid != this.id) {
                enviarMensagem(pid, new Mensagem(TipoMensagem.COORDENADOR, this.id));
            }
        }
    }

    // =================================================================
    //               LÓGICA DO ALGORITMO DE ANEL
    // =================================================================
    private int getProximoNoAnel() {
        // Encontra o próximo processo vivo no anel lógico
        List<Integer> pidsOrdenados = new ArrayList<>(todosPids);
        Collections.sort(pidsOrdenados);
        int currentIndex = pidsOrdenados.indexOf(this.id);

        // Tenta encontrar o próximo vivo, dando a volta no anel se necessário
        for (int i = 1; i <= pidsOrdenados.size(); i++) {
            int nextIndex = (currentIndex + i) % pidsOrdenados.size();
            int proximoId = pidsOrdenados.get(nextIndex);
            // Verifica se o próximo está vivo (não é o próprio processo)
            if (proximoId != this.id) {
                try {
                    Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
                    registry.lookup("Processo" + proximoId); // Tenta encontrar o processo
                    return proximoId; // Retorna se encontrou
                } catch (Exception e) {
                    // Se não encontrou, continua para o próximo
                }
            }
        }
        return this.id; // Caso seja o único vivo
    }

    private void iniciarEleicaoAnel() {
        if (!emEleicao.compareAndSet(false, true)) {
            System.out.printf("[P%d] Já está em um processo de eleição (Anel).%n", id);
            return;
        }
        System.out.printf("[P%d] Iniciou uma ELEIÇÃO (Anel).%n", id);
        List<Integer> pidsAtivosIniciais = new ArrayList<>();
        pidsAtivosIniciais.add(this.id); // Adiciona a si mesmo na lista

        int proximoId = getProximoNoAnel();
        enviarMensagem(proximoId, new Mensagem(TipoMensagem.ELEICAO, this.id, pidsAtivosIniciais));
    }

    private void processarEleicaoAnel(Mensagem mensagem) {
        // Adiciona seu próprio ID à lista de processos ativos
        List<Integer> pidsAtivosRecebidos = new ArrayList<>(mensagem.getPidsAtivos());
        pidsAtivosRecebidos.add(this.id);

        // Se a lista voltou para o remetente original, a eleição termina
        if (mensagem.getIdRemetente() == this.id) {
            // Se o ID deste processo está na lista, significa que ele deu a volta completa
            if (mensagem.getPidsAtivos().contains(this.id)) {
                // Eleição concluída. O processo com o maior ID na lista é o novo coordenador.
                int novoCoordenador = Collections.max(mensagem.getPidsAtivos());
                System.out.printf("[P%d] Eleição em Anel concluída. Novo coordenador será P%d%n", id, novoCoordenador);
                this.emEleicao.set(false);

                // Envia a mensagem de COORDENADOR para o próximo
                int proximoId = getProximoNoAnel();
                enviarMensagem(proximoId, new Mensagem(TipoMensagem.COORDENADOR, novoCoordenador));
            }
        } else {
            // Repassa a mensagem de eleição para o próximo nó no anel
            System.out.printf("[P%d] Repassando mensagem de eleição. PIDs na lista: %s%n", id, pidsAtivosRecebidos);
            int proximoId = getProximoNoAnel();
            enviarMensagem(proximoId, new Mensagem(TipoMensagem.ELEICAO, mensagem.getIdRemetente(), pidsAtivosRecebidos));
        }
    }

    // SIMULAÇÃO DE COMPORTAMENTO
    public void iniciar() {
        new Thread(() -> {
            while (true) {
                try {
                    if (this.isAtivo() && this.id != this.coordenadorId) {
                        Thread.sleep(5000 + (long) (Math.random() * 2000));
                        System.out.printf("[P%d] Pingando coordenador P%d...%n", id, this.coordenadorId);
                        enviarMensagem(this.coordenadorId, new Mensagem(TipoMensagem.PING, this.id));
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {}
            }
        }).start();
    }
}