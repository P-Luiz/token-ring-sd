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

## 5. Estrutura planejada dos arquivos

```text
token-ring-sd/
├── src/
│   ├── RedeUDP.java
│   ├── Configuracao.java
│   ├── Mestre.java
│   └── Escravo.java
├── scripts/
│   ├── testar_local.sh
│   └── testar_local.bat
├── docs/
│   └── Trabalho_SD_2026-1a2d1828fb114a5baa3bfc8993492a55.pdf
├── .gitignore
└── README.md
```

Todos os arquivos Java devem ficar diretamente na raiz de `src/`, sem declaração `package`, para permitir a compilação e execução exigidas pelo professor:

```bash
cd src
javac *.java
java Mestre 0 0 1
java Escravo 1 0 1
```

## 6. Contrato entre as classes

Os participantes devem preservar estas assinaturas públicas para que as implementações possam ser integradas sem conflitos:

### `src/Configuracao.java`

```java
public static void validarArgs(String[] args, boolean isMestre)
public static int getMinhaPorta(int id)
public static int getPortaProximoNo(int id)
public static String getIpProximoNo(String[] args)
public static int processarToken(int tokenAtual, int operacao)
```

- `getMinhaPorta(0)` deve retornar `5000` e `getMinhaPorta(3)` deve retornar `5003`.
- `getPortaProximoNo(3)` deve retornar `5000`; nos demais IDs, `5000 + id + 1`.
- Se `args[3]` existir e não estiver vazio, `getIpProximoNo` deve retorná-lo; caso contrário, retorna `"localhost"`.
- `processarToken` chama `tokenAtual + 1` quando a operação é `0` e `tokenAtual * 2` quando é `1`.

### `src/RedeUDP.java`

```java
public static void enviarToken(DatagramSocket socket, int token,
                              String ipDestino, int portaDestino) throws IOException
public static int receberToken(DatagramSocket socket) throws IOException
```

- O socket, o buffer de entrada e o ciclo de espera devem permanecer sob controle dos processos Mestre e Escravo.
- `enviarToken` não pode fechar o socket recebido como parâmetro.
- `receberToken` deve aguardar um único datagrama e converter o conteúdo recebido em `int`.
- Os métodos não devem abrir nem fechar sockets internamente, pois os processos reutilizam o socket durante várias voltas.

## 7. Divisão das quatro issues

| Issue | Responsável planejado | Arquivo principal | Objetivo |
|---|---|---|---|
| #1 — Comunicação UDP | Pedro | `RedeUDP.java` | Criar o envio e recebimento seguro de tokens via `DatagramPacket`. |
| #2 — Configuração e regras | Ian | `Configuracao.java` | Validar parâmetros, calcular portas/IP e aplicar operações matemáticas. |
| #3 — Processo Mestre | Participante que já assumiu esta issue | `Mestre.java` | Iniciar o token, controlar voltas e encerrar a execução. |
| #4 — Escravo e testes | Artu | `Escravo.java` e scripts | Processar os nós trabalhadores e automatizar os cenários locais. |

Uma issue só é considerada concluída depois da integração com os arquivos dos demais participantes e da execução dos testes previstos.

### Issue #1 — implementar `RedeUDP.java`

Responsável pelo módulo: **artu**.

**O que implementar:**

1. Converter o token inteiro em `String` e depois em `byte[]` com `getBytes()`.
2. Criar o `DatagramPacket` com o conteúdo, o IP do destino e a porta de destino.
3. Enviar o pacote usando o método `socket.send(packet)`.
4. Usar um vetor de bytes de tamanho `1000` em `receberToken`.
5. Criar um `DatagramPacket` vazio, chamar `socket.receive(packet)` e converter somente os dados válidos em texto.
6. Usar `trim()` antes de `Integer.parseInt` para evitar erros por espaços ou bytes vazios.
7. Não abrir nem fechar o `DatagramSocket`; manter essa responsabilidade no processo chamador.

**Critérios de aceite:**

- [ ] As duas assinaturas públicas estão exatamente conforme o contrato.
- [ ] O envio funciona com IP local e IP remoto.
- [ ] O recebimento converte corretamente um inteiro enviado pelo mesmo método.
- [ ] Nenhum método fecha o socket recebido.
- [ ] Erros de rede são propagados por `IOException`.

### Issue #2 — implementar `Configuracao.java`

Responsável pelo módulo: **Ian**.

**O que implementar:**

1. Validar presença de pelo menos três argumentos.
2. Validar se o ID é compatível com o tipo de processo informado por `validarArgs`.
3. Validar se a operação é `0` ou `1`.
4. Validar se o número de voltas é maior ou igual a `1`.
5. Converter números com tratamento adequado para evitar encerramento confuso em formato inválido.
6. Implementar o cálculo das portas locais e do próximo nó.
7. Resolver o IP opcional ou usar `localhost` como padrão.
8. Aplicar a operação matemática ao token.

**Critérios de aceite:**

- [ ] `getMinhaPorta(0)` retorna `5000` e os IDs `1` a `3` retornam `5001` a `5003`.
- [ ] `getPortaProximoNo(3)` retorna `5000`.
- [ ] `getPortaProximoNo(1)` retorna `5002` e `getPortaProximoNo(2)` retorna `5003`.
- [ ] O quarto argumento é opcional e um argumento ausente resulta em `localhost`.
- [ ] Operação `0` soma `1`; operação `1` multiplica por `2`.
- [ ] IDs, operação e voltas inválidos produzem uma mensagem clara e encerram o processo.

### Issue #3 — implementar `Mestre.java`

Responsável pelo módulo: **pedro**.

**O que implementar:**

1. Validar os argumentos e confirmar que `args[0]` é `0`.
2. Abrir um `DatagramSocket` na porta `5000`.
3. Inicializar o token em `2` e enviá-lo ao Escravo 1 na primeira volta.
4. Implementar o ciclo controlado pela quantidade `N` de voltas.
5. Ao receber o token de volta do Escravo 3, aplicar a operação do Mestre com `Configuracao.processarToken`.
6. Reenviar o token apenas enquanto houver voltas restantes.
7. Ao concluir a última volta, imprimir o resultado final.
8. Fechar o socket no bloco `finally`, mesmo quando ocorrer uma exceção.

**Critérios de aceite:**

- [ ] Executa com `java Mestre 0 0 1` e `java Mestre 0 1 1`.
- [ ] Espera o Escravo 3 antes de processar o retorno da volta.
- [ ] Aplica corretamente sua operação ao retorno de cada volta.
- [ ] Para `N = 1`, não reaplica nenhuma operação extra além da esperada pelo ciclo.
- [ ] Fecha o socket ao término ou em caso de erro.
- [ ] Exibe uma saída final identificável, como `Resultado final do token: 6`.

**Ponto de integração:** a ordem de início deve ser Escravos 1, 2 e 3 primeiro, e Mestre por último.

### Issue #4 — implementar `Escravo.java` e os scripts

Responsável pelo módulo: **rj**.

**O que implementar:**

1. Validar que o ID está entre `1` e `3` e corresponder à porta local correta.
2. Abrir o socket na porta `5001`, `5002` ou `5003` conforme o ID.
3. Receber tokens em um ciclo pelo método `RedeUDP.receberToken`.
4. Aplicar a operação local por meio de `Configuracao.processarToken`.
5. Imprimir ao menos o ID do nó, o valor recebido e o valor enviado.
6. Usar `Configuracao.getPortaProximoNo` e `Configuracao.getIpProximoNo` para repassar o token.
7. Encerrar o ciclo após o Mestre executar a quantidade `N` de voltas, ou conforme a duração acordada para o processo.
8. Criar `testar_local.sh` para Linux/macOS e `testar_local.bat` para Windows.

**Critérios de aceite:**

- [ ] Os Escravos executam com os IDs `1`, `2` e `3`.
- [ ] Cada Escravo aplica exatamente uma operação por passagem.
- [ ] A operação da instância corresponde ao argumento informado na linha de comando.
- [ ] Os logs permitem identificar cada valor recebido e enviado.
- [ ] Os scripts validam os resultados `6`, `32` e `10`.
- [ ] Os processos aguardam o início dos próximos nós e não encerram o socket de forma prematura.

## 8. Como compilar

Em ambiente Linux ou Windows com Java instalado:

```bash
cd src
javac *.java
```

Se houver arquivos `.class` depois da compilação, o `.gitignore` já está configurado para não enviá-los ao repositório.

## 9. Execução local

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

## 10. Execução distribuída em computadores diferentes

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


## 11. Fluxo de trabalho no GitHub

Cada integrante deve usar uma branch curta e específica:

```text
Pedro: git checkout -b feat/rede-udp
Ian:   git checkout -b feat/configuracao
RJ:    git checkout -b feat/mestre
Artu:  git checkout -b feat/escravo-testes
```

Ao concluir o trabalho:

```bash
git add src/<arquivo-modificado>
git commit -m "feat: implementar ..."
git push origin <nome-da-sua-branch>
```

Depois, abra um Pull Request para `main`, descreva a implementação, liste os cenários testados e inclua `Closes #X` no corpo da descrição. Só faça merge após a revisão e os testes integrarem a alteração à equipe.


