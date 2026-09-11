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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Produção Agrícola", description = "Planejamento e execução de safras e acompanhamento produtivo")
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

    @Operation(summary = "Criar plano de produção", description = "Cria um plano de produção agrícola para determinado agricultor.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plano de produção criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agricultor não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PostMapping("/farmers/{farmerId}/production-plans")
    public ResponseEntity<ProductionPlanResponseDTO> createProductionPlan(
            @PathVariable UUID farmerId,
            @Valid @RequestBody ProductionPlanRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionPlan plan = createProductionPlanUseCase.create(farmerId, dto, loggedUser);
        ProductionPlanResponseDTO response = Mapper.toProductionPlanResponseDTO(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Registrar execução de produção", description = "Registra um evento de execução referente a um plano de produção.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Execução registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano de produção não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
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

    @Operation(summary = "Atualizar plano de produção", description = "Atualiza os dados de um plano de produção existente.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano de produção atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano de produção não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
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

    @Operation(summary = "Listar planos de produção do agricultor", description = "Retorna todos os planos de produção vinculados a um agricultor.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agricultor não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
    @GetMapping("/farmers/{farmerId}/production-plans")
    public ResponseEntity<List<ProductionPlanResponseDTO>> listProductionPlansByFarmer(
            @PathVariable UUID farmerId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<ProductionPlan> plans = listProductionPlansUseCase.listByFarmer(farmerId, loggedUser);
        List<ProductionPlanResponseDTO> response = plans.stream()
                .map(Mapper::toProductionPlanResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar plano de produção por ID", description = "Recupera os detalhes de um plano de produção específico.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plano de produção encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano de produção não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
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

    @Operation(summary = "Excluir plano de produção", description = "Remove um plano de produção do sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plano de produção excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano de produção não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @DeleteMapping("/production-plans/{planId}")
    public ResponseEntity<Void> deleteProductionPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        deleteProductionPlanUseCase.delete(planId, loggedUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar execução de produção", description = "Atualiza os dados de uma execução de produção já registrada.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Execução atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Execução não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
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

    @Operation(summary = "Listar execuções de um plano", description = "Retorna o histórico de execuções registradas para determinado plano.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
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

    @Operation(summary = "Comparar planejado vs executado", description = "Gera um comparativo de métricas e produtividade entre o plano e as execuções realizadas.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comparativo gerado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Plano não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
    @GetMapping("/production-plans/{planId}/comparison")
    public ResponseEntity<ProductionComparisonDTO> compareProduction(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        ProductionComparisonDTO response = compareProductionUseCase.compare(planId, loggedUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar execução de produção por ID", description = "Recupera os detalhes de uma execução específica.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Execução encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Execução não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
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

    @Operation(summary = "Excluir execução de produção", description = "Remove um registro de execução de produção.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Execução excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Execução não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @DeleteMapping("/production-executions/{executionId}")
    public ResponseEntity<Void> deleteProductionExecution(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        deleteProductionExecutionUseCase.delete(executionId, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
