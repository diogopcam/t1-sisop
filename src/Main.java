//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
//import ProgramLoader;
import Model.Programa;

import java.util.List;
import java.util.Scanner;

import Model.Instrucao;
import Model.Processo;

public class Main {
    public static void main(String[] args) {
        try {

            Scanner scanner = new Scanner(System.in);
            ProgramLoader loader = new ProgramLoader();

            // TO DO: Remover teste
            List<Programa> programas = loader.listarProgramas();
            System.out.println("Programas disponíveis:");
            for (Programa prog : programas) {
                System.out.println("- " + prog.getNomeArquivo());
            }
            //

            System.out.println("\nSelecione um programa para carregar (ex: exemplo.txt):");
            String dirPrograma = scanner.nextLine();
            scanner.close();

            // Pessoa 1: Carregar programa
            Programa programa = loader.carregarPrograma("./bin/" + dirPrograma);
            System.out.println("Parser funcionando!");
            System.out.println(programa);

            // Pessoa 2: Criar processo executável
            Processo processo = new Processo(1, programa, 0, 5, 0);
            System.out.println("\nProcesso criado!");
            System.out.println(processo);

            // TO DO: Remover teste
            while (!processo.isFinalizado()) {
                Instrucao instrucao = processo.getProximaInstrucao();
            
                if (instrucao != null) {
                    processo.executarInstrucao(instrucao);
                    processo.setPc(processo.getPc() + 1); // incrementa o pc
                } else {
                    break; // finalizou a execucao
                }
            }
            //

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}