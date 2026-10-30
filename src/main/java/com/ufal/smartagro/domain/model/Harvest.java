package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Harvest {
    private UUID id;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public Harvest(UUID id, String label, LocalDate startDate, LocalDate endDate) {
        this(id, label, startDate, endDate, null, null, null);
    }
}
