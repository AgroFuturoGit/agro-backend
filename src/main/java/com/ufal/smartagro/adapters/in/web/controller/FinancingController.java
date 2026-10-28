package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.financing.FinancingUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.farmer.financing.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
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

@Tag(name = "Financiamentos", description = "Gerenciamento de financiamentos e crédito rural do agricultor")
@RequiredArgsConstructor
@RestController
@RequestMapping
public class FinancingController {

    private final RegisterFinancingUseCase registerFinancingUseCase;
    private final FindAllFinancingsByFarmerUseCase findAllFinancingsByFarmerUseCase;
    private final FindFinancingByIdUseCase findFinancingByIdUseCase;
    private final UpdateFinancingUseCase updateFinancingUseCase;
    private final DeleteFinancingUseCase deleteFinancingUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Cadastrar financiamento", description = "Adiciona um financiamento ao agricultor.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Financiamento cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PostMapping("/farmers/{farmerId}/financings")
    public ResponseEntity<FinancingResponseDTO> register(
            @PathVariable UUID farmerId,
            @Valid @RequestBody FinancingRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Financing saved = registerFinancingUseCase.register(farmerId, dto, loggedUser);
        URI location = URI.create("/financings/" + saved.getId());
        return ResponseEntity.created(location).body(Mapper.toFinancingResponseDTO(saved));
    }

    @Operation(summary = "Listar financiamentos", description = "Lista os financiamentos de um agricultor.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/farmers/{farmerId}/financings")
    public ResponseEntity<List<FinancingResponseDTO>> findAll(
            @PathVariable UUID farmerId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<Financing> list = findAllFinancingsByFarmerUseCase.findAll(farmerId, loggedUser);
        return ResponseEntity.ok(list.stream().map(Mapper::toFinancingResponseDTO).toList());
    }

    @Operation(summary = "Buscar financiamento por ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Financiamento encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Financiamento não encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/financings/{id}")
    public ResponseEntity<FinancingResponseDTO> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Financing item = findFinancingByIdUseCase.findById(id, loggedUser);
        return ResponseEntity.ok(Mapper.toFinancingResponseDTO(item));
    }

    @Operation(summary = "Atualizar financiamento", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Financiamento atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Financiamento não encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PutMapping("/financings/{id}")
    public ResponseEntity<FinancingResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody FinancingUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Financing updated = updateFinancingUseCase.update(id, dto, loggedUser);
        return ResponseEntity.ok(Mapper.toFinancingResponseDTO(updated));
    }

    @Operation(summary = "Excluir financiamento (soft-delete)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Financiamento excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Financiamento não encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @DeleteMapping("/financings/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        deleteFinancingUseCase.delete(id, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
