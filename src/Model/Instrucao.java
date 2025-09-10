package Model;

public class Instrucao {
    private String tipo;        // ADD, SUB, LOAD, STORE, etc.
    private String operando;    // operando ou valor
    private String modo;        // "imediato" ou "direto"
    private String label;       // para labels
    private int linhaOriginal;  // número da linha no arquivo

    public Instrucao(String tipo, String operando, String modo, String label, int linhaOriginal) {
        this.tipo = tipo;
        this.operando = operando;
        this.modo = modo;
        this.label = label;
        this.linhaOriginal = linhaOriginal;
    }

    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getOperando() { return operando; }
    public void setOperando(String operando) { this.operando = operando; }

    public String getModo() { return modo; }
    public void setModo(String modo) { this.modo = modo; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getLinhaOriginal() { return linhaOriginal; }
    public void setLinhaOriginal(int linhaOriginal) { this.linhaOriginal = linhaOriginal; }

    @Override
    public String toString() {
        return String.format("Instrucao[tipo=%s, operando=%s, modo=%s, label=%s, linha=%d]",
                tipo, operando, modo, label, linhaOriginal);
    }
}