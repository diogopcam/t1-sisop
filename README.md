**Requisitos**

    Java 17 ou superior instalado no sistema.
    
    Terminal ou prompt de comando.
    
**Como executar**

Abra o terminal e navegue até o diretório do projeto, onde o arquivo SimuladorSO.jar está localizado.

    cd /caminho/para/o/diretorio


Execute o simulador usando o comando: java -jar SimuladorSO.jar


O menu do simulador será exibido:

=== SIMULADOR SO (CLI) ===
1) Listar programas
2) Adicionar processo
3) Iniciar simulação (tempo real)
4) Passo único
5) Estado das filas
0) Sair

Interaja com o simulador digitando o número da opção desejada e pressionando Enter.

**Funcionalidades do Menu**

1) Listar programas: Mostra os programas disponíveis para adicionar como processos.

2) Adicionar processo: Permite escolher um programa para criar um processo e definir seu tempo de chegada, tipo (tempo real ou melhor esforço), prioridade e quantum.

3) Iniciar simulação (tempo real): Executa todos os processos de acordo com o escalonamento e tempos de chegada.

4) Passo único: Avança a simulação instrução por instrução, útil para análise detalhada.

5) Estado das filas: Exibe o estado atual das filas de processos: prontos, bloqueados, etc.

0) Sair: Encerra o simulador.