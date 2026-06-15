package com.ufal.smartagro.domain.model;

import com.ufal.smartagro.domain.model.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Organization {
    private UUID id;
    private String name;
    private String taxId;
    private OrganizationType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
