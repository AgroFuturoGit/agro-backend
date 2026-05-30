# 🚢 Deploy em Produção — Backend

Sobe PostgreSQL + Spring Boot em containers. O frontend é deployado de forma independente no próprio repositório [agro-frontend](../agro-frontend).

Para o fluxo de **desenvolvimento** (só DB em Docker), ver [README.md](README.md).

---

## Visão geral

| Serviço | Imagem | Porta exposta |
|---|---|---|
| `db` | `postgres:17-alpine` | só rede interna |
| `backend` | build de [Dockerfile](Dockerfile) | `8080` |

Backend conecta no DB via DNS interno `db:5432`. Healthcheck via Actuator na porta interna `9090`.

---

## Pré-requisitos

- **Docker** ≥ 24 e **Docker Compose v2**

---

## 1. Configurar variáveis de ambiente

```bash
cp .env.example .env.prod
chmod 600 .env.prod
```

Editar `.env.prod` com valores reais:

```bash
DB_NAME=agro_backend
DB_USER=agro_prod
DB_PASSWORD=<senha-forte>
JWT_SECRET=<mínimo-32-chars — gerar com: openssl rand -base64 48>

# URL do frontend (usada pra liberar CORS no backend)
PUBLIC_FRONTEND_URL=https://app.seu-dominio.com
```

---

## 2. Build e subida

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Verificar:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml ps
# db (healthy), backend (healthy)

curl http://localhost:8080/auth/login   # deve retornar 405 ou 400
```

---

## 3. Ver logs

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml logs -f
docker compose --env-file .env.prod -f docker-compose.prod.yml logs --tail=200 backend
```

---

## 4. Atualizar para nova versão

```bash
git pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Flyway aplica migrations novas automaticamente no startup.

---

## 5. Backup do banco

```bash
docker compose -f docker-compose.prod.yml exec -T db \
  pg_dump -U "$DB_USER" "$DB_NAME" | gzip > backup-$(date +%Y%m%d).sql.gz
```

Restaurar:

```bash
gunzip -c backup-AAAAMMDD.sql.gz | docker compose -f docker-compose.prod.yml exec -T db \
  psql -U "$DB_USER" -d "$DB_NAME"
```

---

## 6. Parar

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down       # mantém dados
docker compose --env-file .env.prod -f docker-compose.prod.yml down -v    # apaga banco (cuidado!)
```
