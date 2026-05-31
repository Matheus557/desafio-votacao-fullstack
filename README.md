# Voting

Aplicação de votação para pautas de assembleia. O backend gerencia pautas, sessões de votação e votos por associado; o frontend oferece uma tela simples para cadastrar pautas, abrir votação e registrar votos.

## Tecnologias

- Backend: Java 17, Spring Boot 3.5, Spring Web, Spring Data JPA, Validation, PostgreSQL, Lombok e Swagger.
- Frontend: React 19, TypeScript, Vite e CSS.
- Infra/testes: Docker Compose, Maven Wrapper, JUnit 5, Mockito e MockMvc.

## Como Funciona

- A pauta nasce `FECHADA`.
- Ao abrir a pauta, o usuário define ou altera o tempo da votação.
- Enquanto o tempo está ativo, a pauta fica `ABERTA`.
- Quando o tempo acaba, a pauta vira `ENCERRADA` e não pode ser aberta de novo.
- Cada voto usa `associateId`, `cpf` e `vote`.
- O `associateId` identifica o associado e só pode votar uma vez por pauta.
- O CPF é usado apenas no `CpfValidationClient`, um client fake que simula validação externa.
- A API recebe votos como `YES` ou `NO`.

## Rodando o Projeto

Suba o banco:

```bash
docker compose up -d
```

Rode o backend:

```bash
cd backend
./mvnw spring-boot:run
```

Rode o frontend:

```bash
cd frontend
npm install
npm run dev
```

URLs:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

## Testes

```bash
cd backend
./mvnw test
```

```bash
cd frontend
npm run build
```

## Endpoints Principais

Base da API: `http://localhost:8080/api`

### Pautas

- `POST /pautas/cadastro`: cadastra uma pauta.
- `GET /pautas/informacoes`: lista pautas com status, contagem de votos e datas.
- `PATCH /pautas/{id}/abrir`: abre uma pauta e inicia o tempo de votação.
- `GET /pautas/{id}/aberta`: informa se a pauta ainda aceita votos.

Cadastro de pauta:

```json
{
  "nome": "Pauta de orçamento",
  "descricao": "Definição do orçamento anual",
  "tempoAbertoPorMinutos": 10
}
```

Abertura de pauta:

```json
{
  "tempoAbertoPorMinutos": 5
}
```

### Votos

Endpoint principal:

```text
POST /pautas/{agendaId}/votos
```

Request:

```json
{
  "associateId": 1,
  "cpf": "12345678900",
  "vote": "YES"
}
```

Regras do voto:

- busca a pauta pelo `agendaId`;
- verifica se a sessão está aberta;
- bloqueia duplicidade por `agendaId + associateId`;
- valida o CPF no `CpfValidationClient`;
- salva apenas se o associado estiver apto a votar.

Possíveis erros:

- `404`: CPF inválido no client fake.
- `403`: associado não habilitado para votar.
- `400`: pauta fechada, voto inválido ou associado já votou.

## Telas

### Home

Lista as pautas, mostra status (`Fechada`, `Aberta`, `Encerrada`), totais de votos e ações para cadastrar, abrir ou votar.

### Cadastro de Pauta

Modal com título, descrição e tempo padrão da pauta.

### Abertura de Pauta

Modal para abrir uma pauta fechada e ajustar o tempo da votação antes de iniciar.

### Votação

Tela com título, descrição, contador, `associateId`, CPF e seleção `Sim/Não`. Mantém a votação aberta para outros associados até o tempo acabar.

## Banco

O projeto usa PostgreSQL via Docker Compose:

```text
Banco: voting_db
Usuário: postgres
Senha: postgres
Porta: 5432
```
