package com.ufal.smartagro.adapters.in.web.dto.farmer;

import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import java.time.LocalDateTime;
import java.util.UUID;

public record FarmerResponseDTO(
    UUID id,
    UserResponseDTO user,
    CommunityResponseDTO community,
    String aliasName,
    Boolean isCompliant,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
