import java.io.*;
import java.util.*;
import Model.Instrucao;
import Model.Programa;

public class ProgramLoader {

    private static final List<String> COMANDOS_VALIDOS = Arrays.asList(
            "ADD", "SUB", "MULT", "DIV", "LOAD", "STORE",
            "BRANY", "BRPOS", "BRZERO", "BRNEG", "SYSCALL"
    );

    public List<Programa> listarProgramas() {
        File dir = new File("bin");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        List<Programa> programas = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                Programa programa = new Programa();
                programa.setNomeArquivo(file.getName());
                programas.add(programa);
            }
        }
        return programas;
    }

    public Programa carregarPrograma(String filename) throws IOException {
        Programa programa = new Programa();
        programa.setNomeArquivo(filename);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String linha;
            String secaoAtual = null;
            int numeroLinha = 0;

            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();
                System.out.println("DEBUG: Lendo linha: '" + linha + "'");

                if (linha.isEmpty()) continue;

                if (linha.equalsIgnoreCase(".code")) {
                    secaoAtual = "code";
                    continue;
                } else if (linha.equalsIgnoreCase(".data")) {
                    secaoAtual = "data";
                    continue;
                } else if (linha.equalsIgnoreCase(".endcode") || linha.equalsIgnoreCase(".enddata")) {
                    secaoAtual = null;
                    continue;
                }

                if ("code".equals(secaoAtual)) {
                    List<Instrucao> instrucoesDaLinha = parsearLinhaCodigo(linha, numeroLinha);

                    for (Instrucao inst : instrucoesDaLinha) {
                        programa.adicionarInstrucao(inst);
                    }
                    
                    if (instrucoesDaLinha.stream().anyMatch(inst -> !"LABEL".equals(inst.getTipo()))) {
                        numeroLinha++;
                    }
                } else if ("data".equals(secaoAtual)) {
                    parsearLinhaDados(linha, programa);
                }
            }
        }

        programa.processarLabels();
        return programa;
    }

    private List<Instrucao> parsearLinhaCodigo(String linha, int numeroLinha) {
        List<Instrucao> instrucoes = new ArrayList<>();

        if (linha.contains(":") && !linha.endsWith(":")) {
            int indexDoisPontos = linha.indexOf(":");
            String labelPart = linha.substring(0, indexDoisPontos).trim();
            String instrucaoPart = linha.substring(indexDoisPontos + 1).trim();

            // adiciona a pseudo-instrução LABEL
            instrucoes.add(new Instrucao("LABEL", null, null, labelPart, numeroLinha));
            
            // adiciona a instrucao real, se existir
            if (!instrucaoPart.isEmpty()) {
                instrucoes.add(parsearApenasInstrucao(instrucaoPart, numeroLinha));
            }
        } 

        // apenas label
        else if (linha.endsWith(":")) {
            String nomeLabel = linha.substring(0, linha.length() - 1).trim();
            instrucoes.add(new Instrucao("LABEL", null, null, nomeLabel, numeroLinha));
        } 
        // apenas instrucao real
        else {
            Instrucao inst = parsearApenasInstrucao(linha, numeroLinha);
            if (inst != null)
                instrucoes.add(inst);
        }
        return instrucoes;
    }
    
    private Instrucao parsearApenasInstrucao(String linha, int numeroLinha) {
        if (linha.isEmpty()) return null;

        String[] partes = linha.split("\\s+", 2);
        String comando = partes[0].toUpperCase();
        String operandos = partes.length > 1 ? partes[1].trim() : "";

        if (!COMANDOS_VALIDOS.contains(comando))
            throw new RuntimeException("Comando inválido na linha " + numeroLinha + ": " + comando);

        String modo = "direto";
        String operando = operandos;

        if (operandos.startsWith("#")) {
            modo = "imediato";
            operando = operandos.substring(1).trim();
        }

        if ("STORE".equals(comando) && "imediato".equals(modo))
            throw new RuntimeException("ERRO: STORE não suporta modo imediato");
        
        if ("SYSCALL".equals(comando)) {
            try {
                int syscallNum = Integer.parseInt(operando);
                if (syscallNum < 0 || syscallNum > 2) {
                    throw new RuntimeException("ERRO: SYSCALL inválido (deve ser 0, 1 ou 2)");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("ERRO: Operando do SYSCALL deve ser um número");
            }
        }

        return new Instrucao(comando, operando, modo, null, numeroLinha);
    }

    private void parsearLinhaDados(String linha, Programa programa) {
        String[] partes = linha.split("\\s+", 2);
        
        if (partes.length < 2) 
            return;

        String nomeVar = partes[0].trim();
        String valorStr = partes[1].trim();

        try {
            int valor = Integer.parseInt(valorStr);
            programa.adicionarDado(nomeVar, valor);
        } catch (NumberFormatException e) {
            System.err.println("Aviso: Valor inválido para '" + nomeVar + "': " + valorStr);
        }
    }
}