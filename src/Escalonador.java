import Model.Processo;
import Model.Instrucao;
import java.util.*;

public class Escalonador {
    private Queue<Processo> filaTempoReal;
    private Queue<Processo> filaMelhorEsforco;
    private List<Processo> bloqueados;
    private int tempoGlobal;

    public Escalonador() {
        this.filaTempoReal = new LinkedList<>();
        this.filaMelhorEsforco = new LinkedList<>();
        this.bloqueados = new ArrayList<>();
        this.tempoGlobal = 0;
    }

    public void admitirProcesso(Processo processo) {
        if (processo.getPrioridade() == 0) {
            filaTempoReal.add(processo);
        } else {
            filaMelhorEsforco.add(processo);
        }
    }

    public void executar() {
        while (!todasFilasVazias()) {
            tickBloqueios();

            Processo processoAtual = escolherProcesso();

            if (processoAtual != null) {
                if (processoAtual.getPrioridade() != 0 && !filaTempoReal.isEmpty()) {
                    System.out.println("[Tempo " + tempoGlobal + "] Processo " + processoAtual.getPid() + " preemptado por um processo de tempo real.");
                    processoAtual.setEstado("pronto");
                    admitirProcesso(processoAtual);
                    
                    processoAtual = null;
                } else {
                    System.out.println("[Tempo " + tempoGlobal + "] Executando Processo " + processoAtual.getPid());
                    processoAtual.setEstado("executando");
                    
                    int instrucoesExecutadasNoQuantum = 0;
                    
                    int quantumDoProcesso;
                    if (processoAtual.getPrioridade() == 0) {
                        quantumDoProcesso = processoAtual.getQuantum();
                    } else {
                        quantumDoProcesso = Integer.MAX_VALUE;
                    }

                    while (instrucoesExecutadasNoQuantum < quantumDoProcesso) {
                        Instrucao instrucao = processoAtual.getProximaInstrucao();
                        if (instrucao == null) break;

                        processoAtual.executarInstrucao(instrucao);
                        instrucoesExecutadasNoQuantum++;

                        if (processoAtual.getEstado().equals("bloqueado") || processoAtual.isFinalizado()) {
                            break;
                        }

                        if (processoAtual.getPrioridade() != 0 && !filaTempoReal.isEmpty()) {
                            System.out.println("[Tempo " + tempoGlobal + "] Processo " + processoAtual.getPid() + " preemptado por um processo de tempo real.");
                            processoAtual.setEstado("pronto");
                            admitirProcesso(processoAtual);
                            processoAtual = null;
                            break;
                        }
                    }

                    if (processoAtual != null && !processoAtual.isFinalizado() && !processoAtual.getEstado().equals("bloqueado")) {
                        if (processoAtual.getPrioridade() == 0 && instrucoesExecutadasNoQuantum >= quantumDoProcesso) {
                            System.out.println("[Tempo " + tempoGlobal + "] Quantum de " + processoAtual.getPid() + " esgotado. Voltando para a fila.");
                        }
                        processoAtual.setEstado("pronto");
                        admitirProcesso(processoAtual);
                    } else if (processoAtual != null && processoAtual.isFinalizado()) {
                        System.out.println("[Tempo " + tempoGlobal + "] Processo " + processoAtual.getPid() + " finalizado.");
                    } else if (processoAtual != null && processoAtual.getEstado().equals("bloqueado")) {
                        System.out.println("[Tempo " + tempoGlobal + "] Processo " + processoAtual.getPid() + " bloqueado.");
                        bloqueados.add(processoAtual);
                    }
                }
            } else {
                System.out.println("[Tempo " + tempoGlobal + "] Nenhum processo pronto. CPU ociosa.");
            }

            tempoGlobal++;
        }
        System.out.println("Todos os processos finalizados.");
    }

    private Processo escolherProcesso() {
        if (!filaTempoReal.isEmpty()) {
            return filaTempoReal.poll();
        } else if (!filaMelhorEsforco.isEmpty()) {
            return filaMelhorEsforco.poll();
        }
        return null;
    }

    private void tickBloqueios() {
        List<Processo> temp = new ArrayList<>();
        for (Processo bloqueado : bloqueados) {
            bloqueado.tickBloqueio();
            if (bloqueado.getEstado().equals("pronto")) {
                temp.add(bloqueado);
                System.out.println("[Tempo " + tempoGlobal + "] Processo " + bloqueado.getPid() + " desbloqueado.");
            }
        }
        bloqueados.removeAll(temp);
        for (Processo processo : temp)
            admitirProcesso(processo);
    }

    private boolean todasFilasVazias() {
        return filaTempoReal.isEmpty() && filaMelhorEsforco.isEmpty() && bloqueados.isEmpty();
    }
}