package com.ufal.smartagro.domain.exception;

/**
 * A atualização foi construída sobre uma versão do recurso que o servidor já
 * não tem — outro usuário alterou o mesmo registro nesse intervalo.
 *
 * Distinta de uma requisição inválida: o payload está correto, apenas perdeu a
 * corrida. Quem recebe o 409 deve reconciliar com a versão devolvida em
 * {@code current}, e não corrigir os dados enviados.
 */
public class ConcurrentUpdateException extends RuntimeException {

    /** O recurso como está no servidor, já no formato de resposta da API. */
    private final transient Object current;

    public ConcurrentUpdateException(String message, Object current) {
        super(message);
        this.current = current;
    }

    public Object getCurrent() {
        return current;
    }
}
