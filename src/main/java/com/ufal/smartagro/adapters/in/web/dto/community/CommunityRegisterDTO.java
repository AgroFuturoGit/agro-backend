package com.ufal.smartagro.adapters.in.web.dto.community;

import jakarta.validation.constraints.NotBlank;

public record CommunityRegisterDTO(
    @NotBlank(message = "O nome da comunidade é obrigatório")
    String name
) {}
