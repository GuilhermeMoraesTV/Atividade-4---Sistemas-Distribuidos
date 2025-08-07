# Simulação de Algoritmos de Eleição: Bully e Anel

Este projeto, desenvolvido para a disciplina de Sistemas Distribuídos, consiste na simulação de um sistema distribuído com cinco processos autônomos. A aplicação demonstra e permite a análise comparativa de dois algoritmos de eleição clássicos: **Bully** e **Anel**.

O sistema foi projetado para observar como cada algoritmo lida com a tolerância a falhas, especificamente na eleição de um novo processo coordenador após a falha do líder atual. A comunicação entre os processos é implementada utilizando **Java RMI (Remote Method Invocation)**.

## Arquitetura do Sistema

O sistema é composto pelos seguintes componentes principais:

* **Processo (`Processo.java`):** A classe central que representa cada um dos 5 nós no sistema. Contém a lógica para ambos os algoritmos (Bully e Anel), a gestão do seu estado (ativo/inativo) e os métodos de comunicação.
* **Simulador (`Simulador.java`):** A classe principal (`main`) que orquestra a simulação. É responsável por iniciar o RMI Registry, criar e registrar os processos, e executar os cenários de falha predefinidos (A ou B).
* **Interface RMI (`InterfaceProcessoRMI.java`):** Define o contrato de comunicação remota, permitindo que um processo invoque o método `receberMensagem` em outro.
* **Mensagem (`Mensagem.java` e `TipoMensagem.java`):** Estruturas de dados que representam as mensagens trocadas, contendo o tipo (`ELEICAO`, `OK`, `COORDENADOR`, `PING`), o remetente e outros dados relevantes.

## Estrutura do Projeto

O código-fonte foi organizado para separar as fontes (`src`) das classes compiladas (`bin`), facilitando a gestão do projeto.

```
/
|
|-- src/
|   |-- eleicao/
|       |-- InterfaceProcessoRMI.java
|       |-- Mensagem.java
|       |-- Processo.java
|       |-- Simulador.java
|       |-- TipoMensagem.java
|
|-- bin/
|   |-- (pasta criada automaticamente após a compilação)
|
|-- COMPILAR.bat
|-- EXECUTAR_CENARIO_A.bat
`-- EXECUTAR_CENARIO_B.bat
```

## Como Compilar e Executar

Foram criados scripts (`.bat`) para automatizar o processo no Windows.

### Passo 1: Compilar o Projeto

Para compilar todo o código-fonte da pasta `src` e gerar os ficheiros `.class` na pasta `bin`, execute o script `COMPILAR.bat`.

> Dê um duplo clique no ficheiro: **`COMPILAR.bat`**

O script irá limpar compilações antigas, criar a lista de todos os ficheiros `.java` e compilá-los.

### Passo 2: Executar um Cenário de Simulação

Após a compilação, você pode executar qualquer um dos dois cenários de falha.

1.  **Execute o script do cenário desejado** (por exemplo, `EXECUTAR_CENARIO_A.bat`).
2.  O programa irá pedir-lhe para **escolher o algoritmo** a ser testado diretamente no console.
3.  Digite `BULLY` ou `ANEL` (não diferencia maiúsculas de minúsculas) e pressione Enter.
4.  A simulação para o cenário e algoritmo escolhidos começará.

#### Scripts Disponíveis:

* **`EXECUTAR_CENARIO_A.bat`**: Simula o **Cenário A**, onde o coordenador (P5) falha e, após uma nova eleição, ele se recupera e uma segunda eleição é iniciada.
* **`EXECUTAR_CENARIO_B.bat`**: Simula o **Cenário B**, um teste de estresse onde os dois processos de maior ID (P5 e P4) falham, forçando os processos restantes a elegerem um novo líder (P3).

## Entendendo a Saída (Log)

O console exibirá em tempo real a atividade de todos os cinco processos. Para analisar o comportamento de cada algoritmo, observe os seguintes eventos no log:

* **Deteção de Falha:** Uma linha como `[P_] Falha ao contatar P5. Considerado como FALHO.` indica que um processo detetou a ausência do coordenador.
* **Início da Eleição:** Procure por `[P_] Iniciou uma ELEIÇÃO (Bully).` ou `[P_] Iniciou uma ELEIÇÃO (Anel).`.
* **Mensagens da Eleição:**
    * **Bully:** Você verá um fluxo de mensagens `ELEICAO` e `OK`, muitas vezes de forma concorrente.
    * **Anel:** Você verá a mensagem `ELEICAO` a ser repassada de forma ordenada, com a lista de PIDs a crescer a cada passo.
* **Eleição do Novo Coordenador:** O log indicará claramente quando um processo vence a eleição (`[P_] Se declarou o novo COORDENADOR` para o Bully, ou `[P_] Eleição em Anel concluída. Novo coordenador será P_` para o Anel).
* **Anúncio e Confirmação:** Finalmente, todos os processos ativos confirmarão o novo líder com a mensagem `[P_] Novo coordenador definido: P_`.