package eleicao;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Simulador {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Erro: Cenario nao especificado. Use 'A' ou 'B'.");
            return;
        }
        String cenario = args[0].toUpperCase();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Qual algoritmo deseja simular? (BULLY ou ANEL)");
        String algoritmo = scanner.nextLine().toUpperCase();
        scanner.close();

        if (!algoritmo.equals("BULLY") && !algoritmo.equals("ANEL")) {
            System.out.println("Erro: Algoritmo invalido.");
            return;
        }

        System.out.printf("== Iniciando Simulacao do Algoritmo %s (Cenario %s) ==%n", algoritmo, cenario);

        final int NUM_PROCESSOS = 5;
        List<Processo> processos = new ArrayList<>();
        List<Remote> stubs = new ArrayList<>();
        List<Integer> todosPids = new ArrayList<>();

        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("Serviço de Registro RMI iniciado na porta 1099.");

            for (int i = 1; i <= NUM_PROCESSOS; i++) {
                todosPids.add(i);
            }

            for (int pid : todosPids) {
                Processo p = new Processo(pid, todosPids, algoritmo);
                InterfaceProcessoRMI stub = (InterfaceProcessoRMI) UnicastRemoteObject.exportObject(p, 0);
                registry.bind("Processo" + pid, stub);
                processos.add(p);
                stubs.add(stub);
                System.out.printf("Processo P%d registrado no RMI.%n", pid);
            }

            System.out.println("\n--- Simulação Iniciada ---");
            System.out.println("Todos os processos estão ativos. O coordenador inicial é P" + NUM_PROCESSOS);
            processos.forEach(Processo::iniciar);

            // Executa o cenario escolhido
            if ("A".equals(cenario)) {
                executarCenarioA(processos, stubs, registry);
            } else if ("B".equals(cenario)) {
                executarCenarioB(processos, registry);
            }

        } catch (Exception e) {
            System.err.println("Erro no Simulador: " + e.toString());
            e.printStackTrace();
            // Garante que o processo termine em caso de erro
            System.exit(1);
        }
    }

    private static void executarCenarioA(List<Processo> processos, List<Remote> stubs, Registry registry) throws Exception {
        System.out.println("\n--- SIMULANDO CENÁRIO A: FALHA E RECUPERAÇÃO DO COORDENADOR ---");
        Thread.sleep(8000);

        Processo coordenador = processos.get(processos.size() - 1);
        System.out.printf("%n>>> AÇÃO: Coordenador P%d irá falhar agora. <<< %n", coordenador.getId());

        coordenador.setAtivo(false);
        registry.unbind("Processo" + coordenador.getId());
        System.out.printf("[Simulador] Processo P%d DESREGISTRADO do RMI.%n", coordenador.getId());

        Thread.sleep(10000);
        System.out.printf("%n>>> AÇÃO: Antigo coordenador P%d irá se recuperar. <<< %n", coordenador.getId());

        Remote stubDoCoordenador = stubs.get(processos.size() - 1);
        registry.bind("Processo" + coordenador.getId(), stubDoCoordenador);
        coordenador.setAtivo(true);
        System.out.printf("[Simulador] Processo P%d REREGISTRADO no RMI.%n", coordenador.getId());
    }

    private static void executarCenarioB(List<Processo> processos, Registry registry) throws Exception {
        System.out.println("\n--- SIMULANDO CENÁRIO B: FALHA DE MÚLTIPLOS PROCESSOS ---");
        Thread.sleep(8000);

        Processo p5 = processos.get(4);
        System.out.printf("%n>>> AÇÃO: Coordenador P%d irá falhar. <<< %n", p5.getId());
        p5.setAtivo(false);
        registry.unbind("Processo" + p5.getId());
        System.out.printf("[Simulador] Processo P%d DESREGISTRADO do RMI.%n", p5.getId());

        Thread.sleep(2000);

        Processo p4 = processos.get(3);
        System.out.printf("%n>>> AÇÃO: Processo P%d também irá falhar. <<< %n", p4.getId());
        p4.setAtivo(false);
        registry.unbind("Processo" + p4.getId());
        System.out.printf("[Simulador] Processo P%d DESREGISTRADO do RMI.%n", p4.getId());

        System.out.println("\n>>> Aguardando eleição entre os processos restantes (P1, P2, P3)... <<<");
    }
}