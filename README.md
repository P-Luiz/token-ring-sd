# Simulação de Anel Token Ring com UDP

## Sobre o projeto

Este projeto é o trabalho prático da disciplina de **Sistemas Distribuídos** do curso de Ciência da Computação. O objetivo é implementar uma topologia Token Ring formada por um **processo Mestre** e três **processos Escravos**, usando `DatagramSocket` e `DatagramPacket` em Java.

A comunicação deve ser feita exclusivamente por **UDP** e o token deve circular no sentido horário. O Mestre inicia o valor do token em `2`, os quatro nós aplicam suas operações a cada passagem e o Mestre informa o valor final após todas as voltas.

> Documento da especificação: [`docs/Trabalho_SD_2026-1a2d1828fb114a5baa3bfc8993492a55.pdf`](docs/Trabalho_SD_2026-1a2d1828fb114a5baa3bfc8993492a55.pdf)

## 1. Arquitetura

```text
                    sentido horário
          ┌───────────────────────────────┐
          │                               ▼
[ Mestre 0 / 5000 ] ───► [ Escravo 1 / 5001 ]
          ▲                    │
          │                    ▼
[ Escravo 3 / 5003 ] ◄─── [ Escravo 2 / 5002 ]
```

- **Mestre (`ID 0`):** inicia o token com `2`, controla o número de voltas e apresenta o resultado final.
- **Escravo 1 (`ID 1`):** recebe do Mestre, aplica sua operação e envia ao Escravo 2.
- **Escravo 2 (`ID 2`):** recebe do Escravo 1, aplica sua operação e envia ao Escravo 3.
- **Escravo 3 (`ID 3`):** recebe do Escravo 2, aplica sua operação e envia de volta ao Mestre.
- Todos os processos podem ser iniciados em máquinas diferentes da mesma rede. Nesse caso, o quarto argumento informa o IP do próximo nó.

## 2. Regras do funcionamento

O Mestre inicia o token em `2`. A cada recepção, o nó aplica a operação indicada em seu argumento:

| Operação | Ação | Exemplo |
|---|---|---|
| `0` | Soma `1` ao token | `10 → 11` |
| `1` | Multiplica o token por `2` | `10 → 20` |

Uma volta completa percorre todos os nós nesta ordem:

```text
Mestre → Escravo 1 → Escravo 2 → Escravo 3 → Mestre
```

O Mestre usa o número de voltas recebido em `args[2]` para repetir o ciclo. Os Escravos devem continuar em execução até terem participado do número de voltas definido. Deve haver exatamente o mesmo número de voltas nos quatro processos.

### Exemplos obrigatórios

| Operações dos nós | Voltas | Fluxo por volta | Resultado final |
|---|---:|---|---:|
| Todas `0` | 1 | `2 +1+1+1` no Mestre | `6` |
| Todas `1` | 1 | `2×2×2×2×2` | `32` |
| Todas `0` | 2 | Duas voltas completas | `10` |

Nos exemplos, o Mestre também aplica sua própria operação ao receber o token no final de cada volta.

## 3. Argumentos de linha de comando

```text
java Classe <ID> <OPERACAO> <VOLTAS> [IP_PROXIMO_NO]
```

| Posição | Nome | Valores | Descrição |
|---|---|---|---|
| `args[0]` | ID | `0` para Mestre; `1`, `2` ou `3` para Escravos | Identifica o processo e determina a porta local. |
| `args[1]` | Operação | `0` ou `1` | `0` soma 1; `1` multiplica por 2. |
| `args[2]` | Voltas | Inteiro maior ou igual a `1` | Quantidade de ciclos completos no anel. |
| `args[3]` | IP opcional | IP ou nome de máquina | IP do próximo nó no sentido horário. Usa `localhost` quando omitido. |

### Regras de validação

- O Mestre só pode usar `ID 0`.
- Escravos só podem usar os IDs `1`, `2` e `3`.
- `OPERACAO` deve ser `0` ou `1`.
- `VOLTAS` deve ser um inteiro maior ou igual a `1`.
- Devem ser informados pelo menos três argumentos.
- `N` e a operação devem ser combinados corretamente antes da execução conjunta.
- Em caso de argumento inválido, o processo deve imprimir uma mensagem clara e encerrar sem `NullPointerException`, `NumberFormatException` ou socket indefinido.

## 4. Portas e endereçamento

| Nó | ID | Porta local | Porta do próximo nó |
|---|---:|---:|---:|
| Mestre | 0 | 5000 | 5001 |
| Escravo 1 | 1 | 5001 | 5002 |
| Escravo 2 | 2 | 5002 | 5003 |
| Escravo 3 | 3 | 5003 | 5000 |

A porta local é calculada por `5000 + id`. O nó seguinte é calculado pela mesma fórmula, exceto no Nó 3, que volta para a porta `5000` do Mestre.



## 5. Como compilar

Em ambiente Linux ou Windows com Java instalado:

```bash
cd src
javac *.java
```

Se houver arquivos `.class` depois da compilação, o `.gitignore` já está configurado para não enviá-los ao repositório.

## 6. Execução local

Abra quatro processos em quatro terminais separados. Inicie os Escravos antes do Mestre.

### Cenário A — incremento, uma volta

```bash
# Terminal 1
java Escravo 1 0 1

# Terminal 2
java Escravo 2 0 1

# Terminal 3
java Escravo 3 0 1

# Terminal 4
java Mestre 0 0 1
```

Resultado esperado no Mestre: `6`.

### Cenário B — multiplicação, uma volta

```bash
java Escravo 1 1 1
java Escravo 2 1 1
java Escravo 3 1 1
java Mestre 0 1 1
```

Resultado esperado no Mestre: `32`.

### Cenário C — incremento, duas voltas

```bash
java Escravo 1 0 2
java Escravo 2 0 2
java Escravo 3 0 2
java Mestre 0 0 2
```

Resultado esperado no Mestre: `10`.

## 7. Execução distribuída em computadores diferentes

Todos os computadores devem estar na mesma rede e aceitar UDP nas portas `5000` a `5003`. Configure o firewall antes dos testes.

```bash
# Máquina do Escravo 1: próximo nó é o Escravo 2
java Escravo 1 0 2 192.168.1.12

# Máquina do Escravo 2: próximo nó é o Escravo 3
java Escravo 2 0 2 192.168.1.13

# Máquina do Escravo 3: próximo nó é o Mestre
java Escravo 3 0 2 192.168.1.10

# Máquina do Mestre: próximo nó é o Escravo 1
java Mestre 0 0 2 192.168.1.11
```

É obrigatório que os quatro comandos usem o mesmo valor de operação e o mesmo número de voltas. Nos exemplos locais, o último argumento pode ser omitido.



