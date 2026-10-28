package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.production.occurrence.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Ocorrências de Campo", description = "Gerenciamento de eventos climáticos, pragas e doenças registrados no apontamento")
@RequiredArgsConstructor
@RestController
@RequestMapping
public class FieldOccurrenceController {

    private final RegisterFieldOccurrenceUseCase registerFieldOccurrenceUseCase;
    private final FindAllOccurrencesByExecutionUseCase findAllOccurrencesByExecutionUseCase;
    private final FindFieldOccurrenceByIdUseCase findFieldOccurrenceByIdUseCase;
    private final UpdateFieldOccurrenceUseCase updateFieldOccurrenceUseCase;
    private final DeleteFieldOccurrenceUseCase deleteFieldOccurrenceUseCase;

    @Operation(summary = "Registrar ocorrência de campo", description = "Adiciona uma ocorrência (clima, praga, doença) a um apontamento. Suporta deduplicação idempotente via clientId no corpo ou header Idempotency-Key com header Location.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ocorrência registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PostMapping("/production-executions/{executionId}/occurrences")
    public ResponseEntity<FieldOccurrenceResponseDTO> register(
            @PathVariable UUID executionId,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
            @Valid @RequestBody FieldOccurrenceRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loggedUserDetails.toDomainUser();

        if (dto.clientId() == null && idempotencyKey != null) {
            dto = dto.withClientId(idempotencyKey);
        }

        FieldOccurrence saved = registerFieldOccurrenceUseCase.register(executionId, dto, loggedUser);
        FieldOccurrenceResponseDTO response = Mapper.toFieldOccurrenceResponseDTO(saved);
        URI location = URI.create("/occurrences/" + saved.getId());
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Listar ocorrências da execução", description = "Lista as ocorrências registradas em um apontamento.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/production-executions/{executionId}/occurrences")
    public ResponseEntity<List<FieldOccurrenceResponseDTO>> findAll(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loggedUserDetails.toDomainUser();

        List<FieldOccurrence> list = findAllOccurrencesByExecutionUseCase.findAll(executionId, loggedUser);
        return ResponseEntity.ok(list.stream().map(Mapper::toFieldOccurrenceResponseDTO).toList());
    }

    @Operation(summary = "Buscar ocorrência por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocorrência encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ocorrência não encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/occurrences/{id}")
    public ResponseEntity<FieldOccurrenceResponseDTO> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loggedUserDetails.toDomainUser();

        FieldOccurrence item = findFieldOccurrenceByIdUseCase.findById(id, loggedUser);
        return ResponseEntity.ok(Mapper.toFieldOccurrenceResponseDTO(item));
    }

    @Operation(summary = "Atualizar ocorrência de campo", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ocorrência atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Ocorrência não encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PutMapping("/occurrences/{id}")
    public ResponseEntity<FieldOccurrenceResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody FieldOccurrenceUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loggedUserDetails.toDomainUser();

        FieldOccurrence updated = updateFieldOccurrenceUseCase.update(id, dto, loggedUser);
        return ResponseEntity.ok(Mapper.toFieldOccurrenceResponseDTO(updated));
    }

    @Operation(summary = "Excluir ocorrência (soft-delete)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ocorrência excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ocorrência não encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @DeleteMapping("/occurrences/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loggedUserDetails.toDomainUser();

        deleteFieldOccurrenceUseCase.delete(id, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
