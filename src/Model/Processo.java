package Model;
import java.util.*;

public class Processo {
    // variaveis do processo
    private int pid;
    private int pc;                       // contador de programa
    private int acc;                      // acumulador
    private String estado;                // pronto, executando, bloqueado, finalizado
    private int prioridade;               // 0 = alta, 1 = baixa
    private int quantum;
    private int tempoChegada;
    private Programa programa;
    private int tempoDeBloqueio;          // tempo restante de bloqueio
    private Map<String, Integer> memoria; // copia dos dados do programa
    Scanner scanner = new Scanner(System.in);

    public Processo(int pid, Programa programa, int prioridade, int quantum, int tempoChegada) {
        this.pid = pid;
        this.pc = 0;
        this.acc = 0;
        this.estado = "pronto";
        this.prioridade = prioridade;
        this.quantum = quantum;
        this.tempoChegada = tempoChegada;
        this.programa = programa;
        this.memoria = new HashMap<>(programa.getDados());
    }

    // getters e setters
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

    public void bloquear(/* int unidadesTempo */) {
        estado = "bloqueado";
        tempoDeBloqueio = new Random().nextInt(3) + 3; // 3 a 5 unidades (3,4,5)
    }

    public void tickBloqueio() {
        if (estado.equals("bloqueado") && tempoDeBloqueio > 0) {
            tempoDeBloqueio--;

            if (tempoDeBloqueio == 0)
                estado = "pronto";
        }
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

    public void executarInstrucao(Instrucao instrucao) {
        if (instrucao == null) {
            estado = "finalizado";
            System.out.println("PROCESSO " + pid + ":" + " Não tem mais instruções para executar.");
            return;
        }

        System.out.println("PROCESSO " + pid + ":" + " Executando: " + instrucao);
        String tipo = instrucao.getTipo();
        String operando = instrucao.getOperando();
        String modo = instrucao.getModo();

        if (tipo.equals("ADD" )|| tipo.equals("SUB") || tipo.equals("MULT") || tipo.equals("DIV"))
            executarInstrAritmetica(tipo, operando, modo);
        else if (tipo.equals("LOAD") || tipo.equals("STORE"))
            executarInstrMemoria(tipo, operando, modo);
        else if (tipo.equals("BRANY") || tipo.equals("BRPOS") || tipo.equals("BRZERO") || tipo.equals("BRNEG"))
            executarSalto(tipo, operando);
        else if (tipo.equals("SYSCALL"))
            executarSyscall(operando);
        else 
            System.out.println("PROCESSO " + pid +  ":" + " Instrução desconhecida: " + tipo);

        if (isFinalizado()) {
            estado = "finalizado";
            System.out.println("PROCESSO " + pid + ":" + " Finalizou a execução.");
        }
    }

    public void executarInstrAritmetica(String operacao, String operando, String modo) {
        try {
            int valor;
            if (modo.equalsIgnoreCase("IMEDIATO")) {
                valor = Integer.parseInt(operando);
            } else {
                valor = getValorMemoria(operando);
            }

            switch (operacao) {
                case "ADD":
                    acc += valor;
                    break;
                case "SUB":
                    acc -= valor;
                    break;
                case "MULT":
                    acc *= valor;
                    break;
                case "DIV":
                    if (valor != 0) {
                        acc /= valor;
                    } else {
                        System.out.println("PROCESSO " + pid +  ":" + " Erro: Divisão por zero");
                        return;
                    }
                    break;
                default:
                    System.out.println("PROCESSO " + pid +  ":" + " Operação aritmética desconhecida: " + operacao);
                    return;
            }

            incrementarPc();

        } catch (NumberFormatException e) {
            System.out.println("PROCESSO " + pid + ":" + " Erro: Operando inválido para " + operacao + ": " + operando);
        }
    }

    public void executarSyscall(String operando) {
        try {
            int syscallNum = Integer.parseInt(operando);

            if (syscallNum < 0 || syscallNum > 2) {
                System.out.println("PROCESSO " + pid + ":" + "Erro: Número de SYSCALL inválido: " + syscallNum);
                return;
            }

            if (syscallNum == 0) {
                estado = "finalizado";
                System.out.println("PROCESSO " + pid + ":" + " Solicitou saída (exit).");

                incrementarPc();
                return;
            } else if (syscallNum == 1 || syscallNum == 2) {
                bloquear(); // chama o bloqueio do processo

                if (syscallNum == 1)
                    System.out.println("PROCESSO " + pid + ":" + " Solicitou leitura do conteudo do acumulador (read).");
                else {
                    System.out.println("PROCESSO " + pid + ":" + " Solicitou escrita do conteudo do acumulador (write).\n" +
                                       "Digite um valor inteiro para o acumulador: ");
                    acc = scanner.nextInt();
                }

                System.out.println("PROCESSO " + pid +  ":" + " Conteúdo do Acumulador: " + acc);    
                System.out.println("PROCESSO " + pid +  ":" + " Processo bloqueado por " + tempoDeBloqueio + " unidades de tempo." );

                incrementarPc();
                return;
            } else
                System.out.println("PROCESSO " + pid +  ":" + " SYSCALL desconhecido: " + syscallNum);

        } catch (NumberFormatException e) {
            System.out.println("PROCESSO " + pid +  ":" + " Erro: Operando de SYSCALL inválido: " + operando);
        }   
    }

     public void executarInstrMemoria(String operacao, String operando, String modo) {
        try {    
            if (operacao.equals("LOAD")){
                if (modo.equals("IMEDIATO"))
                    acc = Integer.parseInt(operando);
                else
                    acc = getValorMemoria(operando);
                incrementarPc();
            } else if (operacao.equals("STORE")){
                    if (modo.equals("IMEDIATO"))
                        System.out.println("PROCESSO " + pid +  ":" + " Erro: STORE não pode usar modo imediato");
                    else {
                        setValorMemoria(operando, acc);
                        incrementarPc();
                    }
            } else
                System.out.println("PROCESSO " + pid +  ":" + " Operação de memória desconhecida: " + operacao);
        }

        catch (NumberFormatException e) {
            System.out.println("PROCESSO " + pid +  ":" + "Erro: Operando inválido para LOAD imediato: " + operando);
        }
    }

    public void executarSalto(String tipo, String operando) {
        boolean deveSaltar = false;

        if (tipo.equals("BRANY")) 
            deveSaltar = true;
        else if (tipo.equals("BRPOS") && acc > 0) 
            deveSaltar = true;
        else if (tipo.equals("BRZERO") && acc == 0) 
            deveSaltar = true;
        else if (tipo.equals("BRNEG") && acc < 0) 
            deveSaltar = true;
        else
            System.out.println("PROCESSO " + pid +  ":" + " Condição de salto não satisfeita para " + tipo);
            
        if (deveSaltar) {
            try {
                // estamos considerando que o processamento das labels ja foi feito
                // logo, o operando eh sempre um numero de linha (nao uma label)
                int linhaAlvo = Integer.parseInt(operando);

                if (linhaAlvo >= 0 && linhaAlvo < programa.getCodigo().size()) {
                    pc = linhaAlvo;
                    System.out.println("PROCESSO " + pid + ":" + " Saltou para a linha " + linhaAlvo);
                } else
                    System.out.println("PROCESSO " + pid +  ":" + " Erro: Linha de salto fora do alcance: " + linhaAlvo);

            } catch (NumberFormatException e) {
                System.out.println("PROCESSO " + pid +  ":" + " Erro: Operando de salto inválido: " + operando);
            }
        } else 
            incrementarPc();
    }
}