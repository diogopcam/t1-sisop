package Model;
import java.util.*;
import ui.ProcessIO;

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
    private transient ProcessIO io;

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
    public void setIO(ProcessIO io) { this.io = io; }
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
    public int getTempoDeBloqueio() { return tempoDeBloqueio; }

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
        log("Processo bloqueado por " + tempoDeBloqueio + " unidades de tempo.");
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

    private void log(String msg) {
        System.out.println("PROCESSO " + pid + ": " + msg);
        if (io != null) io.print(this, msg);
    }

    public void executarInstrucao(Instrucao instrucao) {
        if (instrucao == null) { estado = "finalizado"; 
        log("Não tem mais instruções para executar."); 
        return; 
        }
        log("Executando: " + instrucao);
        String tipo = instrucao.getTipo();
        String operando = instrucao.getOperando();
        String modo = instrucao.getModo();

        if (tipo.equals("ADD")||tipo.equals("SUB")||tipo.equals("MULT")||tipo.equals("DIV")) executarInstrAritmetica(tipo, operando, modo);
        else if (tipo.equals("LOAD")||tipo.equals("STORE")) executarInstrMemoria(tipo, operando, modo);
        else if (tipo.equals("BRANY")||tipo.equals("BRPOS")||tipo.equals("BRZERO")||tipo.equals("BRNEG")) executarSalto(tipo, operando);
        else if (tipo.equals("SYSCALL")) executarSyscall(operando);
        else log("Instrução desconhecida: " + tipo);

        if (isFinalizado()) { 
            estado = "finalizado"; 
            log("Finalizou a execução."); 
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
                        log("Erro: Divisão por zero");
                        return;
                    }
                    break;
                default:
                    log("Operação aritmética desconhecida: " + operacao);
                    return;
            }

            incrementarPc();

        } catch (NumberFormatException e) {
            log("Erro: Operando inválido para " + operacao + ": " + operando);
        }
    }

    public void executarSyscall(String operando) {
        try {
            int syscallNum = Integer.parseInt(operando);

            if (syscallNum < 0 || syscallNum > 2) {
                log("Erro: Número de SYSCALL inválido: " + syscallNum);  // [ALTERADO]
                return;
            }

            if (syscallNum == 0) {
                estado = "finalizado";
                log("Solicitou saída (exit).");                           // [ALTERADO]
                incrementarPc();
                return;
            } else if (syscallNum == 1 || syscallNum == 2) {
                if (syscallNum == 1) {
                    log("Solicitou escrita do conteudo do acumulador (write): " + acc); 
                    if (io != null) io.print(this, "Conteúdo do Acumulador: " + acc);   
                } else {
                    String prompt = "Digite um valor inteiro para o acumulador: ";
                    String entrada = null;
                    if (io != null) entrada = io.promptInput(this, prompt);            
                    if (entrada == null) {
                        Scanner sc = new Scanner(System.in);                           
                        entrada = sc.nextLine();
                    }
                    if ("0101".equals(entrada.trim())) {                               
                        if (io != null) io.print(this, "Ola");
                        System.out.println("Ola");
                    }
                    try {
                        acc = Integer.parseInt(entrada.trim());
                    } catch (NumberFormatException nfe) {
                        log("Entrada inválida. Mantendo ACC = " + acc);                
                    }
                    log("Conteúdo do Acumulador: " + acc);                             
                }

                bloquear();                                                            
                incrementarPc();
                return;
            } else {
                log("SYSCALL desconhecido: " + syscallNum);                           
            }

        } catch (NumberFormatException e) {
            log("Erro: Operando de SYSCALL inválido: " + operando);                    
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
                        log("Erro: STORE não pode usar modo imediato");
                    else {
                        setValorMemoria(operando, acc);
                        incrementarPc();
                    }
            } else
                log("Operação de memória desconhecida: " + operacao);
        }

        catch (NumberFormatException e) {
            log("Erro: Operando inválido para LOAD imediato: " + operando);
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
            log("Condição de salto não satisfeita para " + tipo); 
            
        if (deveSaltar) {
            try {
                // estamos considerando que o processamento das labels ja foi feito
                // logo, o operando eh sempre um numero de linha (nao uma label)
                int linhaAlvo = Integer.parseInt(operando);

                if (linhaAlvo >= 0 && linhaAlvo < programa.getCodigo().size()) {
                    pc = linhaAlvo;
                    log("Saltou para a linha " + linhaAlvo); 
                } else
                    log("Erro: Linha de salto fora do alcance: " + linhaAlvo);

            } catch (NumberFormatException e) {
                log("Erro: Operando de salto inválido: " + operando);
            }
        } else 
            incrementarPc();
    }
}