package com.ufal.smartagro.adapters.in.web.dto.crop;

import java.util.UUID;

public record CropResponseDTO(
        UUID id,
        String name,
        String variety,
        Boolean isPriority
) {
}
