import Model.Instrucao;
import Model.Processo;
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

    public int getTempoGlobal() { return tempoGlobal; }

    public void admitirProcesso(Processo processo) {
        processo.setEstado("pronto");
        switch (processo.getPrioridade()) {
            case 0:
                filaRTPrioridade0.add(processo);
                break;
            case 1:
                filaRTPrioridade1.add(processo);
                break;
            default:
                filaMelhorEsforco.add(processo);
                break;
        }
        System.out.println(
            "CPU: [Tempo " + tempoGlobal + "] Admitido PID " + processo.getPid()
            + " (prio=" + processo.getPrioridade() + ")"
        );                                                                
    }

    public void agendarProcesso(Processo p) {                             
        if (p.getTempoChegada() <= tempoGlobal) {
            admitirProcesso(p);
            System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + p.getPid() + " chegou e foi admitido.");
        } else {
            aguardando.add(p);
            System.out.println("CPU: [Tempo " + tempoGlobal + "] Processo " + p.getPid() + " agendado para t=" + p.getTempoChegada());
        }
    }

    public boolean isVazio() {                                             // [NOVO] Ajuda a controlar loop no CLI
        return filaRTPrioridade0.isEmpty() && filaRTPrioridade1.isEmpty()
            && filaMelhorEsforco.isEmpty() && bloqueados.isEmpty() && aguardando.isEmpty();
    }

    public void step() { executarCiclo(); } 

    // public void iniciarSimulacao() {
    //     Timer timer = new Timer();
    //     TimerTask tarefaCiclo = new TimerTask() {
    //         @Override
    //         public void run() {
    //             if (!todasFilasVazias())
    //                 executarCiclo();
    //             else {
    //                 System.out.println("\nCPU: ##### Simulação finalizada em " + tempoGlobal + " ciclos #####");
    //                 timer.cancel();
    //                 return;
    //             }
    //         }
    //     };
    //     timer.scheduleAtFixedRate(tarefaCiclo, 0, 1000);
    // }

    private void executarCiclo() {
        System.out.println("\nCPU: ##### CICLO DE CLOCK: " + tempoGlobal + " #####");
        tickBloqueios();
        tickProcessosAguardando();
        Processo processoAtual = escolherProcesso();
        if (processoAtual != null) {
            processoAtual.setEstado("executando");
            System.out.println("CPU: Executando PID " + processoAtual.getPid() + " (Prio: " + processoAtual.getPrioridade() + ")");
            int quantumDoProcesso = (processoAtual.getPrioridade()==0 || processoAtual.getPrioridade()==1)
                    ? processoAtual.getQuantum() : Integer.MAX_VALUE;       
            int exec = 0; boolean preemptado = false; boolean isTR = (processoAtual.getPrioridade()==0 || processoAtual.getPrioridade()==1);
            while (exec < quantumDoProcesso) {
                if (processoAtual.getPrioridade()==1 && !filaRTPrioridade0.isEmpty()) {
                    System.out.println("CPU: [PREEMPÇÃO] PID " + processoAtual.getPid() + " por TR prio 0");
                    preemptado = true; break;                              
                }
                if (!isTR && (!filaRTPrioridade0.isEmpty() || !filaRTPrioridade1.isEmpty())) {
                    System.out.println("CPU: [PREEMPÇÃO] PID " + processoAtual.getPid() + " por TR");
                    preemptado = true; break;                               
                }
                Instrucao inst = processoAtual.getProximaInstrucao();
                if (inst == null || processoAtual.isFinalizado()) break;
                processoAtual.executarInstrucao(inst);
                exec++;
                if (processoAtual.getEstado().equals("bloqueado") || processoAtual.isFinalizado()) break;
            }
            if (preemptado) {
                admitirProcesso(processoAtual);
                System.out.println("CPU: [RESULTADO] PID " + processoAtual.getPid() + " preemptado."); 
            } else if (processoAtual.isFinalizado()) {
                System.out.println("CPU: [RESULTADO] PID " + processoAtual.getPid() + " finalizado."); 
            } else if (processoAtual.getEstado().equals("bloqueado")) {
                System.out.println("CPU: [RESULTADO] PID " + processoAtual.getPid() + " bloqueado.");  
                bloqueados.add(processoAtual);
            } else if (isTR && exec >= quantumDoProcesso) {
                System.out.println("CPU: [RESULTADO] Quantum esgotado para PID " + processoAtual.getPid()); 
                admitirProcesso(processoAtual);
            } else if (!isTR) {
                admitirProcesso(processoAtual);                           
            }
        } else {
            System.out.println("CPU: Nenhum processo pronto para executar.");
        }
        tempoGlobal++;
        imprimirSnapshot();                                         
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

    public void imprimirSnapshot() {                                     
        System.out.println("-- FILAS @ t=" + tempoGlobal + " --");
        System.out.println("RT-0: " + listaDetalhada(filaRTPrioridade0));
        System.out.println("RT-1: " + listaDetalhada(filaRTPrioridade1));
        System.out.println("BE  : " + listaDetalhada(filaMelhorEsforco));
        System.out.print("BLK : [");
        for (int i=0;i<bloqueados.size();i++) {
            Processo p = bloqueados.get(i);
            System.out.print("PID=" + p.getPid() + "(pc=" + p.getPc() + ",acc=" + p.getAcc() + ",restante=" + p.getTempoDeBloqueio() + ")");
            if (i<bloqueados.size()-1) System.out.print(", ");
        }
        System.out.println("]");
    }

    private String listaDetalhada(Queue<Processo> q) {                  
        StringBuilder sb = new StringBuilder("[");
        Iterator<Processo> it = q.iterator();
        while (it.hasNext()) {
            Processo p = it.next();
            sb.append("PID=").append(p.getPid())
              .append("(pc=").append(p.getPc())
              .append(",acc=").append(p.getAcc())
              .append(",estado=").append(p.getEstado())
              .append(")");
            if (it.hasNext()) sb.append(", ");
        }
        return sb.append("]").toString();
    }

    // private boolean todasFilasVazias() {
    //     return filaRTPrioridade0.isEmpty()
    //             && filaRTPrioridade1.isEmpty() 
    //             && filaMelhorEsforco.isEmpty() 
    //             && bloqueados.isEmpty()
    //             && aguardando.isEmpty();
    // }
}