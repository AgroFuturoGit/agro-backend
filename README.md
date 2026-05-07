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

## 🚀 Rodando Localmente

### Pré-requisitos

Para executar o projeto localmente, instale:

- **Java JDK 21**
- **Git**
- **Docker** e **Docker Compose**

Não é necessário instalar Maven separadamente, pois o projeto já possui o **Maven Wrapper** (`mvnw`).

Em sistemas baseados em Ubuntu, como Zorin OS:

```bash
sudo apt update
sudo apt install openjdk-21-jdk git
```

Confirme a instalação:

```bash
java -version
javac -version
```

As versões exibidas devem ser `21.x`.

### Preparando o projeto

Clone o repositório e entre na pasta do projeto:

```bash
git clone <url-do-repositorio>
cd agro-backend
```

Caso o Maven Wrapper ainda não tenha permissão de execução, execute:

```bash
chmod +x mvnw
```

Confirme que o Maven Wrapper está funcionando:

```bash
./mvnw -v
```

Na primeira execução, o Maven pode baixar as dependências do projeto.

### Subindo o banco de dados

O PostgreSQL roda via Docker Compose com os valores padrão:

| Campo | Valor |
|-------|-------|
| Banco | `agro_backend` |
| Usuário | `agro` |
| Senha | `agro` |
| Porta | `5432` |

Para iniciar o banco:

```bash
docker compose up -d db
```

Para acompanhar os logs:

```bash
docker compose logs -f db
```

Para parar o banco:

```bash
docker compose down
```

### Executando a aplicação

Para rodar a aplicação localmente:

```bash
./mvnw spring-boot:run
```

A aplicação permanece em execução com o servidor Web embutido e escuta, por padrão, em:

```text
http://localhost:8080
```

O Flyway está ativado e procura migrations em:

```text
src/main/resources/db/migration/
```

As migrations devem seguir o padrão de nome do Flyway, por exemplo:

```text
V1__init_schema.sql
```

### Rodando os testes

```bash
./mvnw test
```

### Gerando o arquivo `.jar`

```bash
./mvnw package
```

O arquivo gerado ficará na pasta `target/`. Para executá-lo:

```bash
java -jar target/*.jar
```

## 🌿 Branches

| Branch | Descrição |
|--------|-----------|
| `main` | Código em produção, estável e revisado |
| `staging` | Ambiente de homologação, pré-produção |
| `develop` | Branch principal de desenvolvimento |

> Novas funcionalidades devem ser criadas a partir de `develop` seguindo o padrão `feature/nome-da-funcionalidade`
