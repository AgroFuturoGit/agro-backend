package com.ufal.smartagro.domain.model;

/** Resultado da autenticação contendo token de acesso e entidade do usuário autenticado. */
public record LoginResult(String token, User user) {}
