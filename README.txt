IMPLEMENTADO (CONCLUÍDO)
PARSER E ESTRUTURAS BÁSICAS
ProgramLoader: Carregamento de programas a partir de arquivos

Interpretação completa de instruções: LOAD, STORE, ADD, SUB, MULT, DIV, SYSCALL, BRANY, BRPOS, BRZERO, BRNEG

Suporte a modos de endereçamento: direto e imediato (com #)

Processamento de labels e saltos

Validação de sintaxe e comandos

Separação das seções .code e .data

Classe Processo com PCB: PC, ACC, estado, prioridade, quantum, tempo de chegada

Memória individual por processo

Gerenciamento de estados: pronto, executando, bloqueado, finalizado

A IMPLEMENTAR (PRÓXIMAS ETAPAS)
PESSOA 2 - NÚCLEO DE EXECUÇÃO
Implementar método executarInstrucao() na classe Processo

Executar todas as operações aritméticas: ADD, SUB, MULT, DIV

Implementar operações de memória: LOAD (imediato/direto), STORE (apenas direto)

Implementar saltos condicionais: BRANY, BRPOS, BRZERO, BRNEG

Implementar SYSCALL:

0: finalizar processo

1: imprimir ACC (bloquear 3-5 unidades)

2: ler do teclado para ACC (bloquear 3-5 unidades)

Gerenciar bloqueio/desbloqueio de processos

PESSOA 3 - ESCALONADOR
Criar duas filas: processos de tempo real e melhor esforço

Implementar Round Robin para tempo real com 2 níveis de prioridade (0-alta, 1-baixa)

Implementar FCFS para melhor esforço

Gerenciar preempção quando processo de maior prioridade chegar

Controlar quantum (5 unidades por padrão)

Implementar troca de processos sem custo de contexto

PESSOA 4 - INTERFACE E CONTROLE PRINCIPAL
Desenvolver interface para carregar programas

Permitir definir: tempo de chegada, tipo de processo (tempo real/melhor esforço)

Para tempo real: permitir definir prioridade e quantum

Mostrar estado das filas e processos em tempo real

Implementar loop principal de simulação

Adicionar funcionalidade especial: digitar "0101" imprime "Ola"

TODOS - INTEGRAÇÃO E TESTES
Integrar todas as componentes

Testar com programas exemplo (como o da Figura 1)

Validar escalonamento e preempção

Testar bloqueio/desbloqueio por SYSCALL

Preparar documentação e manual do usuário