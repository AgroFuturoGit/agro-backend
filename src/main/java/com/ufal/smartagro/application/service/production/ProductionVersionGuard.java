package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.ConcurrentUpdateException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Controle de concorrência otimista nas atualizações de produção.
 *
 * O cliente informa em {@code baseUpdatedAt} a versão do recurso que tinha em
 * mãos quando editou. Se o servidor já avançou além dela, a escrita é recusada
 * com 409 em vez de sobrescrever o trabalho de outra pessoa.
 *
 * Comparar contra a versão-base, e não contra o instante da edição, é o que
 * torna a detecção possível: um registro feito offline chega ao servidor com
 * carimbo de tempo sempre mais recente, então uma comparação por "quem escreveu
 * por último" nunca acusaria conflito algum.
 *
 * A verificação é opcional. Cliente que não envia {@code baseUpdatedAt} mantém
 * o comportamento anterior — necessário enquanto o frontend web não adota o
 * campo.
 */
@Component
public class ProductionVersionGuard {

    /**
     * A serialização JSON e a coluna do banco não guardam a mesma precisão de
     * fração de segundo, então a comparação é feita em milissegundos. Sem isso,
     * um valor que o próprio servidor devolveu poderia voltar "diferente" e
     * gerar conflito onde não houve.
     */
    private static final ChronoUnit PRECISAO = ChronoUnit.MILLIS;

    public void ensureUpToDate(
            LocalDateTime baseUpdatedAt,
            LocalDateTime currentUpdatedAt,
            LocalDateTime currentCreatedAt,
            Object currentResource,
            String message
    ) {
        if (baseUpdatedAt == null) return;

        // Registro nunca atualizado não tem `updatedAt`; nesse caso a versão é
        // o próprio instante de criação.
        LocalDateTime versaoServidor = currentUpdatedAt != null ? currentUpdatedAt : currentCreatedAt;
        if (versaoServidor == null) return;

        if (!baseUpdatedAt.truncatedTo(PRECISAO).isEqual(versaoServidor.truncatedTo(PRECISAO))) {
            throw new ConcurrentUpdateException(message, currentResource);
        }
    }
}
