import Model.Processo;
import Model.Programa;
import java.util.*;
import ui.ConsoleIO;
import ui.ProcessIO;

public class Main {
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        ProgramLoader loader = new ProgramLoader();
        Escalonador escalonador = new Escalonador();
        ProcessIO io = new ConsoleIO();
        int nextPid = 1;

        while (true) {
            System.out.println("\n=== SIMULADOR SO (CLI) ===");
            System.out.println("1) Listar programas");
            System.out.println("2) Adicionar processo");
            System.out.println("3) Iniciar simulação (tempo real)");
            System.out.println("4) Passo único");
            System.out.println("5) Estado das filas");
            System.out.println("0) Sair");
            System.out.print("> ");
            String op = in.nextLine().trim();
            try {
                switch (op) {
                    case "1": listarProgramas(loader); break;
                    case "2": nextPid = adicionarProcesso(loader, escalonador, io, nextPid); break;
                    case "3": rodarSimulacaoTempoReal(escalonador); break;
                    case "4": escalonador.step(); break;
                    case "5": escalonador.imprimirSnapshot(); break;
                    case "0": System.out.println("Encerrando."); return;
                    default: System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void listarProgramas(ProgramLoader loader) throws Exception {
        List <Programa> programas = loader.listarProgramas();
        if (programas.isEmpty()) { System.out.println("Nenhum .txt encontrado em ./bin."); return; }
        System.out.println("Programas disponíveis:");
        int i = 1; 
        for (Programa p : programas) System.out.println((i++) + ") " + p.getNomeArquivo());
    }

    private static int adicionarProcesso(ProgramLoader loader, Escalonador escalonador, ProcessIO io, int nextPid) throws Exception {
        List<Programa> programas = loader.listarProgramas();
        if (programas.isEmpty()) { System.out.println("Nenhum programa disponível."); return nextPid; }
        for (int i = 0; i < programas.size(); i++) System.out.println((i + 1) + ") " + programas.get(i).getNomeArquivo());
        System.out.print("Escolha o programa (#): ");
        int idx = Integer.parseInt(in.nextLine().trim());
        if (idx < 1 || idx > programas.size()) { System.out.println("Índice inválido."); return nextPid; }
        String nome = programas.get(idx - 1).getNomeArquivo();
        Programa programa = loader.carregarPrograma("bin/" + nome);

        System.out.print("Tempo de chegada (t>=0): ");
        int chegada = Integer.parseInt(in.nextLine().trim());

        System.out.print("Tipo (TR=tempo real / BE=melhor esforço) [TR/BE]: ");
        String tipo = in.nextLine().trim().toUpperCase(Locale.ROOT);
        int prioridade; int quantum;
        if (tipo.equals("TR")) {
            System.out.print("Prioridade (0=alta, 1=baixa) [1]: ");
            String pr = in.nextLine().trim();
            prioridade = pr.isEmpty() ? 1 : Integer.parseInt(pr);
            System.out.print("Quantum (>0) [5]: ");
            String q = in.nextLine().trim();
            quantum = q.isEmpty() ? 5 : Integer.parseInt(q);
        } else {
            prioridade = 2;                 // Melhor Esforço
            quantum = Integer.MAX_VALUE;    // sem quantum
        }

        Processo p = new Processo(nextPid, programa, prioridade, quantum, chegada);
        p.setIO(io);
        escalonador.agendarProcesso(p);
        System.out.println("Adicionado PID=" + nextPid + " (" + nome + ")");
        return nextPid + 1;
    }

    private static void rodarSimulacaoTempoReal(Escalonador escalonador) throws InterruptedException {
        System.out.println("Iniciando simulação. Pressione Ctrl+C para interromper.");
        while (!escalonador.isVazio()) {
            escalonador.step();
            Thread.sleep(1000); // 1s por ciclo
        }
        System.out.println("Simulação concluída.");
    }
}

