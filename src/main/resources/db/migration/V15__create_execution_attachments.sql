-- Anexos fotográficos do apontamento de colheita: a comprovação visual de uma
-- ocorrência climática, de praga na lavoura ou de nota fiscal de insumo.
--
-- O binário fica no próprio banco, em BYTEA, e não no sistema de arquivos da
-- API. A razão é operacional: o contêiner da API não tem volume persistente,
-- então um arquivo gravado em disco desapareceria no próximo deploy, enquanto
-- o volume do Postgres já existe e já é o que se faz backup. A troca por
-- armazenamento de objetos (S3, MinIO) é o caminho natural quando o volume
-- crescer — e por isso as colunas de metadado ficam separadas do conteúdo na
-- leitura, de modo que a migração não mude o contrato da API.
CREATE TABLE execution_attachments (
    id UUID PRIMARY KEY,
    production_execution_id UUID NOT NULL
        REFERENCES production_executions (id),

    -- Identificador gerado no aparelho, antes do primeiro envio.
    --
    -- É o que torna o upload seguro para repetir. A fila offline reenvia
    -- quando a resposta se perde na rede — e sem esta chave o servidor não
    -- teria como distinguir "a mesma foto de novo" de "outra foto igual",
    -- gravando duas. Com ela, o reenvio devolve o anexo que já existe.
    client_id UUID,

    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    content BYTEA NOT NULL,

    created_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_execution_attachments_execution
    ON execution_attachments (production_execution_id);

-- Parcial de propósito: só vale entre os anexos vivos daquele apontamento.
-- Um anexo descartado não pode impedir que a mesma foto seja reenviada.
CREATE UNIQUE INDEX uq_execution_attachments_client_id
    ON execution_attachments (production_execution_id, client_id)
    WHERE client_id IS NOT NULL AND deleted_at IS NULL;

ALTER TABLE execution_attachments
    ADD CONSTRAINT chk_execution_attachment_size
        CHECK (size_bytes > 0 AND size_bytes <= 5242880),
    -- O app comprime antes de enviar; aceitar outros formatos abriria a porta
    -- para gravar no banco arquivo que nada no produto sabe exibir.
    ADD CONSTRAINT chk_execution_attachment_content_type
        CHECK (content_type IN ('image/jpeg', 'image/png', 'image/webp'));
