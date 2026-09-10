# 🌱 Agro Backend - MVP

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0+-brightgreen.svg)](https://spring.io/projects/spring-boot)

Backend do sistema de gestão da informação para o setor agrícola e acompanhamento de safras.

## 🏗️ Arquitetura

O projeto adota a arquitetura de **Monolito Modular (Modular Monolith)** combinado com **Arquitetura Hexagonal (Ports and Adapters)** em cada módulo.

### Módulos Principais (Bounded Contexts)

- **IAM (Identidade e Acesso):** Autenticação, RBAC (Controle de Acesso Baseado em Papéis) e trilhas de auditoria (LGPD).
- **Social & Cadastral:** Gestão de produtores, famílias, comunidades e cooperativas/associações.
- **Financeiro:** Registro de financiamentos e adimplência.
- **Core Agrícola:** Safras, áreas plantadas, culturas, ocorrências climáticas, pragas e doenças.
- **Analytics:** Prognósticos, relatórios e evolução histórica dos cultivos.

## 💻 Stack Tecnológica

- **Linguagem:** Java 21
- **Framework:** Spring Boot (Web, Data JPA, Validation)
- **Gerenciador de Build:** Maven
- **Bancos de Dados:** PostgreSQL
- **Migrations:** Flyway
- **Ambiente local:** Docker Compose para o PostgreSQL

## 🚀 Rodando em Desenvolvimento

A estratégia de desenvolvimento é: **só o banco roda em Docker**; backend e frontend rodam direto na máquina para ter hot reload e debug rápido.

> Para subir tudo em containers (cenário de produção), veja [DEPLOYMENT.md](DEPLOYMENT.md).

### Pré-requisitos

- **Java JDK 21**
- **Git**
- **Docker** e **Docker Compose**

Não é necessário instalar Maven separadamente — o projeto já tem o **Maven Wrapper** (`mvnw`).

Em sistemas baseados em Ubuntu:

```bash
sudo apt update
sudo apt install openjdk-21-jdk git
```

Confirme a instalação:

```bash
java -version    # deve ser 21.x
```

### 1. Clonar e preparar

```bash
git clone <url-do-repositorio>
cd agro-backend
cp .env.example .env
chmod +x mvnw   # se necessário
```

Edite `.env` se quiser sobrescrever os valores padrão (usuário, senha, porta).

### 2. Subir o banco de dados

```bash
docker compose up -d
```

O PostgreSQL sobe na porta `${DB_PORT}` (padrão `5433` para não conflitar com um Postgres local em 5432). Healthcheck e volume persistente já estão configurados.

Acompanhar os logs:

```bash
docker compose logs -f db
```

Parar (mantendo o volume):

```bash
docker compose down
```

Parar e **apagar os dados**:

```bash
docker compose down -v
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A API fica disponível em `http://localhost:8080`. O Flyway aplica as migrations de `src/main/resources/db/migration/` automaticamente no startup.

> Nome de migration segue o padrão Flyway: `V1__create_users_table.sql`.

### 4. Rodar o frontend (em outro terminal)

O frontend mora no repo irmão [agro-frontend](../agro-frontend). Clone-o lado a lado e rode:

```bash
cd ../agro-frontend
cp .env.example .env.local
npm install
npm run dev
```

Frontend em `http://localhost:3000`, falando com a API em `http://localhost:8080`.

### Outros comandos úteis

```bash
./mvnw test          # rodar testes
./mvnw package       # gerar .jar em target/
java -jar target/*.jar  # rodar o jar empacotado
```

## 📖 Documentação da API (Swagger / OpenAPI)

Com a aplicação em execução, a documentação interativa e os contratos da API podem ser acessados em:

- **Swagger UI (Interface Interativa):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Docs (Especificação JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Autenticação no Swagger UI:
1. Execute a requisição no endpoint `POST /auth/login` informando e-mail e senha para obter o token JWT.
2. Clique no botão **Authorize** (ícone de cadeado no topo direito da interface do Swagger).
3. No campo **Value**, insira o token JWT retornado.
4. Clique em **Authorize** e feche a janela. Todos os endpoints protegidos utilizarão o token automaticamente.

---

## 🌿 Branches

| Branch | Descrição |
|--------|-----------|
| `main` | Código em produção, estável e revisado |
| `staging` | Ambiente de homologação, pré-produção |
| `develop` | Branch principal de desenvolvimento |

> Novas funcionalidades devem ser criadas a partir de `develop` seguindo o padrão `feature/nome-da-funcionalidade`

## Padrão de Commits
Adotar o padrão Conventional Commits.

### Estrutura:
- tipo: descrição curta

### Tipos mais usados:
- feat: nova funcionalidade
- fix: correção de bug
- refactor: refatoração de código
- docs: documentação
- test: testes
- chore: tarefas gerais/configuração

## 🚢 Deploy

Para subir o banco e o backend em containers (produção), ver [DEPLOYMENT.md](DEPLOYMENT.md).

O frontend é deployado de forma independente no repo [agro-frontend](../agro-frontend).
