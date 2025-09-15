import Model.Processo;
import Model.Instrucao;

import java.util.*;

public class Escalonador {
    private Queue<Processo> filaRTPrioridade0;  
    private Queue<Processo> filaRTPrioridade1;    
    private Queue<Processo> filaMelhorEsforco;  
    private List<Processo> bloqueados;    
    private List<Processo> aguardando; // processos de melhor esforco que nao atingiram tempo de chegada     
    private int tempoGlobal;                    

    public Escalonador() {
        this.filaRTPrioridade0 = new LinkedList<>();
        this.filaRTPrioridade1 = new LinkedList<>();
        this.filaMelhorEsforco = new LinkedList<>();
        this.bloqueados = new ArrayList<>();
        this.aguardando = new ArrayList<>();
        this.tempoGlobal = 0;
    }

    public void admitirProcesso(Processo processo) {
        processo.setEstado("pronto");
    
        if (processo.getPrioridade() == 0) {
            filaRTPrioridade0.add(processo);
            System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + processo.getPid() + " inserido na fila de tempo real. Prioridade 0.");
        } else if (processo.getPrioridade() == 1) {
            filaRTPrioridade1.add(processo);
            System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + processo.getPid() + " inserido na fila de tempo real. Prioridade 1.");
        } else {
            filaMelhorEsforco.add(processo);
            System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + processo.getPid() + " inserido na fila de melhor esforço.");
        }
    }

    public void iniciarSimulacao() {
        Timer timer = new Timer();
        TimerTask tarefaCiclo = new TimerTask() {
            @Override
            public void run() {
                if (!todasFilasVazias())
                    executarCiclo();
                else {
                    System.out.println("\nCPU: ##### Simulação finalizada em " + tempoGlobal + " ciclos #####");
                    timer.cancel();
                    return;
                }
            }
        };
        timer.scheduleAtFixedRate(tarefaCiclo, 0, 1000);
    }

    private void executarCiclo() {
        System.out.println("\nCPU: ##### CICLO DE CLOCK: " + tempoGlobal + " #####");
        tickBloqueios();
        tickProcessosAguardando();

        Processo processoAtual = escolherProcesso();

        if (processoAtual != null) {
            processoAtual.setEstado("executando");
            System.out.println("CPU: Executando processo " + processoAtual.getPid() + " (Prioridade: " + processoAtual.getPrioridade() + ")");

            int quantumDoProcesso = 0;
            int instrucoesExecutadas = 0;
            boolean foiPreemptado = false;
            boolean isTempoReal = processoAtual.getPrioridade() == 0 || processoAtual.getPrioridade() == 1;

            if (isTempoReal)
                quantumDoProcesso = processoAtual.getQuantum();
            else
                 quantumDoProcesso = Integer.MAX_VALUE; // sem quantum definido para melhor esforco

            while (instrucoesExecutadas < quantumDoProcesso) {
                if (processoAtual.getPrioridade() == 1 && !filaRTPrioridade0.isEmpty()) {
                    System.out.println("CPU: [PREEMPÇÃO]: Processo " + processoAtual.getPid() + " (Prioridade 1) preemptado pelo processo na fila de prioridade 0.");
                    foiPreemptado = true;
                    break;
                }

                if (!isTempoReal && (!filaRTPrioridade0.isEmpty() || !filaRTPrioridade1.isEmpty())) {
                    System.out.println("CPU: [PREEMPÇÃO] Processo " + processoAtual.getPid() + " (Melhor Esforço) preemptado por processo de Tempo Real.");
                    foiPreemptado = true;
                    break;
                }

                Instrucao instrucao = processoAtual.getProximaInstrucao();
                if (instrucao == null || processoAtual.isFinalizado())
                    break;

                processoAtual.executarInstrucao(instrucao);
                instrucoesExecutadas++;

                if (processoAtual.getEstado().equals("bloqueado") || processoAtual.isFinalizado())
                    break;
            }

            if (foiPreemptado){
                admitirProcesso(processoAtual);
                System.out.println("CPU: [RESULTADO]: Processo " + processoAtual.getPid() + " preemptado e retornando à fila.");
            } else if (processoAtual.isFinalizado())
                System.out.println("CPU: [RESULTADO]: Processo " + processoAtual.getPid() + " finalizado.");
            else if (processoAtual.getEstado().equals("bloqueado")) {
                System.out.println("CPU: [RESULTADO]: Processo " + processoAtual.getPid() + " bloqueado.");
                bloqueados.add(processoAtual);
            } else if (isTempoReal && instrucoesExecutadas >= quantumDoProcesso) {
                System.out.println("CPU: [RESULTADO]: Quantum do processo " + processoAtual.getPid() + " esgotado. Retornando à fila.");
                admitirProcesso(processoAtual);
            } else if (!isTempoReal)
                admitirProcesso(processoAtual); 

        } else {
            System.out.println("CPU: Nenhum processo pronto para executar.");
        }

        tempoGlobal++;
    }

    private Processo escolherProcesso() {
        if (!filaRTPrioridade0.isEmpty())
            return filaRTPrioridade0.poll();
        else if (!filaRTPrioridade1.isEmpty())
            return filaRTPrioridade1.poll();
        else if (!filaMelhorEsforco.isEmpty())
            return filaMelhorEsforco.poll();

        return null;
    }

    private void tickBloqueios() {
        List<Processo> temp = new ArrayList<>();

        for (Processo bloqueado : bloqueados) {
            bloqueado.tickBloqueio();
            if (bloqueado.getEstado().equals("pronto")) {
                temp.add(bloqueado);
                System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + bloqueado.getPid() + " desbloqueado.");
            }
        }
        bloqueados.removeAll(temp);
        for (Processo processo : temp)
            admitirProcesso(processo);
    }

    public void tickProcessosAguardando() {
        List<Processo> temp = new ArrayList<>();

        for (Processo futuro : aguardando) {
            if (futuro.getTempoChegada() <= tempoGlobal) {
                temp.add(futuro);
                admitirProcesso(futuro);
                System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + futuro.getPid() + " chegou e foi admitido.");
            }
        }
        aguardando.removeAll(temp);
    }

    private boolean todasFilasVazias() {
        return filaRTPrioridade0.isEmpty()
                && filaRTPrioridade1.isEmpty() 
                && filaMelhorEsforco.isEmpty() 
                && bloqueados.isEmpty()
                && aguardando.isEmpty();
    }
}