package com.ufal.smartagro.application.service.production;

import java.util.UUID;

/**
 * Uma foto chegando para ser anexada, já fora do formato da web.
 *
 * O caso de uso não recebe `MultipartFile`: isso amarraria a regra de negócio
 * ao Spring Web. O controlador traduz, e o que entra aqui é só o conteúdo e
 * aquilo que o descreve.
 */
public record NewExecutionAttachment(
        UUID clientId,
        String filename,
        String contentType,
        byte[] content
) {
}
