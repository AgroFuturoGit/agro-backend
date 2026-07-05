package com.ufal.smartagro.adapters.in.web.dto.production;

import com.ufal.smartagro.adapters.in.web.dto.crop.CropResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ProductionPlanResponseDTO(
        UUID id,
        ProducerResponseDTO producer,
        HarvestResponseDTO harvest,
        CropResponseDTO crop,
        BigDecimal plantedArea,
        BigDecimal expectedYield,
        LocalDate plannedPlantingDate,
        Map<String, Object> plannedCalendar,
        LocalDateTime createdAt
) {
}
