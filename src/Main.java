//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//import ProgramLoader;
import Model.Programa;
import Model.Processo;

public class Main {
    public static void main(String[] args) {
        ProgramLoader loader = new ProgramLoader();

        try {
            // Pessoa 1: Carregar programa
            Programa programa = loader.carregarPrograma("exemplo.txt");
            System.out.println("✅ Parser funcionando!");
            System.out.println(programa);

            // Pessoa 2: Criar processo executável
            Processo processo = new Processo(1, programa, 0, 5, 0);
            System.out.println("\n✅ Processo criado!");
            System.out.println(processo);

            // Mostrar primeira instrução
            if (processo.getProximaInstrucao() != null) {
                System.out.println("\n📋 Primeira instrução: " + processo.getProximaInstrucao());
            }

        } catch (Exception e) {
            System.err.println("❌ Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}