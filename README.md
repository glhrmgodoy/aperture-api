# Aperture API

![CI](https://github.com/glhrmgodoy/aperture-api/actions/workflows/ci.yml/badge.svg)

API REST de uma rede social de filmes, inspirada no Letterboxd. Usuários avaliam filmes, escrevem reviews, montam listas personalizadas (inclusive ranqueadas), mantêm uma watchlist, seguem outros usuários, comentam e curtem reviews.

Projeto de portfólio focado em **modelagem de domínio relacional**, **integridade de dados garantida no banco** e **testes automatizados rodando em CI**.

## Stack

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA + Hibernate
- PostgreSQL 16
- Flyway (migrations versionadas)
- MapStruct (mapeamento DTO ↔ Entity)
- Bean Validation
- Springdoc OpenAPI (Swagger UI)
- Spring Boot Actuator
- JUnit 5 + Mockito
- Docker Compose
- GitHub Actions (CI)

## Destaques técnicos

- **9 entidades relacionadas**: usuários, filmes, reviews, comentários, curtidas, listas, itens de lista, watchlist e seguidores (relacionamento N:N de usuário com ele mesmo).
- **Integridade garantida em duas camadas**: as regras são validadas no service e reforçadas por constraints no PostgreSQL (`UNIQUE`, `CHECK`). Assim, nem uma requisição concorrente consegue gravar dados inválidos.
- **Índices criados via migration** (`V2__create_indexes.sql`) nas colunas mais consultadas (filme da review, dono da lista, gênero etc.).
- **Busca de filmes** por título, gênero, ano de lançamento ou diretor.
- **Paginação** na listagem de listas personalizadas (`Pageable`).
- **Auditoria automática** de `createdAt` / `updatedAt` com Spring Data JPA Auditing.
- **Tratamento global de exceções** com `ProblemDetail` (RFC 9457): `404` para recurso inexistente, `422` para regra de negócio e `409` para violação de constraint.
- **114 testes automatizados** (JUnit 5 + Mockito) cobrindo todos os services.
- **Pipeline de CI** que, a cada push e pull request na `main`, sobe um PostgreSQL, aplica as migrations e roda o build completo com testes.

## Regras de negócio

| Regra | Onde é garantida |
|---|---|
| Username e e-mail únicos | Service + `UNIQUE` |
| Um usuário só pode avaliar cada filme uma vez | Service + `UNIQUE (user_id, movie_id)` |
| Nota da review entre 0.5 e 5.0 | Bean Validation + `CHECK` |
| Usuário não pode seguir a si mesmo nem seguir alguém duas vezes | Service + `CHECK` + `UNIQUE` |
| Um filme não pode se repetir na mesma lista nem na watchlist | Service + `UNIQUE` |
| Em lista ranqueada, a posição do filme é obrigatória | Service |
| Só o dono pode editar ou excluir uma lista | Service |
| Um comentário pode ser removido pelo autor do comentário ou pelo autor da review | Service |
| Curtida funciona como *toggle*: curtir de novo remove a curtida | Service + `UNIQUE (user_id, review_id)` |

## Como rodar

### Pré-requisitos

- Java 21+
- Docker (para subir o PostgreSQL) ou uma instância própria do Postgres rodando

### 1. Variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (ele **não** deve ser commitado):

```env
POSTGRES_DB=aperture_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha_local
```

A aplicação lê as mesmas variáveis, então exporte-as no terminal ou configure-as na sua IDE antes de rodar.

### 2. Subir o banco de dados

```bash
docker compose up -d
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

As migrations do Flyway rodam automaticamente na inicialização. O Hibernate está em modo `validate`, ou seja, o schema é controlado só pelas migrations.

A API sobe em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

### 4. Rodar os testes

```bash
./mvnw test
```

## Endpoints principais

### Usuários

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/users` | Cadastra usuário |
| GET | `/api/v1/users` | Lista usuários |
| GET | `/api/v1/users/{id}` | Busca usuário |
| PUT | `/api/v1/users/{id}` | Atualiza usuário |
| DELETE | `/api/v1/users/{id}` | Remove usuário |

### Filmes

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/movies` | Cadastra filme |
| GET | `/api/v1/movies?title=` | Busca por título |
| GET | `/api/v1/movies?genre=` | Busca por gênero |
| GET | `/api/v1/movies?releaseYear=` | Busca por ano |
| GET | `/api/v1/movies?director=` | Busca por diretor |
| GET | `/api/v1/movies/{id}` | Detalhes do filme |
| PUT | `/api/v1/movies/{id}` | Atualiza filme |
| DELETE | `/api/v1/movies/{id}` | Remove filme |

### Reviews, comentários e curtidas

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/reviews` | Cria review |
| GET | `/api/v1/reviews/{id}` | Busca review |
| GET | `/api/v1/reviews/movie/{movieId}` | Reviews de um filme |
| GET | `/api/v1/reviews/user/{userId}` | Reviews de um usuário |
| PUT | `/api/v1/reviews/{id}` | Atualiza review |
| DELETE | `/api/v1/reviews/{id}` | Remove review |
| POST | `/api/v1/reviews/{reviewId}/comments?authenticatedUserId=` | Comenta uma review |
| GET | `/api/v1/reviews/{reviewId}/comments` | Lista comentários |
| DELETE | `/api/v1/reviews/{reviewId}/comments/{commentId}?authenticatedUserId=` | Remove comentário |
| POST | `/api/v1/reviews/{reviewId}/like?userId=` | Curte / descurte (toggle) |
| GET | `/api/v1/reviews/{reviewId}/like` | Total de curtidas |

### Listas personalizadas

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/lists` | Cria lista (pública/privada, ranqueada ou não) |
| GET | `/api/v1/lists` | Lista todas |
| GET | `/api/v1/lists/{id}` | Busca lista |
| GET | `/api/v1/lists/user/{userId}` | Listas de um usuário (paginado) |
| PUT | `/api/v1/lists/{id}?authenticatedUserId=` | Atualiza lista (só o dono) |
| DELETE | `/api/v1/lists/{id}?authenticatedUserId=` | Remove lista (só o dono) |
| POST | `/api/v1/lists/{listId}/movies?authenticatedUserId=` | Adiciona filme à lista |
| GET | `/api/v1/lists/{listId}/movies` | Filmes da lista |
| DELETE | `/api/v1/lists/{listId}/movies/{movieId}?authenticatedUserId=` | Remove filme da lista |

### Watchlist e seguidores

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/watchlist/{userId}/movies/{movieId}` | Adiciona à watchlist |
| GET | `/api/v1/watchlist/{userId}` | Watchlist do usuário |
| DELETE | `/api/v1/watchlist/{userId}/movies/{movieId}` | Remove da watchlist |
| POST | `/api/v1/follows/{followerId}/follow/{followingId}` | Seguir usuário |
| DELETE | `/api/v1/follows/{followerId}/unfollow/{followingId}` | Deixar de seguir |
| GET | `/api/v1/follows/{userId}/followers` | Seguidores |
| GET | `/api/v1/follows/{userId}/following` | Quem o usuário segue |

## Estrutura do projeto

```
src/main/java/com/godoy/aperture/
├── controller/      # Endpoints REST
├── service/         # Regras de negócio
├── repository/      # Interfaces Spring Data JPA
├── domain/
│   ├── entity/      # User, Movie, Review, Comment, Like, CustomList, ListItem, WatchList, Follow
│   └── enums/       # Genre, ListVisibility
├── dto/
│   ├── request/
│   └── response/
├── mapper/          # Interfaces MapStruct
└── exception/       # Exceções customizadas + GlobalExceptionHandler

src/main/resources/db/migration/
├── V1__create_tables.sql
└── V2__create_indexes.sql
```

## Status do projeto

- [x] Modelagem de domínio + migrations Flyway
- [x] Constraints e índices no banco
- [x] Regras de negócio
- [x] Tratamento global de exceções (ProblemDetail)
- [x] Controllers REST + Swagger
- [x] Testes automatizados (114)
- [x] CI com GitHub Actions
