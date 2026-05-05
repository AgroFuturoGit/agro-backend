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

## 🌿 Branches

| Branch | Descrição |
|--------|-----------|
| `main` | Código em produção, estável e revisado |
| `staging` | Ambiente de homologação, pré-produção |
| `develop` | Branch principal de desenvolvimento |

> Novas funcionalidades devem ser criadas a partir de `develop` seguindo o padrão `feature/nome-da-funcionalidade`
