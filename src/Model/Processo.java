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

    public void executarInstrucao() {
        if (isFinalizado()) {
            estado = "finalizado";
            System.out.println("Processo " + pid + " já está finalizado.");
            return;
        }

        Instrucao instrucao = getProximaInstrucao();
        if (instrucao == null) {
            estado = "finalizado";
            System.out.println("Processo " + pid + " não tem mais instruções para executar.");
            return;
        }

        System.out.println("Processo " + pid + " executando: " + instrucao);
        String tipo = instrucao.getTipo();
        String operando = instrucao.getOperando();
        String modo = instrucao.getModo();

        if (tipo == "ADD" || tipo == "SUB" || tipo == "MULT" || tipo == "DIV")
            executarInstrAritmetica(tipo, operando, modo);
        else if (tipo == "LOAD" || tipo == "STORE")
            executarInstrMemoria(tipo, operando, modo);
        else if (tipo == "BRANY" || tipo == "BRPOS" || tipo == "BRZERO" || tipo == "BRNEG")
            executarSalto(tipo, operando);
        else if (tipo == "SYSCALL")
            executarSyscall(operando);
        else 
            System.out.println("Instrução desconhecida: " + tipo);
        incrementarPc();

        if (isFinalizado()) {
            estado = "finalizado";
            System.out.println("Processo " + pid + " finalizou a execução.");
        }
    }

    public void executarInstrAritmetica(String operacao, String operando, String modo) {
        if (operacao == "ADD"){
            if (modo == "imediato")
                acc += Integer.parseInt(operando);
            else
                acc += getValorMemoria(operando);
        }  
        
        if (operacao == "SUB"){
            if (modo == "imediato")
                acc -= Integer.parseInt(operando);
            else
                acc -= getValorMemoria(operando);
        }
        
        if (operacao == "MULT"){
            if (modo == "imediato")
                acc *= Integer.parseInt(operando);
            else
                acc *= getValorMemoria(operando);
        }
        
        if (operacao == "DIV"){
            if (modo == "imediato") {
                int val = Integer.parseInt(operando);
                
                if (val != 0) 
                    acc /= val;
                else 
                    System.out.println("Erro: Divisão por zero");
            } else {
                int val = getValorMemoria(operando);
                
                if (val != 0) 
                    acc /= val;
                else 
                    System.out.println("Erro: Divisão por zero");
            } 
        } 
    }

    public void executarSyscall(String operando) {
        int syscallNum = Integer.parseInt(operando);
        try {
            switch (syscallNum) {
                case 0:
                    System.out.println("Processo " + pid + " solicitou saída (exit).");
                    estado = "finalizado";
                    break;

                case 1:
                    System.out.println("Processo " + pid + " solicitou leitura do conteudo do acumulador (read).");
                    System.out.println("Acumulador atualizado: " + acc);
                    break;
                // 2: ler do teclado para ACC (bloquear 3-5 unidades)
                // case 2:
                //     System.out.println("Processo " + pid + " solicitou escrita do conteudo do acumulador (write).");
                //     System.out.println("Conteúdo do Acumulador: " + acc);
                //     break;

                default:
                    System.out.println("SYSCALL desconhecido: " + syscallNum);
            }

        } catch (Exception e) {
            if (e instanceof NumberFormatException)
                System.out.println("Erro: Operando SYSCALL inválido: " + operando);
            else
                System.out.println("Erro ao executar SYSCALL: " + e.getMessage());
        }
    }

     public void executarInstrMemoria(String operacao, String operando, String modo) {
        try {
            if (operacao == "LOAD"){
                if (modo == "imediato")
                    acc = Integer.parseInt(operando);
                else
                    acc = getValorMemoria(operando);
            } else if (operacao == "STORE"){
                    if (modo == "imediato")
                        System.out.println("Erro: STORE não pode usar modo imediato");
                    else
                        setValorMemoria(operando, acc);
            } else
                System.out.println("Operação de memória desconhecida: " + operacao);
        }

        catch (NumberFormatException e) {
            System.out.println("Erro: Operando inválido para LOAD imediato: " + operando);
        }
    }

    public void executarSalto(String tipo, String operando) {
        boolean deveSaltar = false;

        if (tipo == "BRANY") 
            deveSaltar = true;
        else if (tipo == "BRPOS" && acc > 0) 
            deveSaltar = true;
        else if (tipo == "BRZERO" && acc == 0) 
            deveSaltar = true;
        else if (tipo == "BRNEG" && acc < 0) 
            deveSaltar = true;

        if (deveSaltar) {
            try {
                int linhaAlvo = Integer.parseInt(operando);

                if (linhaAlvo >= 0 && linhaAlvo < programa.getCodigo().size()) {
                    pc = linhaAlvo;
                    System.out.println("Processo " + pid + " saltou para a linha " + linhaAlvo);
                } else
                    System.out.println("Erro: Linha de salto fora do alcance: " + linhaAlvo);

            } catch (NumberFormatException e) {
                System.out.println("Erro: Operando de salto inválido: " + operando);
            }
        }
    }
}