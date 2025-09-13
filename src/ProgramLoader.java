import java.io.*;
import java.util.*;
import Model.Instrucao;
import Model.Programa;

public class ProgramLoader {

    private static final List<String> COMANDOS_VALIDOS = Arrays.asList(
            "ADD", "SUB", "MULT", "DIV", "LOAD", "STORE",
            "BRANY", "BRPOS", "BRZERO", "BRNEG", "SYSCALL"
    );

    public List<Programa> listarProgramas(){
        File dir = new File("./bin");
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

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String linha;
        String secaoAtual = null;
        int numeroLinha = 0;

        while ((linha = reader.readLine()) != null) {
            linha = linha.trim();
            System.out.println("DEBUG: Lendo linha: '" + linha + "'");

            // Pular linhas vazias
            if (linha.isEmpty()) continue;

            // Identificar mudança de seção
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

            // Processar conforme a seção
            if ("code".equals(secaoAtual)) {
                Instrucao instrucao = parsearLinhaCodigo(linha, numeroLinha);
                if (instrucao != null) {
                    programa.adicionarInstrucao(instrucao);
                    numeroLinha++;
                }
            } else if ("data".equals(secaoAtual)) {
                parsearLinhaDados(linha, programa);
            }
        }

        reader.close();
        programa.processarLabels();
        return programa;
    }

    private Instrucao parsearLinhaCodigo(String linha, int numeroLinha) {
        System.out.println("DEBUG: Parseando linha: '" + linha + "'");

        // 1. Verificar se é label + instrução na mesma linha
        if (linha.contains(":") && !linha.endsWith(":")) {
            return parsearLinhaComLabel(linha, numeroLinha);
        }

        // 2. Verificar se é apenas label
        if (linha.endsWith(":")) {
            String nomeLabel = linha.substring(0, linha.length() - 1).trim();
            System.out.println("DEBUG: Label encontrado: '" + nomeLabel + "'");
            return new Instrucao("LABEL", null, null, nomeLabel, numeroLinha);
        }

        // 3. Processar instrução normal
        return parsearApenasInstrucao(linha, numeroLinha);
    }

    private Instrucao parsearApenasInstrucao(String linha, int numeroLinha) {
        System.out.println("DEBUG: Parseando apenas instrução: '" + linha + "'");

        if (linha.isEmpty()) return null;

        // Dividir comando e operandos
        String[] partes = linha.split("\\s+", 2);
        String comando = partes[0].toUpperCase();
        String operandos = partes.length > 1 ? partes[1].trim() : "";

        System.out.println("DEBUG: Comando: '" + comando + "', Operandos: '" + operandos + "'");

        // Validar comando
        if (!COMANDOS_VALIDOS.contains(comando)) {
            throw new RuntimeException("Comando inválido: " + comando);
        }

        // Determinar modo de endereçamento
        String modo = "direto";
        String operando = operandos;

        if (operandos.startsWith("#")) {
            modo = "imediato";
            operando = operandos.substring(1).trim();
        }

        // Validar STORE (não pode ser imediato)
        if ("STORE".equals(comando) && "imediato".equals(modo)) {
            throw new RuntimeException("ERRO: STORE não suporta modo imediato");
        }

        // Validar SYSCALL (deve ter operando numérico)
        if ("SYSCALL".equals(comando)) {
            try {
                int syscallNum = Integer.parseInt(operando);
                if (syscallNum < 0 || syscallNum > 2) {
                    throw new RuntimeException("ERRO: SYSCALL inválido (0-2)");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("ERRO: SYSCALL deve ter número");
            }
        }

        return new Instrucao(comando, operando, modo, null, numeroLinha);
    }

    private Instrucao parsearLinhaComLabel(String linha, int numeroLinha) {
        System.out.println("DEBUG: Linha com label e instrução: '" + linha + "'");

        // Encontrar a posição do :
        int indexDoisPontos = linha.indexOf(":");
        if (indexDoisPontos == -1) return null;

        // Separar label e instrução
        String labelPart = linha.substring(0, indexDoisPontos).trim();
        String instrucaoPart = linha.substring(indexDoisPontos + 1).trim();

        System.out.println("DEBUG: Label: '" + labelPart + "', Instrução: '" + instrucaoPart + "'");

        // Processar a instrução COMPLETA
        Instrucao instrucao = parsearApenasInstrucao(instrucaoPart, numeroLinha);
        if (instrucao != null) {
            return new Instrucao("LABEL", null, null, labelPart, numeroLinha);
        }

        return null;
    }

    private void parsearLinhaDados(String linha, Programa programa) {
        // Formato: nome valor
        String[] partes = linha.split("\\s+", 2);
        if (partes.length < 2) return;

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