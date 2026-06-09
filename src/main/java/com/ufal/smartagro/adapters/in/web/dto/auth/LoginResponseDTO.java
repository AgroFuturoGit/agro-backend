package com.ufal.smartagro.adapters.in.web.dto.auth;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;

public record LoginResponseDTO(
        String token,
        UserResponseDTO userResponseDTO
) {
}
