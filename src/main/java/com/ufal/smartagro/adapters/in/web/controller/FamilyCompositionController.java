package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.family.FamilyCompositionUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.farmer.family.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.FamilyComposition;
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

@Tag(name = "Composição Familiar", description = "Gerenciamento da composição familiar do agricultor")
@RequiredArgsConstructor
@RestController
@RequestMapping
public class FamilyCompositionController {

    private final RegisterFamilyCompositionUseCase registerFamilyCompositionUseCase;
    private final FindAllFamilyCompositionsByFarmerUseCase findAllFamilyCompositionsByFarmerUseCase;
    private final FindFamilyCompositionByIdUseCase findFamilyCompositionByIdUseCase;
    private final UpdateFamilyCompositionUseCase updateFamilyCompositionUseCase;
    private final DeleteFamilyCompositionUseCase deleteFamilyCompositionUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Cadastrar membro familiar", description = "Adiciona um membro à composição familiar do agricultor.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membro cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PostMapping("/farmers/{farmerId}/family-compositions")
    public ResponseEntity<FamilyCompositionResponseDTO> register(
            @PathVariable UUID farmerId,
            @Valid @RequestBody FamilyCompositionRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        FamilyComposition saved = registerFamilyCompositionUseCase.register(farmerId, dto, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Mapper.toFamilyCompositionResponseDTO(saved));
    }

    @Operation(summary = "Listar composição familiar", description = "Lista os membros familiares de um agricultor.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/farmers/{farmerId}/family-compositions")
    public ResponseEntity<List<FamilyCompositionResponseDTO>> findAll(
            @PathVariable UUID farmerId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<FamilyComposition> list = findAllFamilyCompositionsByFarmerUseCase.findAll(farmerId, loggedUser);
        return ResponseEntity.ok(list.stream().map(Mapper::toFamilyCompositionResponseDTO).toList());
    }

    @Operation(summary = "Buscar membro familiar por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/family-compositions/{id}")
    public ResponseEntity<FamilyCompositionResponseDTO> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        FamilyComposition item = findFamilyCompositionByIdUseCase.findById(id, loggedUser);
        return ResponseEntity.ok(Mapper.toFamilyCompositionResponseDTO(item));
    }

    @Operation(summary = "Atualizar membro familiar", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PutMapping("/family-compositions/{id}")
    public ResponseEntity<FamilyCompositionResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody FamilyCompositionUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        FamilyComposition updated = updateFamilyCompositionUseCase.update(id, dto, loggedUser);
        return ResponseEntity.ok(Mapper.toFamilyCompositionResponseDTO(updated));
    }

    @Operation(summary = "Excluir membro familiar (soft-delete)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @DeleteMapping("/family-compositions/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        deleteFamilyCompositionUseCase.delete(id, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
