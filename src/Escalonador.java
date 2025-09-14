import Model.Processo;
import Model.Instrucao;

import java.util.*;

public class Escalonador {
    private Queue<Processo> filaTempoReal;      // prioridade mais alta
    private Queue<Processo> filaMelhorEsforco;  // prioridade mais baixa
    private List<Processo> bloqueados;          // processos bloqueados
    private int tempoGlobal;                    // relogio do sistema

    public Escalonador() {
        this.filaTempoReal = new LinkedList<>();
        this.filaMelhorEsforco = new LinkedList<>();
        this.bloqueados = new ArrayList<>();
        this.tempoGlobal = 0;
    }

    public void admitirProcesso(Processo processo) {
        if (processo.getPrioridade() == 0)
            filaTempoReal.add(processo);
        else
            filaMelhorEsforco.add(processo);
    }

    public void executar() {
        while (!todasFilasVazias()) {
            tickBloqueios(); // atualiza o estado dos processos bloqueados
            Processo processo = escolherProcesso();

            if (!(processo == null)) {
                Instrucao instrucao = processo.getProximaInstrucao();

                if (instrucao != null)
                    processo.executarInstrucao(instrucao);

                if (processo.isFinalizado())
                    System.out.println("[Tempo " + tempoGlobal + "] Processo " + processo.getPid() + " finalizado.");
                else if (processo.getEstado().equals("bloqueado")) {
                    System.out.println("[Tempo " + tempoGlobal + "] Processo " + processo.getPid() + " bloqueado.");
                    bloqueados.add(processo);
                } else {
                    processo.setEstado("pronto");
                    admitirProcesso(processo);
                }
            } else
                System.out.println("[Tempo " + tempoGlobal + "] Nenhum processo pronto. CPU ociosa.");

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

        // readmite processos desbloqueados
        bloqueados.removeAll(temp);
        for (Processo processo : temp)
            admitirProcesso(processo);
    }     

    private boolean todasFilasVazias() {
        return filaTempoReal.isEmpty() && filaMelhorEsforco.isEmpty() && bloqueados.isEmpty();
    }
}
