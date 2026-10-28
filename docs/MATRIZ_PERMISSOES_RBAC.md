# 🔐 Matriz Oficial de Permissões RBAC (Role-Based Access Control)

Este documento especifica a política de controle de acesso aos recursos do backend do **SmartAgro**. Cada requisição é autorizada com base na role contida nas credenciais do JWT e validada pela camada de segurança (`@PreAuthorize`) e validadores de escopo de domínio (`ProductionAccessValidator`).

---

## 👥 Papéis do Sistema (Roles)

| Role | Descrição | Escopo de Acesso Padrão |
| :--- | :--- | :--- |
| **`ADMIN`** | Administrador Geral do Sistema | Global (irrestrito em todas as entidades e organizações) |
| **`MANAGER`** | Gestor de Cooperativa / Associação | Restrito à sua respectiva Organização e suas Comunidades |
| **`TECHNICIAN`** | Técnico Agrícola Extensionista | Restrito às Comunidades onde possui vínculo de assistência técnica ativo |
| **`FARMER`** | Agricultor / Produtor Familiar | Restrito exclusivamente aos seus próprios dados cadastrais e produtivos |

---

## 📋 Tabela Geral de Módulos e Endpoints

### 1. Autenticação e Gestão de Usuários (`/auth`, `/users`)

| Módulo | Método | Endpoint | Papéis Permitidos | Regra de Escopo / Dono |
| :--- | :--- | :--- | :--- | :--- |
| Auth | `POST` | `/auth/login` | Público | Não requer autenticação. Retorna JWT Bearer. |
| Auth | `GET` | `/auth/me` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Retorna os dados do próprio usuário autenticado. |
| IAM | `POST` | `/users/register` | `ADMIN` | Apenas administrador cria novas contas avulsas no sistema. |
| IAM | `GET` | `/users` | `ADMIN` | Listagem geral de contas de usuário. |
| IAM | `GET` | `/users/{id}` | `ADMIN` | Consulta detalhada de usuário por UUID. |
| IAM | `PUT` | `/users/{id}` | `ADMIN` | Atualização cadastral/papel (proibido auto-rebaixamento). |
| IAM | `DELETE`| `/users/{id}` | `ADMIN` | Soft delete de conta (proibida a autoexclusão da própria conta). |
| IAM | `GET` | `/roles` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Consulta do catálogo de papéis disponíveis no sistema. |

---

### 2. Cadastros Sociais e Organizacionais (`/organizations`, `/communities`, `/farmers`, `/technicians`, `/managers`)

| Módulo | Método | Endpoint | Papéis Permitidos | Regra de Escopo / Dono |
| :--- | :--- | :--- | :--- | :--- |
| Org | `GET` | `/organizations` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Leitura do catálogo de organizações. |
| Org | `POST` | `/organizations` | `ADMIN` | Criação restrita a administradores. |
| Org | `PUT` | `/organizations/{id}` | `ADMIN` | Edição restrita a administradores. |
| Org | `DELETE`| `/organizations/{id}` | `ADMIN` | Soft delete restrito a administradores. |
| Comunidade | `GET` | `/communities` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Consulta de comunidades. Gestor/técnico podem filtrar por org/assistência. |
| Comunidade | `POST` | `/communities` | `ADMIN, MANAGER` | Gestor só pode criar em sua própria organização. |
| Comunidade | `PUT` | `/communities/{id}` | `ADMIN, MANAGER` | Gestor só pode editar comunidades de sua própria organização. |
| Comunidade | `DELETE`| `/communities/{id}` | `ADMIN, MANAGER` | Soft delete de comunidade. Gestor restrito à sua organização. |
| Produtor | `GET` | `/farmers` | `ADMIN, MANAGER, TECHNICIAN` | Gestor: apenas de sua org; Técnico: apenas de suas comunidades. |
| Produtor | `GET` | `/farmers/{id}` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Produtor acessa apenas o seu; Técnico em suas comunidades; Gestor na org. |
| Produtor | `POST` | `/farmers` | `ADMIN, MANAGER, TECHNICIAN` | Cadastro de agricultor (comunidade obrigatória para escopo). |
| Produtor | `PUT` | `/farmers/{id}` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Edição com controle de versão otimista e escopo de atuação. |
| Produtor | `DELETE`| `/farmers/{id}` | `ADMIN, MANAGER` | Soft delete em cascata (desativa conta de usuário associada). |
| Técnico | `GET` | `/technicians` | `ADMIN, MANAGER, TECHNICIAN` | Listagem da equipe técnica. |
| Técnico | `POST` | `/technicians` | `ADMIN, MANAGER` | Cadastro de técnicos com CREA/CFT/CFTA. |
| Técnico | `PUT` | `/technicians/{id}` | `ADMIN, MANAGER` | Atualização cadastral da equipe técnica. |
| Técnico | `DELETE`| `/technicians/{id}` | `ADMIN` | Soft delete de perfil técnico. |
| Assistência| `POST` | `/technical-assistances` | `ADMIN, MANAGER` | Vinculação do técnico às comunidades atendidas. |
| Gestor | `GET` | `/managers` | `ADMIN` | Listagem de gestores das organizações. |
| Gestor | `POST` | `/managers` | `ADMIN` | Associação de novos gestores às cooperativas/associações. |

---

### 3. Catálogos Agrícolas Básicos (`/crops`, `/harvests`)

| Módulo | Método | Endpoint | Papéis Permitidos | Regra de Escopo / Dono |
| :--- | :--- | :--- | :--- | :--- |
| Culturas | `GET` | `/crops` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Consulta do catálogo de culturas agrícolas cadastradas. |
| Culturas | `GET` | `/crops/{id}` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Detalhes da cultura, tipo de colheita e produtividade esperada. |
| Culturas | `POST` | `/crops` | `ADMIN, TECHNICIAN` | Cadastro de nova cultura (unicidade por nome + variedade ativa). |
| Culturas | `PUT` | `/crops/{id}` | `ADMIN, TECHNICIAN` | Atualização com controle de concorrência (`baseUpdatedAt`). |
| Culturas | `DELETE`| `/crops/{id}` | `ADMIN, TECHNICIAN` | Bloqueado com `409 Conflict` se houver planos vinculados ativos. |
| Safras | `GET` | `/harvests` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Consulta do calendário de safras vigentes e históricas. |
| Safras | `GET` | `/harvests/{id}` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Detalhes do período e label da safra. |
| Safras | `POST` | `/harvests` | `ADMIN, TECHNICIAN` | Criação de safra (`start_date <= end_date`). |
| Safras | `PUT` | `/harvests/{id}` | `ADMIN, TECHNICIAN` | Atualização do ciclo de safra. |
| Safras | `DELETE`| `/harvests/{id}` | `ADMIN, TECHNICIAN` | Bloqueado com `409 Conflict` se houver planos vinculados ativos. |

---

### 4. Núcleo Produtivo e de Campo (`/production-plans`, `/production-executions`, `/field-occurrences`, `/attachments`)

| Módulo | Método | Endpoint | Papéis Permitidos | Regra de Escopo / Dono |
| :--- | :--- | :--- | :--- | :--- |
| Planos | `GET` | `/production-plans` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Produtor: seus planos; Técnico: carteira; Gestor: org; Admin: global. |
| Planos | `GET` | `/production-plans/{id}` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Validado pelo `ProductionAccessValidator`. |
| Planos | `POST` | `/production-plans` | `ADMIN, TECHNICIAN, FARMER` | Criação do planejamento produtivo para a safra. |
| Planos | `PUT` | `/production-plans/{id}` | `ADMIN, TECHNICIAN, FARMER` | Atualização com controle de versão otimista (`baseUpdatedAt`). |
| Planos | `DELETE`| `/production-plans/{id}` | `ADMIN, TECHNICIAN, FARMER` | Soft delete do plano de produção. |
| Apontamentos| `GET`| `/production-executions`| `ADMIN, MANAGER, TECHNICIAN, FARMER` | Filtrado automaticamente pelo escopo do ator. |
| Apontamentos| `POST`| `/production-executions`| `ADMIN, TECHNICIAN, FARMER` | Lançamento de colheita (`PENDING` por padrão se lançado por produtor). |
| Apontamentos| `PUT`| `/production-executions/{id}`| `ADMIN, TECHNICIAN, FARMER` | Farmer: reverte para PENDING se alterar peso/data; Tech: valida/rejeita. |
| Apontamentos| `DELETE`| `/production-executions/{id}`| `ADMIN, TECHNICIAN, FARMER` | Soft delete do apontamento de colheita. |
| Ocorrências | `GET` | `/field-occurrences` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Acesso aos registros fitossanitários/climáticos do apontamento. |
| Ocorrências | `POST`| `/field-occurrences` | `ADMIN, TECHNICIAN, FARMER` | Registro de praga, clima ou observação de campo. |
| Ocorrências | `PUT` | `/field-occurrences/{id}`| `ADMIN, TECHNICIAN, FARMER` | Atualização com suporte à limpeza de opcionais via string em branco. |
| Ocorrências | `DELETE`| `/field-occurrences/{id}`| `ADMIN, TECHNICIAN, FARMER` | Exclusão lógica da ocorrência. |
| Anexos | `GET` | `/production-executions/{eId}/attachments` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Consulta dos metadados fotográficos do apontamento. |
| Anexos | `POST` | `/production-executions/{eId}/attachments` | `ADMIN, TECHNICIAN, FARMER` | Upload multipart/form-data (máx 5MB, jpeg/png/webp, com clientId). |
| Anexos | `GET` | `/production-executions/{eId}/attachments/{aId}` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Download do binário da imagem comprobatória. |
| Anexos | `DELETE`| `/production-executions/{eId}/attachments/{aId}`| `ADMIN, TECHNICIAN, FARMER` | Soft delete do anexo fotográfico. |

---

### 5. Indicadores Analíticos e Relatórios (`/analytics`)

| Módulo | Método | Endpoint | Papéis Permitidos | Regra de Escopo / Latência |
| :--- | :--- | :--- | :--- | :--- |
| Timeline | `GET` | `/analytics/plans/{id}/timeline` | `ADMIN, MANAGER, TECHNICIAN, FARMER` | Curva S de colheita vs meta do plano (semanal ou mensal). |
| Agricultor | `GET` | `/analytics/farmers/me/summary` | `FARMER` | Totais da safra, cartões com status dinâmico e alertas operacionais. |
| Agricultor | `GET` | `/analytics/farmers/me/summary/export` | `FARMER` | Exportação PDF ou Excel (.xlsx). Grava auditoria `EXPORT`. |
| Técnico | `GET` | `/analytics/technicians/me/summary` | `TECHNICIAN` | Painel da carteira: comunidades, agricultores e motor de alertas. |
| Técnico | `GET` | `/analytics/technicians/me/summary/export` | `TECHNICIAN` | Exportação de relatório da assistência técnica (PDF/XLSX). |
| Gestor | `GET` | `/analytics/organizations/me/summary` | `MANAGER` | Baseado na view `mv_org_harvest_summary` (latência de até 5min). |
| Gestor | `GET` | `/analytics/organizations/me/summary/export` | `MANAGER` | Exportação de relatório da organização (PDF/XLSX). |
| Gestor/Admin| `GET` | `/analytics/organizations/{id}/summary` | `ADMIN, MANAGER` | Gestor acessa apenas a sua organização; Administrador acessa todas. |
| Gestor/Admin| `GET` | `/analytics/organizations/{id}/summary/export` | `ADMIN, MANAGER` | Exportação gerencial por organização (PDF/XLSX). |
| Admin | `GET` | `/analytics/global/summary` | `ADMIN` | Visão macro-gerencial do sistema (`scope: GLOBAL` preservado). |
| Admin | `GET` | `/analytics/global/summary/export` | `ADMIN` | Exportação de consolidação nacional/estadual (PDF/XLSX). |

---

### 6. Auditoria de Segurança (`/audit-logs`)

| Módulo | Método | Endpoint | Papéis Permitidos | Regra de Escopo / Dono |
| :--- | :--- | :--- | :--- | :--- |
| Auditoria | `GET` | `/audit-logs` | `ADMIN` | Consulta e auditoria de ações críticas (exportações, exclusões e acessos). |
