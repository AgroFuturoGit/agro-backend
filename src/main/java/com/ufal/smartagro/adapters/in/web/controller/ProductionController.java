package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.*;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.production.*;
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

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class ProductionController {

    private final CreateProductionPlanUseCase createProductionPlanUseCase;
    private final CreateProductionExecutionUseCase createProductionExecutionUseCase;
    private final UpdateProductionPlanUseCase updateProductionPlanUseCase;
    private final ListProductionPlansUseCase listProductionPlansUseCase;
    private final FindProductionPlanByIdUseCase findProductionPlanByIdUseCase;
    private final DeleteProductionPlanUseCase deleteProductionPlanUseCase;
    
    private final UpdateProductionExecutionUseCase updateProductionExecutionUseCase;
    private final ListProductionExecutionsUseCase listProductionExecutionsUseCase;
    private final CompareProductionUseCase compareProductionUseCase;
    private final FindProductionExecutionByIdUseCase findProductionExecutionByIdUseCase;
    private final DeleteProductionExecutionUseCase deleteProductionExecutionUseCase;

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

    @PreAuthorize("hasRole('PRODUCER')")
    @PutMapping("/production-plans/{planId}")
    public ResponseEntity<ProductionPlanResponseDTO> updateProductionPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody ProductionPlanUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionPlan plan = updateProductionPlanUseCase.update(planId, dto);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/producers/{producerId}/production-plans")
    public ResponseEntity<List<ProductionPlanResponseDTO>> listProductionPlansByProducer(
            @PathVariable UUID producerId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        List<ProductionPlan> plans = listProductionPlansUseCase.listByProducer(producerId);
        List<ProductionPlanResponseDTO> response = plans.stream()
                .map(Mapper::toProductionPlanResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/production-plans/{planId}")
    public ResponseEntity<ProductionPlanResponseDTO> findProductionPlanById(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionPlan plan = findProductionPlanByIdUseCase.findById(planId);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @DeleteMapping("/production-plans/{planId}")
    public ResponseEntity<Void> deleteProductionPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteProductionPlanUseCase.delete(planId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @PutMapping("/production-executions/{executionId}")
    public ResponseEntity<ProductionExecutionResponseDTO> updateProductionExecution(
            @PathVariable UUID executionId,
            @Valid @RequestBody ProductionExecutionUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionExecution execution = updateProductionExecutionUseCase.update(executionId, dto);
        ProductionExecutionResponseDTO response = Mapper.toProductionExecutionResponseDTO(execution);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/production-plans/{planId}/executions")
    public ResponseEntity<List<ProductionExecutionResponseDTO>> listProductionExecutions(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        List<ProductionExecution> executions = listProductionExecutionsUseCase.listByProductionPlan(planId);
        List<ProductionExecutionResponseDTO> response = executions.stream()
                .map(Mapper::toProductionExecutionResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/production-plans/{planId}/comparison")
    public ResponseEntity<ProductionComparisonDTO> compareProduction(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionComparisonDTO response = compareProductionUseCase.compare(planId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/production-executions/{executionId}")
    public ResponseEntity<ProductionExecutionResponseDTO> findProductionExecutionById(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        ProductionExecution execution = findProductionExecutionByIdUseCase.findById(executionId);
        ProductionExecutionResponseDTO response = Mapper.toProductionExecutionResponseDTO(execution);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PRODUCER')")
    @DeleteMapping("/production-executions/{executionId}")
    public ResponseEntity<Void> deleteProductionExecution(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteProductionExecutionUseCase.delete(executionId);
        return ResponseEntity.noContent().build();
    }
}
