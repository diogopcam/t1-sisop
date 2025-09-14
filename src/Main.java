//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//import ProgramLoader;
import Model.Processo;
import Model.Programa;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            ProgramLoader loader = new ProgramLoader();
            List<Programa> programas = loader.listarProgramas();

            // if (!programas.isEmpty()) {
            //     System.out.println("Programas disponíveis:");
            //     for (Programa prog : programas) {
            //         System.out.println("- " + prog.getNomeArquivo());
            // }

            Escalonador escalonador = new Escalonador(); // instancia o escalonador
            int pidCounter = 1;

            for (Programa p : programas) {
                Programa programa = loader.carregarPrograma("./bin/" + p.getNomeArquivo());

                //////////////////////////////////////////////////////////////////////////////
                // TO DO: remover valores mockados
                Processo processo = new Processo(pidCounter, programa, 1, 5, 0);
                escalonador.admitirProcesso(processo);

                pidCounter++;
            }
            escalonador.executar();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
