package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.*;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.production.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
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
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'PRODUCER')")
    @PostMapping("/producers/{producerId}/production-plans")
    public ResponseEntity<ProductionPlanResponseDTO> createProductionPlan(
            @PathVariable UUID producerId,
            @Valid @RequestBody ProductionPlanRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionPlan plan = createProductionPlanUseCase.create(producerId, dto, loggedUser);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'PRODUCER')")
    @PostMapping("/production-plans/{planId}/executions")
    public ResponseEntity<ProductionExecutionResponseDTO> createProductionExecution(
            @PathVariable UUID planId,
            @Valid @RequestBody ProductionExecutionRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionExecution execution = createProductionExecutionUseCase.create(planId, dto, loggedUser);
        ProductionExecutionResponseDTO response = Mapper.toProductionExecutionResponseDTO(execution);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'PRODUCER')")
    @PutMapping("/production-plans/{planId}")
    public ResponseEntity<ProductionPlanResponseDTO> updateProductionPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody ProductionPlanUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionPlan plan = updateProductionPlanUseCase.update(planId, dto, loggedUser);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'PRODUCER')")
    @GetMapping("/producers/{producerId}/production-plans")
    public ResponseEntity<List<ProductionPlanResponseDTO>> listProductionPlansByProducer(
            @PathVariable UUID producerId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<ProductionPlan> plans = listProductionPlansUseCase.listByProducer(producerId, loggedUser);
        List<ProductionPlanResponseDTO> response = plans.stream()
                .map(Mapper::toProductionPlanResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'PRODUCER')")
    @GetMapping("/production-plans/{planId}")
    public ResponseEntity<ProductionPlanResponseDTO> findProductionPlanById(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionPlan plan = findProductionPlanByIdUseCase.findById(planId, loggedUser);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @DeleteMapping("/production-plans/{planId}")
    public ResponseEntity<Void> deleteProductionPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteProductionPlanUseCase.delete(planId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'PRODUCER')")
    @PutMapping("/production-executions/{executionId}")
    public ResponseEntity<ProductionExecutionResponseDTO> updateProductionExecution(
            @PathVariable UUID executionId,
            @Valid @RequestBody ProductionExecutionUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionExecution execution = updateProductionExecutionUseCase.update(executionId, dto, loggedUser);
        ProductionExecutionResponseDTO response = Mapper.toProductionExecutionResponseDTO(execution);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'PRODUCER')")
    @GetMapping("/production-plans/{planId}/executions")
    public ResponseEntity<List<ProductionExecutionResponseDTO>> listProductionExecutions(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<ProductionExecution> executions = listProductionExecutionsUseCase.listByProductionPlan(planId, loggedUser);
        List<ProductionExecutionResponseDTO> response = executions.stream()
                .map(Mapper::toProductionExecutionResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'PRODUCER')")
    @GetMapping("/production-plans/{planId}/comparison")
    public ResponseEntity<ProductionComparisonDTO> compareProduction(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionComparisonDTO response = compareProductionUseCase.compare(planId, loggedUser);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'PRODUCER')")
    @GetMapping("/production-executions/{executionId}")
    public ResponseEntity<ProductionExecutionResponseDTO> findProductionExecutionById(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionExecution execution = findProductionExecutionByIdUseCase.findById(executionId, loggedUser);
        ProductionExecutionResponseDTO response = Mapper.toProductionExecutionResponseDTO(execution);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @DeleteMapping("/production-executions/{executionId}")
    public ResponseEntity<Void> deleteProductionExecution(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteProductionExecutionUseCase.delete(executionId);
        return ResponseEntity.noContent().build();
    }
}
