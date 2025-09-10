package Model;
import java.util.*;

public class Processo {
    private int pid;
    private int pc;                 // Contador de Programa
    private int acc;                // Acumulador
    private String estado;          // pronto, executando, bloqueado, finalizado
    private int prioridade;         // 0 = alta, 1 = baixa
    private int quantum;
    private int tempoChegada;
    private Programa programa;
    private Map<String, Integer> memoria; // Cópia dos dados do programa

    public Processo(int pid, Programa programa, int prioridade, int quantum, int tempoChegada) {
        this.pid = pid;
        this.pc = 0;
        this.acc = 0;
        this.estado = "pronto";
        this.prioridade = prioridade;
        this.quantum = quantum;
        this.tempoChegada = tempoChegada;
        this.programa = programa;
        this.memoria = new HashMap<>(programa.getDados()); // Cópia dos dados
    }

    // Getters e Setters
    public int getPid() { return pid; }
    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc; }
    public int getAcc() { return acc; }
    public void setAcc(int acc) { this.acc = acc; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getPrioridade() { return prioridade; }
    public int getQuantum() { return quantum; }
    public int getTempoChegada() { return tempoChegada; }
    public Programa getPrograma() { return programa; }
    public Map<String, Integer> getMemoria() { return memoria; }

    public Instrucao getProximaInstrucao() {
        if (pc < programa.getCodigo().size()) {
            return programa.getCodigo().get(pc);
        }
        return null;
    }

    public void incrementarPc() {
        pc++;
    }

    public int getValorMemoria(String variavel) {
        return memoria.getOrDefault(variavel, 0);
    }

    public void setValorMemoria(String variavel, int valor) {
        memoria.put(variavel, valor);
    }

    public boolean isFinalizado() {
        return pc >= programa.getCodigo().size();
    }

    @Override
    public String toString() {
        return String.format("Processo[pid=%d, pc=%d, acc=%d, estado=%s, prioridade=%d]",
                pid, pc, acc, estado, prioridade);
    }
}