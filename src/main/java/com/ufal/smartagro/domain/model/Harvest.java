package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Harvest {
    private UUID id;
    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
}
