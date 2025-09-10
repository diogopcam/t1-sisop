package Model;
import java.util.*;

public class Programa {
    private List<Instrucao> codigo;
    private Map<String, Integer> dados;
    private Map<String, Integer> labels;
    private String nomeArquivo;

    public Programa() {
        this.codigo = new ArrayList<>();
        this.dados = new HashMap<>();
        this.labels = new HashMap<>();
    }

    public void adicionarInstrucao(Instrucao instrucao) {
        codigo.add(instrucao);
    }

    public void adicionarDado(String nome, int valor) {
        dados.put(nome, valor);
    }

    public void adicionarLabel(String nome, int linha) {
        labels.put(nome, linha);
    }

    public List<Instrucao> getCodigo() { return codigo; }
    public Map<String, Integer> getDados() { return dados; }
    public Map<String, Integer> getLabels() { return labels; }
    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public void processarLabels() {
        // Primeira passada: mapear labels para números de linha
        for (int i = 0; i < codigo.size(); i++) {
            Instrucao inst = codigo.get(i);
            if ("LABEL".equals(inst.getTipo()) && inst.getLabel() != null) {
                labels.put(inst.getLabel(), i);
            }
        }

        // Segunda passada: substituir labels por números de linha
        for (Instrucao inst : codigo) {
            if (inst.getOperando() != null && labels.containsKey(inst.getOperando())) {
                int linhaAlvo = labels.get(inst.getOperando());
                inst.setOperando(String.valueOf(linhaAlvo));
            }
        }

        // Remover instruções LABEL (não são executáveis)
        codigo.removeIf(inst -> "LABEL".equals(inst.getTipo()));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PROGRAMA ===\n");
        sb.append("Código (").append(codigo.size()).append(" instruções):\n");
        for (Instrucao inst : codigo) {
            sb.append("  ").append(inst).append("\n");
        }

        sb.append("\nDados (").append(dados.size()).append(" variáveis):\n");
        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }

        sb.append("\nLabels (").append(labels.size()).append(" labels):\n");
        for (Map.Entry<String, Integer> entry : labels.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" → linha ").append(entry.getValue()).append("\n");
        }

        return sb.toString();
    }
}
