package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.production.CreateProductionExecutionUseCase;
import com.ufal.smartagro.application.service.production.CreateProductionPlanUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class ProductionController {

    private final CreateProductionPlanUseCase createProductionPlanUseCase;
    private final CreateProductionExecutionUseCase createProductionExecutionUseCase;

    @PreAuthorize("hasRole('PRODUCER')")
    @PostMapping("/producers/{producerId}/production-plans")
    public ResponseEntity<ProductionPlanResponseDTO> createProductionPlan(
            @PathVariable UUID producerId,
            @Valid @RequestBody ProductionPlanRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionPlan plan = createProductionPlanUseCase.create(producerId, dto);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @PostMapping("/production-plans/{planId}/executions")
    public ResponseEntity<ProductionExecutionResponseDTO> createProductionExecution(
            @PathVariable UUID planId,
            @Valid @RequestBody ProductionExecutionRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionExecution execution = createProductionExecutionUseCase.create(planId, dto);
        ProductionExecutionResponseDTO response = Mapper.toProductionExecutionResponseDTO(execution);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
