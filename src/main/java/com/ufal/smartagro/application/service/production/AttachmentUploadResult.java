package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ExecutionAttachment;

/**
 * O anexo e a informação de como ele chegou ali.
 *
 * `created` separa o primeiro envio do reenvio já reconhecido pelo `clientId`,
 * para que a API responda 201 num caso e 200 no outro. Sem isso, o controlador
 * teria de adivinhar.
 */
public record AttachmentUploadResult(ExecutionAttachment attachment, boolean created) {
}
