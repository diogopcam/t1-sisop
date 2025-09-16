// File: src/ui/ConsoleIO.java
package ui;

import Model.Processo;
import java.util.Scanner;

public class ConsoleIO implements ProcessIO {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public synchronized String promptInput(Processo processo, String message) {
        System.out.print("[PID=" + processo.getPid() + "] " + message);
        String s = scanner.nextLine();
        return s == null ? "" : s;
    }

    @Override
    public synchronized void print(Processo processo, String message) {
        System.out.println("[PID=" + processo.getPid() + "] " + message);
    }
}

