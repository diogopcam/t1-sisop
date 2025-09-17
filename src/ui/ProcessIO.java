package ui;

import Model.Processo;

public interface ProcessIO {
    String promptInput(Processo processo, String message);
    void print(Processo processo, String message);
}
