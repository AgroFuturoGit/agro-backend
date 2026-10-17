## Especificação do Modelo de Dados - Projeto SmartAgro (ProduPlan)

Data: 16 de outubro de 2026

UML Classes: pendente de atualização para a v2.0

Diagrama ER: pendente de atualização para a v2.0

Versão: 2.1

Este documento detalha a modelagem lógica do banco de dados PostgreSQL para o sistema SmartAgro, cujo nome de aplicação se consolida como ProduPlan, seguindo os requisitos de gestão social e produtiva para a agricultura familiar. A modelagem segue nomeação em inglês, utiliza soft delete e contém um fluxo auditável.

Esta versão incorpora os ajustes de banco exigidos pelo [Epic 5] Adequação de Negócio, Reestruturação de Perfis e Refatoração de Banco de Dados (issue #13) pelo [Epic 6] Dashboards Analíticos e Relatórios de Produção (issue #18) e pelas Features 3.8 (#11) e 3.9 (#12) do [Epic 3] Aplicativo Mobile de Operação de Campo Offline-first, do repositório AgroFuturoGit/agro-docs. Convenção: nas tabelas de atributos, os itens em negrito são novos ou alterados nesta versão.

## 0. Histórico de Versões

| Versão Data |   | Descrição |
| --- | --- | --- |
| 1.0 | 05/05/2026 | Versão inicial do modelo de dados. |
| 2.0 | 06/10/2026 | Refatoração para os Épicos 5 e 6: renomeação Producer → Farmer, |
|   |   | desacoplamento de perfis (preparação para autocadastro), formalização de |
|   |   | Manager, assistência técnica por comunidade, evolução do core agrícola |
|   |   | (ciclo, colheita contínua, apontamentos validados, geolocalizados e com |
|   |   | anexos fotográficos), views analíticas, índices e padronização dos campos de |
|   |   | sincronização. |
| 2.1 | 16/10/2026 | Detalhamento das regras das views analíticas e da trilha de |
|   |   | auditoria (Feature 6.6): justificativa de rejeição em ProductionExecution, |
|   |   | colunas e regras de vw_active_farmers, vw_plan_progress e |
|   |   | mv_org_harvest_summary, e novo enum audit_action. Alterações apenas |
|   |   | aditivas, sem quebra de compatibilidade. |

A versão sobe de 1.0 para 2.0 (incremento major, conforme versionamento semântico) porque contém mudanças incompatíveis: renomeação de tabela, colunas, chaves estrangeiras e valor de enum, além da remoção de colunas. Consultas, entidades JPA e contratos de API escritos para a v1.0 deixam de funcionar sem migração.

## 1. Resumo das Mudanças

| Tipo | Mudança | Origem |
| --- | --- | --- |
| Quebra | Tabela Producer renomeada para Farmer | #14 |
| Quebra | FK producer_id renomeada para farmer_id em FamilyComposition, Financing e | #14 |
|   | ProductionPlan |   |
| Quebra | TechnicalAssistance passa a vincular o Técnico à Comunidade: producer_id | #17, #20 |
|   | substituído por community_id. O técnico atende todos os agricultores das |   |
|   | comunidades a que está vinculado |   |
| Quebra | Enum user_role: valor PRODUCER substituído por FARMER | #14 |
| Quebra Dados pessoais do agricultor (date_of_birth, mother_name, origin, |   | #15 |
|   | education_level) movidos de User para Farmer; User passa a guardar só |   |
|   | credenciais e perfil |   |
| Quebra | Farmer.alias_name substituído por local_name (Endereço/Nome Local, padrão | #15 |
|   | IBGE) |   |


| Tipo | Mudança | Origem |
| --- | --- | --- |
| Quebra | Technician.professional_id substituído por registration_type + | #15 |
|   | registration_number |   |
| Quebra | ProductionPlan.expected_yield removido; a meta passa a ser calculada | #16, #19 |
|   | (planted_area × expected_productivity) |   |
| Quebra | ProductionExecution.actual_yield e recorded_at substituídos por quantity, | #16, #22 |
|   | quantity_kg e harvested_at |   |
| Adição | Nova tabela Manager (vínculo Gestor ↔ Organização), necessária para o | #18, #21 |
|   | isolamento de dados do Gestor |   |
| Adição | Farmer: user_id opcional e único, full_name, cpf (único), endereço, | #15 |
|   | coordenadas GPS, registration_source, created_by |   |
| Adição | Crop: cycle_days, expected_productivity, harvest_type, unit, unit_weight_kg | #16, #22 |
| Adição | ProductionPlan: status, planting_date, expected_harvest_start, | #16, #19, |
|   | expected_harvest_end, expected_productivity (snapshot), localização e GPS | #20 |
| Adição | ProductionExecution: status de validação (status, validated_by, validated_at) e | #18, #19 |
|   | notes |   |
| Adição | ProductionExecution: latitude e longitude opcionais, capturadas no momento | #11 |
|   | do apontamento (inclusive offline) |   |
| Adição | Nova tabela ExecutionAttachment para anexos fotográficos dos apontamentos #12 |   |
| Adição | created_by em Community, Technician e Farmer | #15 |
| Adição | Views vw_active_farmers e vw_plan_progress e view materializada | #18, #19, |
|   | mv_org_harvest_summary | #21 |
| Adição | Índices parciais para as consultas analíticas | #18 |
| Adição | Seis novas enumerações (seção 3) | #15, #16, |
|   |   | #19 |
| Correção created_at e updated_at adicionados às tabelas que não os tinham, conforme |   | Padronizaç |
|   | a regra de campos de sincronização | ão |
| Correção AuditLog passa a ser append-only: removido deleted_at e adicionado target_id Padronizaç |   |   |
|   |   | ão |
| Correção JSON padronizado como JSONB nas descrições de tabela |   | Padronizaç |
|   |   | ão |

## 2. Mapa de Migração (v1.0 → v2.0)

| Objeto | v1.0 | v2.0 |
| --- | --- | --- |
| Tabela | Producer | Farmer |
| Coluna (FK) | *.producer_id | *.farmer_id (exceto TechnicalAssistance) |
| Coluna (FK) | TechnicalAssistance.producer_id | TechnicalAssistance.community_id. |
|   |   | Migração: um vínculo por par técnico × |
|   |   | comunidade distinto, derivado dos |
|   |   | agricultores assistidos na v1.0 |
| Enum user_role PRODUCER |   | FARMER |
| Claim JWT | ROLE_PRODUCER | ROLE_FARMER |
| Coluna | Producer.alias_name | Farmer.local_name |


| Objeto | v1.0 | v2.0 |
| --- | --- | --- |
| Colunas | User.date_of_birth, mother_name, | Farmer.date_of_birth, mother_name, origin, |
|   | origin, education_level | education_level |
| Coluna | User.cpf (obrigatório) | Farmer.cpf (único, fonte oficial do |
|   |   | agricultor); User.cpf opcional, usado por |
|   |   | Técnico, Gestor e Admin |
| Coluna | Technician.professional_id | Technician.registration_type + |
|   |   | registration_number |
| Coluna | ProductionPlan.expected_yield | Removida; calculada em vw_plan_progress |
| Coluna | ProductionExecution.actual_yield | ProductionExecution.quantity + quantity_kg |
| Coluna | ProductionExecution.recorded_at | ProductionExecution.harvested_at (DATE) |
| Coluna | AuditLog.deleted_at | Removida (log imutável) |
| Termo de | Assentamento | Comunidade (Organização / Comunidade) |
| interface |   |   |

## 3. Relacionamentos Principais

O diagrama a seguir ilustra as dependências entre os módulos do sistema:

| De (Tabela) | Para (Tabela) | Cardinalida | Descrição |
| --- | --- | --- | --- |
|   |   | de |   |
| Organization | Community | 1 → N | Uma organização pode ter várias comunidades |
| Community | Farmer | 1 → N | Uma comunidade agrupa vários agricultores |
| User | Farmer | 1 → 0..1 | O agricultor pode existir sem usuário de |
|   |   |   | login (cadastrado pelo técnico); quando |
|   |   |   | houver conta, o vínculo é único |
| User | Manager | 1 → 1 | Cada gestor tem um usuário de login |
| Organization | Manager | 1 → 1 | Uma organização contém um gestor |
|   |   |   | (cardinalidade pendente de validação; ver seção |
|   |   |   | 15) |
| User | Technician | 1 → 0..1 | Um técnico é opcionalmente vinculado a um |
|   |   |   | usuário |
| Farmer | FamilyComposition 1 → N |   | Um agricultor pode ter vários membros |
|   |   |   | familiares |
| Farmer | Financing | 1 → N | Um agricultor pode ter vários financiamentos |
| Farmer | ProductionPlan | 1 → N | Um agricultor cria vários planos por safra |
| Harvest | ProductionPlan | 1 → N | Uma safra agrupa vários planos de produção |
| Crop | ProductionPlan | 1 → N | Uma cultura é usada em vários planos |
| ProductionPlan | ProductionExecutio | 1 → N | Um plano recebe múltiplos apontamentos de |
|   | n |   | colheita (ciclo único ou contínuo) |
| ProductionExecut | FieldOccurrence | 1 → N | Uma execução pode registrar várias ocorrências |
| ion |   |   |   |
| ProductionExec | ExecutionAttachm | 1 → N | Um apontamento pode ter várias fotos |
| ution | ent |   | anexadas |


| De (Tabela) | Para (Tabela) | Cardinalida | Descrição |
| --- | --- | --- | --- |
|   |   | de |   |
| Technician | TechnicalAssistanc | N → N | Via tabela de vínculo com Community: um |
|   | e |   | técnico atende várias comunidades e uma |
|   |   |   | comunidade pode ter vários técnicos |
| User | ProductionExecuti | 1 → N | Um usuário (técnico ou gestor) valida vários |
|   | on |   | apontamentos (validated_by) |
| User | Community, | 1 → N | Registro de quem criou o cadastro |
|   | Technician, |   | (created_by) |
|   | Farmer |   |   |
| User | AuditLog | 1 → N | Todo usuário gera entradas de auditoria |

## 4. Enumerações

| Enum | Valores | Uso |
| --- | --- | --- |
| user_role | ADMIN, MANAGER, TECHNICIAN, | User.role |
|   | FARMER |   |
| organization_type | COOP, ASSOC | Organization.type |
| occurrence_type | CLIMATE, PEST, DISEASE | FieldOccurrence.log_type |
| registration_source | SELF, TECHNICIAN, MANAGER | Farmer.registration_source |
|   |   | (SELF reservado para o |
|   |   | autocadastro) |
| professional_registration | CREA, CFT, CFTA | Technician.registration_type |
| _type |   |   |
| harvest_type | SINGLE, CONTINUOUS | Crop.harvest_type |
| measurement_unit | KG, TON, SACK, BOX | Crop.unit |
| plan_status | PLANNED, ACTIVE, FINISHED, | ProductionPlan.status |
|   | CANCELED |   |
| execution_status | PENDING, VALIDATED, REJECTED ProductionExecution.status |   |
| audit_action | CREATE, UPDATE, DELETE, VALIDATE, | AuditLog.action |
|   | REJECT, EXPORT |   |

## 5. Módulo de Acesso e Segurança (RBAC)

Este módulo gerencia a autenticação e as permissões de acesso baseadas em perfis (Roles).

| Modelo (Tabela) Atributos |   | Descrição |
| --- | --- | --- |
| User | id (PK), full_name, email (opcional), phone, | Entidade central de login. Guarda |
|   | password, role (Enum user_role), cpf | apenas credenciais e perfil. Os |
|   | (opcional), created_at, updated_at, deleted_at | dados sociais do agricultor |
|   |   | foram movidos para Farmer. |
|   |   | Roles: ADMIN, MANAGER, |
|   |   | TECHNICIAN, FARMER. |
| Organization | id (PK), name, tax_id (CNPJ), type (Enum | Representa as Cooperativas ou |
|   | organization_type), created_at, updated_at, | Associações. tax_id armazena o |
|   | deleted_at | CNPJ. |
| Community | id (PK), name, organization_id (FK), | Comunidades rurais vinculadas a |
|   | created_by (FK User), created_at, | uma organização. Criação restrita |
|   | updated_at, deleted_at | a MANAGER e ADMIN. |


| Modelo (Tabela) Atributos |   | Descrição |
| --- | --- | --- |
| Manager | id (PK), user_id (FK, único), organization_id | Perfil do gestor. Define a |
|   | (FK), created_at, updated_at, deleted_at | organização cujos dados o |
|   |   | MANAGER pode visualizar. |

## 6. Módulo Social e Perfil do Agricultor

Gerencia os dados pessoais, familiares e a situação financeira dos agricultores.

| Modelo (Tabela) Atributos |   | Descrição |
| --- | --- | --- |
| Farmer (antigo | id (PK), user_id (FK, opcional, único), | Perfil do agricultor e fonte oficial |
| Producer) | **community_id (FK, opcional)**, full_name, | dos seus dados pessoais. Pode |
|   | cpf (único), date_of_birth, mother_name, | existir sem usuário de login. **O |
|   | origin, education_level, phone, local_name, | vínculo com a comunidade |
|   | street, city, state, ibge_code, latitude, | (`community_id`) passa a ser |
|   | longitude, registration_source (Enum), | opcional para suportar a criação |
|   | created_by (FK User), is_compliant, | inicial do perfil do agricultor via |
|   | created_at, updated_at, deleted_at | autocadastro (antes de ser |
|   |   | aprovado e vinculado por um |
|   |   | gestor).** `local_name` substitui o |
|   |   | antigo apelido (padrão IBGE). |
|   |   | `is_compliant` indica adimplência |
|   |   | financeira. |
| FamilyCompositio | id (PK), farmer_id (FK), name, kinship, age, | Registra os membros da família do |
| n | created_at, updated_at, deleted_at | agricultor para fins de cadastro |
|   |   | social. |
| Financing | id (PK), farmer_id (FK), description, status, | Armazena empréstimos ou |
|   | amount, created_at, updated_at, deleted_at | financiamentos rurais vinculados |
|   |   | ao agricultor. |

## 7. Módulo de Planejamento e Produção Agrícola

Núcleo operacional do sistema, onde se organiza o ciclo de vida das culturas.

| Modelo (Tabela) Atributos |   | Descrição |
| --- | --- | --- |
| Harvest | id (PK), label (ex: "Safra 2026"), start_date, | Define o período da safra. A safra |
|   | end_date, created_at, updated_at, deleted_at | vigente é a que contém a data |
|   |   | atual entre start_date e end_date. |
| Crop | id (PK), name, variety, is_priority (Boolean), | Catálogo de culturas e variedades. |
|   | cycle_days, expected_productivity (kg/ha), | harvest_type indica colheita |
|   | harvest_type (Enum), unit (Enum | única ou contínua/escalonada |
|   | measurement_unit), unit_weight_kg, | (ex: fruticultura). unit_weight_kg |
|   | created_at, updated_at, deleted_at | converte a unidade de |
|   |   | comercialização (saca, caixa) |
|   |   | para kg. |
| ProductionPlan | id (PK), farmer_id (FK), harvest_id (FK), | O planejamento: quanto será |
|   | crop_id (FK), planted_area (ha), | plantado, onde e quando. |
|   | expected_productivity (kg/ha, snapshot), | expected_productivity é copiada |
|   | status (Enum plan_status), planting_date, | de Crop na criação, para que |
|   | expected_harvest_start, | mudanças no catálogo não |
|   | expected_harvest_end, | alterem o histórico. A meta é |
|   | location_description, latitude, longitude, | planted_area × |
|   | planned_calendar (JSONB), created_at, | expected_productivity. Planos |
|   | updated_at, deleted_at | de colheita contínua pertencem |
|   |   | à safra em que foram iniciados e |


| Modelo (Tabela) Atributos | Descrição |
| --- | --- |
|   | podem receber apontamentos |
|   | após o fim dela. |
| ProductionExecuti id (PK), production_plan_id (FK), | Apontamento de colheita. Um |
| on | harvested_at (DATE), quantity, quantity_kg, plano recebe N apontamentos. |
| status (Enum execution_status), | quantity está na unidade da |
|   | validated_by (FK User), validated_at, notes, rejection_reason, cultura e quantity_kg é o valor |
| latitude, longitude, created_at, updated_at, | normalizado, calculado no |
| deleted_at | registro. Só apontamentos |
|   | VALIDATED entram no realizado. |
|   | harvested_at é a data da colheita; |
|   | created_at, a do registro (relevante |
|   | para o uso offline). latitude e |
|   | longitude são opcionais: |
|   | registram onde o apontamento |
|   | foi feito, capturadas no aparelho |
|   | no momento do registro e |
|   | enviadas inalteradas na |
|   | sincronização. Se o GPS falhar, |
|   | o apontamento é salvo sem |
|   | coordenadas. notes guarda só as |
|   | observações do autor do apontamento; |
|   | rejection_reason (opcional) guarda a |
|   | justificativa informada pelo validador |
|   | ao rejeitar (status REJECTED) e é |
|   | limpo se o apontamento voltar a |
|   | PENDING ou for VALIDATED. |
| ExecutionAttach id (PK), execution_id (FK), file_url, | Anexos fotográficos do |
| ment | mime_type, file_size, created_by (FK User), apontamento (ocorrências, |
| created_at, updated_at, deleted_at | pragas, notas fiscais de |
|   | insumos). O arquivo fica no |
|   | armazenamento de objetos e o |
|   | banco guarda só a URL. O |
|   | registro é criado quando o |
|   | upload é confirmado pelo |
|   | servidor. |

## 8. Módulo de Assistência Técnica e Eventos de Campo

Registra o trabalho dos técnicos e problemas encontrados durante a safra.

| Modelo (Tabela) Atributos |   | Descrição |
| --- | --- | --- |
| Technician | id (PK), user_id (FK), registration_type | Dados do técnico agrícola (CREA, |
|   | (Enum), registration_number, specialty, | CFT ou CFTA). Criação restrita a |
|   | created_by (FK User), created_at, | MANAGER e ADMIN. |
|   | updated_at, deleted_at |   |
| TechnicalAssistanc | id (PK), technician_id (FK), community_id | Vínculo entre técnico e |
| e | (FK), start_date, end_date, created_at, | comunidade. O técnico assiste |
|   | updated_at, deleted_at | todos os agricultores ativos das |
|   |   | comunidades vinculadas, |
|   |   | inclusive os que entrarem |
|   |   | depois. Vínculo ativo: end_date |
|   |   | nula ou maior ou igual à data |
|   |   | atual. Base obrigatória do |
|   |   | isolamento de dados do |
|   |   | TECHNICIAN. |
| FieldOccurrence | id (PK), execution_id (FK), log_type (Enum | Registros de pragas, doenças ou |
|   | occurrence_type), description, | eventos climáticos críticos, com |
|   | weather_observation, evidence_url, | suporte a fotos (URL). |
|   | created_at, updated_at, deleted_at |   |


## 9. Módulo de Visualização de Dados via Dashboard

Este módulo não possui tabelas próprias. Os dados são compostos por views e agregações sobre os módulos anteriores, todas executadas no PostgreSQL (SUM, GROUP BY), nunca no frontend.

## 9.1 Views

| View | Tipo | Conteúdo |
| --- | --- | --- |
| vw_active_farmers | View | Agricultores com deleted_at IS NULL. Ponto único de |
|   |   | entrada de todas as consultas analíticas e exportações. |
|   |   | Quando o autocadastro sair do standby, recebe |
|   |   | também o filtro registration_status = APPROVED. Não filtra por |
|   |   | is_compliant: a coluna é exposta para o indicador de Adimplência |
|   |   | (seção 9.2), que precisa dos agricultores inadimplentes. |
| vw_plan_progress | View | Por plano: meta em kg (planted_area × |
|   |   | expected_productivity), realizado em kg (soma de |
|   |   | quantity_kg dos apontamentos VALIDATED e não |
|   |   | excluídos), percentual de conclusão e data do último |
|   |   | apontamento. Construída sobre vw_active_farmers. Colunas: plan_id, |
|   |   | farmer_id, community_id, organization_id, harvest_id, crop_id, |
|   |   | plan_status, planted_area, expected_productivity, target_kg, |
|   |   | realized_kg, completion_pct, last_execution_date. Regras: o plano |
|   |   | sem apontamentos aparece com realized_kg = 0; completion_pct pode |
|   |   | ultrapassar 100 e é nulo quando a meta é 0; planos CANCELED |
|   |   | permanecem na view, com seu status; last_execution_date é a data |
|   |   | do último apontamento não excluído, de qualquer status, pois mede |
|   |   | a atividade em campo e não a validação. |
| mv_org_harvest_summ | View | Consolidação por organização × comunidade × cultura |
| ary | materializada | × safra para o dashboard gerencial. Atualizada em |
|   |   | background com REFRESH MATERIALIZED VIEW |
|   |   | CONCURRENTLY (exige índice único). Colunas: plan_count, |
|   |   | farmer_count, planted_area_ha, target_kg, realized_kg, |
|   |   | completion_pct, last_execution_date. Parte da vw_plan_progress e |
|   |   | exclui os planos CANCELED, para que a meta e o percentual da |
|   |   | organização não sejam inflados por planos que não serão |
|   |   | colhidos, e os agricultores sem comunidade, que não pertencem a |
|   |   | nenhuma organização. Reflete os dados com defasagem de até o |
|   |   | intervalo de atualização (padrão de 5 minutos). Não há isolamento |
|   |   | por perfil no banco: toda consulta deve filtrar por |
|   |   | organization_id (MANAGER) ou community_id (TECHNICIAN). |

## 9.2 Indicadores

| Indicador | Fonte de Dados | Descrição |
| --- | --- | --- |
| Produção por Safra | vw_plan_progress + | Meta (kg) × realizado (kg) por safra |
|   | Harvest |   |
| Produção por Cultura | Crop + vw_plan_progress | Volume planejado e executado por cultura. |
|   |   | Inclui ranking por volume e taxa de |
|   |   | conversão (previsto × realizado). |
| Produção por | Community + | Consolidação de produção por localidade |
| Comunidade | vw_plan_progress |   |
| Ocorrências de Campo | FieldOccurrence | Frequência de pragas, doenças e eventos |
|   |   | climáticos |
| Adimplência dos | vw_active_farmers + | Percentual de agricultores com is_compliant = |
| Agricultores | Financing | true |
| Histórico por Agricultor | vw_plan_progress + | Evolução da produção ao longo das safras |
|   | Harvest |   |
| Prognóstico de Produção ProductionPlan |   | Soma de planted_area × |
|   |   | expected_productivity para a safra vigente |
| Série Temporal de | ProductionExecution + | Apontamentos agrupados com date_trunc |
| Colheita | Crop | (semana, quinzena ou mês) por |
|   |   | harvested_at, comparados à curva teórica |
|   |   | (meta distribuída pelo cycle_days a partir |
|   |   | de planting_date) |
| Alertas do Técnico | vw_plan_progress + | Planos ACTIVE sem apontamento há mais |
|   | TechnicalAssistance (por | de 15 dias, ou com realizado 30% abaixo |
|   | community_id) | da curva esperada para o ciclo |

## 10. Auditoria e Controle


| Modelo (Tabela) Atributos |   | Descrição |
| --- | --- | --- |
| AuditLog | id (PK), user_id (FK), action, target_table, | Trilha de auditoria para alterações |
|   | target_id, details (JSONB), created_at | em dados sensíveis. Tabela |
|   |   | append-only: não tem |
|   |   | updated_at nem deleted_at, e os |
|   |   | registros nunca são alterados |
|   |   | ou excluídos, regra imposta pelo |
|   |   | próprio banco (UPDATE, DELETE e |
|   |   | TRUNCATE são bloqueados). user_id é |
|   |   | obrigatório (quem executou a ação); |
|   |   | target_id é opcional, pois ações |
|   |   | sem alvo único (ex.: EXPORT) não o |
|   |   | têm. action usa o enum audit_action. |
|   |   | O registro é gravado na mesma |
|   |   | transação da ação auditada. Ações |
|   |   | registradas: criação de Comunidade |
|   |   | e de Técnico, validação e rejeição |
|   |   | de apontamentos e, quando existir, |
|   |   | exportação de relatórios (EXPORT). |

## 11. Preparação para Autocadastro (standby)

O autocadastro do agricultor fica em standby, conforme a Feature 5.2 (#15). A v2.0 já adota a estrutura de identidade necessária para ele, de modo que a funcionalidade futura entre como código novo, sem refatoração do modelo.

| Item | Situação |
| --- | --- |
| Farmer.user_id opcional e único | Em vigor na v2.0 |
| Farmer.cpf (único) e dados pessoais em Farmer | Em vigor na v2.0 |
| Farmer.registration_source | Em vigor na v2.0 (valor SELF |
|   | reservado) |
| vw_active_farmers como ponto único das consultas analíticas | Em vigor na v2.0 |
| Farmer.registration_status, approved_by, approved_at | Reservado para a versão do |
|   | autocadastro |
| Farmer.requested_community_id | Reservado para a versão do |
|   | autocadastro |
| User.status e verificação de e-mail/telefone | Reservado para a versão do |
|   | autocadastro |
| Tabela VerificationToken | Reservado para a versão do |
|   | autocadastro |
| Consentimento LGPD (terms_accepted_at, terms_version) | Reservado para a versão do |
|   | autocadastro |

## 11.1 Regras de implementação desde já

- 1. Nunca assumir Farmer.user_id preenchido: usar LEFT JOIN com User e tratar o agricultor sem conta.

- 2. Dados pessoais do agricultor são lidos de Farmer, não de User.

- 3. Consultas analíticas e exportações partem sempre de vw_active_farmers, nunca da tabela Farmer diretamente.

- 4. O token com ROLE_FARMER resolve o agricultor por Farmer.user_id; se não houver correspondência, a API retorna 403.

- 5. Nunca assumir Farmer.community_id preenchido: Como o autocadastro exige a criação prévia do Farmer para guardar o CPF antes do vínculo oficial com a Organização, a chave estrangeira `community_id` deve ser tratada como `NULLABLE` no banco de dados e a aplicação deve prever agricultores com status pendente de vínculo. O campo `requested_community_id` será usado futuramente para a intenção de vínculo.

## 12. Observações Técnicas


| Tópico | Descrição |
| --- | --- |
| UUIDs | Utilizar a extensão pgcrypto ou gen_random_uuid() do PostgreSQL para geração |
|   | automática de UUIDs. |
| Soft Delete | Todas as queries de leitura devem incluir WHERE deleted_at IS NULL. Exceção: |
|   | AuditLog, que é append-only. |
| JSONB | Os campos planned_calendar (ProductionPlan) e details (AuditLog) utilizam |
|   | JSONB com suporte a índices GIN. |
| Enums | Criar os tipos ENUM no PostgreSQL antes das tabelas que os referenciam. A |
|   | troca de PRODUCER por FARMER exige ALTER TYPE ... RENAME VALUE. |
| Coordenadas GPS | latitude e longitude em NUMERIC(9,6). A adoção de PostGIS |
|   | (geography(Point)) fica como evolução futura. |
| Unidades | Área em hectares, produtividade em kg/ha e realizado normalizado em kg |
|   | (quantity_kg). Valores numéricos em NUMERIC, nunca em texto, para |
|   | preservar os tipos na exportação. |
| Índices | Indexar FKs, colunas deleted_at e campos de busca frequente (cpf, email, |
|   | tax_id). Índices parciais (WHERE deleted_at IS NULL) em: |
|   | farmer(community_id), community(organization_id), |
|   | production_plan(farmer_id, harvest_id, crop_id), |
|   | production_execution(production_plan_id, harvested_at), |
|   | technical_assistance(technician_id, community_id), |
|   | manager(organization_id), |
|   | execution_attachment(execution_id). Índice parcial adicional em |
|   | production_execution(production_plan_id), incluindo quantity_kg, apenas |
|   | para status = VALIDATED, que sustenta o cálculo do realizado em |
|   | vw_plan_progress. AuditLog: índices por (target_table, target_id), |
|   | (user_id, created_at) e (created_at). Índice único em farmer(cpf) e |
|   | farmer(user_id). |
| Desempenho | Metas dos critérios de aceite: dashboard do agricultor abaixo de 500 ms e |
|   | dashboard gerencial abaixo de 2 s. |
| Offline | UUIDs garantem que registros criados offline não colidam com IDs gerados no |
|   | servidor na sincronização. |
| Campos de | Todas as tabelas (exceto tabelas de relacionamento puro N:N e AuditLog) |
| Sincronização | devem conter created_at, updated_at e deleted_at do tipo TIMESTAMP. Na v2.0 |
|   | todas as tabelas foram ajustadas a essa regra. |

## 13. Perfis de Acesso (RBAC)

| Role | Descrição | Acesso Principal |
| --- | --- | --- |
| ADMIN | Administrador do sistema Acesso total: configurações, usuários, auditoria e todas |   |
|   |   | as organizações. |
| MANAGER | Gestor de | Somente as comunidades e agricultores da |
|   | cooperativa/associação | organização definida em Manager.organization_id. |
|   |   | Relatórios gerenciais. Cria Comunidades e Técnicos. |
| TECHNICIAN | Técnico | Somente os agricultores das comunidades com |
|   | agrícola/agronomia | vínculo ativo em TechnicalAssistance, com visão |
|   |   | agregada por comunidade. Cadastra ocorrências e |
|   |   | valida apontamentos. |
| FARMER | Agricultor/Produtor rural | Gerencia seus próprios dados, família, planos e |
|   |   | apontamentos. Isolamento pelo farmer_id resolvido a |
|   |   | partir do token. |

Os endpoints de exportação herdam exatamente as mesmas regras de isolamento dos dashboards.

## 14. UML


| Artefato | Descrição |
| --- | --- |
| Diagrama ER | Modelagem Entidade-Relacionamento com as 16 tabelas (inclusão de Manager |
|   | e ExecutionAttachment) e relacionamentos. Pendente de atualização para a |
|   | v2.0. |
| Diagrama de Classes | 16 classes UML com atributos, métodos e 9 enumerações. Pendente de |
|   | atualização para a v2.0. |

## 15. Questões em Aberto

| Questão | Impacto |
| --- | --- |
| Cardinalidade Manager ↔ Organization: 1:1 | Mantida como 1:1, pendente de validação com |
| ou 1:N? | especialistas do domínio. A estrutura da tabela Manager |
|   | já suporta 1:N. |
| Talhão: criar a tabela Plot (farmer_id, nome, | Na v2.0, o próprio ProductionPlan carrega a localização. |
| área, GPS) ou tratar o plano como talhão? | A Feature 6.1 cita "talhões ativos". |
| Impacto financeiro da safra: criar CropPrice | O Épico 6 cita impacto financeiro, mas nenhuma tabela |
| (crop_id, harvest_id, price_per_unit) ou tirar do | guarda preço. |
| escopo? |   |
| Responsável pelo catálogo de culturas (Crop) Proposta: catálogo centralizado mantido por ADMIN e |   |
|   | MANAGER. |
