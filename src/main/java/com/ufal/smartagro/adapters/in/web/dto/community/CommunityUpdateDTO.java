package com.ufal.smartagro.adapters.in.web.dto.community;

import jakarta.validation.constraints.NotBlank;

public record CommunityUpdateDTO(
    @NotBlank(message = "O nome é obrigatório")
    String name
) {}
