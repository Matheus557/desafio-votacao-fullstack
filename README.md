# Voting

Sistema de votação para pautas de assembleia. O backend controla cadastro, abertura e encerramento das pautas, além do recebimento de votos por CPF. O frontend permite cadastrar pautas, abrir uma votação com tempo definido e registrar votos enquanto a pauta estiver aberta.

## Tecnologias

Backend:
- Java 17
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Lombok
- Springdoc OpenAPI/Swagger
- JUnit 5, Mockito e MockMvc

Frontend:
- React 19
- TypeScript
- Vite
- CSS puro

Infra:
- Docker Compose para PostgreSQL
- Maven Wrapper no backend
- npm no frontend

## Regras Principais

- Uma pauta nasce com status `FECHADA`.
- Ao abrir a pauta, o tempo de votação pode ser ajustado.
- Depois de aberta, a pauta fica com status `ABERTA` e o tempo começa a contar.
- Quando o tempo termina, a pauta passa a ser considerada `ENCERRADA`.
- Pauta encerrada não pode ser aberta novamente.
- Votos aceitos: `Sim` ou `Não`.
- Cada CPF pode votar apenas uma vez por pauta.
- Votos em pauta fechada ou encerrada são bloqueados pelo backend.

## Como Rodar

Pré-requisitos:
- Java 17
- Docker e Docker Compose
- Node.js/npm

Suba o banco:

```bash
docker compose up -d
```

Rode o backend:

```bash
cd backend
./mvnw spring-boot:run
```

Backend disponível em:

```text
http://localhost:8080
```

Swagger/OpenAPI:

```text
http://localhost:8080/swagger-ui.html
```

Rode o frontend:

```bash
cd frontend
npm install
npm run dev
```

Frontend disponível em:

```text
http://localhost:5173
```

O Vite usa proxy para chamadas `/api`, apontando para o backend em `http://localhost:8080`.

## Como Testar

Backend:

```bash
cd backend
./mvnw test
```

Frontend:

```bash
cd frontend
npm run build
```

## Endpoints do Backend

Base URL:

```text
http://localhost:8080/api
```

### Pautas

#### `POST /pautas`

Cadastra uma nova pauta. A pauta é criada com status `FECHADA`.

Payload:

```json
{
  "nome": "Pauta de orçamento",
  "descricao": "Definição do orçamento anual",
  "tempoAbertoPorMinutos": 10
}
```

Resposta `201`:

```json
{
  "id": 1,
  "nome": "Pauta de orçamento",
  "descricao": "Definição do orçamento anual",
  "tempoAbertoPorMinutos": 10
}
```

#### `POST /pautas/cadastro`

Alias do cadastro de pauta. Tem o mesmo comportamento de `POST /pautas`.

#### `GET /pautas`

Lista as pautas em formato simples.

Resposta:

```json
[
  {
    "id": 1,
    "nome": "Pauta de orçamento",
    "descricao": "Definição do orçamento anual",
    "tempoAbertoPorMinutos": 10
  }
]
```

#### `GET /pautas/{id}`

Busca uma pauta pelo ID em formato simples.

#### `GET /pautas/informacoes`

Lista pautas com informações completas para a tela inicial: status, período de votação e contagem de votos.

Resposta:

```json
[
  {
    "id": 1,
    "nome": "Pauta de orçamento",
    "descricao": "Definição do orçamento anual",
    "tempoAbertoPorMinutos": 10,
    "dataCriacao": "2026-05-31T17:00:00",
    "dataEncerramento": "2026-05-31T17:10:00",
    "status": "ABERTA",
    "aberta": true,
    "totalVotos": 3,
    "votosSim": 2,
    "votosNao": 1
  }
]
```

Status possíveis:
- `FECHADA`: pauta criada, ainda não aberta.
- `ABERTA`: votação em andamento.
- `ENCERRADA`: votação finalizada; não pode abrir novamente.

#### `PUT /pautas/{id}`

Atualiza nome, descrição e tempo padrão de uma pauta.

Payload:

```json
{
  "nome": "Pauta atualizada",
  "descricao": "Nova descrição",
  "tempoAbertoPorMinutos": 15
}
```

#### `PATCH /pautas/{id}/abrir`

Abre uma pauta para votação. O tempo informado aqui pode alterar o tempo cadastrado inicialmente.

Payload:

```json
{
  "tempoAbertoPorMinutos": 5
}
```

Resposta `200`:

```json
{
  "id": 1,
  "nome": "Pauta de orçamento",
  "descricao": "Definição do orçamento anual",
  "tempoAbertoPorMinutos": 5,
  "dataCriacao": "2026-05-31T17:00:00",
  "dataEncerramento": "2026-05-31T17:05:00",
  "status": "ABERTA",
  "aberta": true,
  "totalVotos": 0,
  "votosSim": 0,
  "votosNao": 0
}
```

Erros comuns:
- `A pauta já está aberta para votação`
- `Tempo da votação encerrado`

#### `GET /pautas/{id}/aberta`

Retorna `true` ou `false` indicando se a pauta está aberta para receber votos.

#### `DELETE /pautas/{id}`

Remove uma pauta pelo ID.

### Votos

#### `POST /votos`

Registra um voto. Cada CPF pode votar apenas uma vez por pauta.

Payload:

```json
{
  "cpf": "12345678900",
  "pautaId": 1,
  "voto": "Sim"
}
```

Resposta `201`:

```json
{
  "id": 1,
  "cpf": "12345678900",
  "pautaId": 1,
  "voto": "SIM"
}
```

Erros comuns:
- `CPF já votou nesta pauta`
- `A pauta não está mais aberta para votação`
- `Voto deve ser 'Sim' ou 'Não'`

#### `POST /votos/receber`

Alias do registro de voto. Tem o mesmo comportamento de `POST /votos`.

#### `GET /votos`

Lista todos os votos.

#### `GET /votos/{id}`

Busca um voto pelo ID.

#### `GET /votos/pauta/{pautaId}`

Lista os votos de uma pauta específica.

#### `DELETE /votos/{id}`

Remove um voto pelo ID.

## Telas do Frontend

### Home

Tela principal do sistema.

Funcionalidades:
- Lista todas as pautas.
- Exibe status `Fechada`, `Aberta` ou `Encerrada`.
- Mostra total de votos, votos `Sim` e votos `Não`.
- Permite cadastrar uma nova pauta.
- Permite abrir uma pauta fechada.
- Impede reabertura de pauta encerrada e mostra a mensagem `Tempo da votação encerrado.`
- Permite entrar na tela de votação apenas quando a pauta está aberta.

### Modal de Cadastro de Pauta

Aberto pelo botão `Cadastrar nova pauta`.

Campos:
- Título
- Descrição
- Tempo padrão em minutos

Ao salvar, chama `POST /api/pautas/cadastro`.

### Modal de Abertura de Pauta

Aberto pelo botão `Abrir votacao` em uma pauta fechada.

Campos:
- Tempo aberta em minutos

Esse tempo pode alterar o tempo definido no cadastro. Ao confirmar, chama `PATCH /api/pautas/{id}/abrir`.

### Tela de Votação

Tela aberta quando uma pauta está com status `ABERTA`.

Funcionalidades:
- Mostra título e descrição da pauta.
- Mostra contador de tempo restante.
- Permite informar CPF.
- Permite selecionar apenas um voto: `Sim` ou `Não`.
- Mostra mensagem de sucesso quando o voto é registrado.
- Mantém a tela aberta para outros CPFs votarem.
- Mostra modal quando o CPF já votou na pauta.
- Bloqueia o formulário quando o tempo termina.
- Mostra mensagem de tempo encerrado.
- Botão `Fechar pauta` volta para a Home.

## Banco de Dados

O projeto usa PostgreSQL via Docker Compose.

Configuração padrão:

```text
Banco: voting_db
Usuário: postgres
Senha: postgres
Porta: 5432
```

O backend usa `ddl-auto: update` para atualizar o schema durante o desenvolvimento. Também existe uma migração de compatibilidade para remover estruturas antigas de usuário, já que o sistema atual trabalha apenas com `pauta` e `voto`.
