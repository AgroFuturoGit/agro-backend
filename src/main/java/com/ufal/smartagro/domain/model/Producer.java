package com.ufal.smartagro.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Producer {
    private UUID id;
    private User user;
    private Community community;
    private String aliasName;
    private Boolean isCompliant;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
