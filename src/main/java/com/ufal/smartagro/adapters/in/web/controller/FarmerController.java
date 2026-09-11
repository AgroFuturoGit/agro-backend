package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.farmer.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.Farmer;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Agricultores", description = "Operações e gerenciamento de agricultores familiares")
@RequiredArgsConstructor
@RestController
@RequestMapping("/farmers")
public class FarmerController {

    private final FindFarmerByUserUseCase findFarmerByUserUseCase;
    private final FindFarmerByIdUseCase findFarmerByIdUseCase;
    private final FindAllFarmersUseCase findAllFarmersUseCase;
    private final UpdateFarmerUseCase updateFarmerUseCase;
    private final DeleteFarmerUseCase deleteFarmerUseCase;

    @Operation(summary = "Buscar dados do agricultor autenticado", description = "Retorna os dados do agricultor logado a partir do token.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil FARMER)"),
            @ApiResponse(responseCode = "404", description = "Agricultor não encontrado")
    })
    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/me")
    public ResponseEntity<FarmerResponseDTO> findMyFarmerData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Farmer farmer = findFarmerByUserUseCase.findByUserId(loggedUserDetails.getId());
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agricultores", description = "Lista agricultores cadastrados, com opção de filtrar por comunidade.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<FarmerResponseDTO>> findAllFarmers(
            @RequestParam(required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        List<Farmer> farmers = findAllFarmersUseCase.findAll(communityId);
        List<FarmerResponseDTO> response = farmers.stream()
                .map(Mapper::toFarmerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar agricultor por ID", description = "Recupera os detalhes de um agricultor específico.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agricultor encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agricultor não encontrado")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FarmerResponseDTO> findFarmerById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Farmer farmer = findFarmerByIdUseCase.findById(id);
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar agricultor", description = "Atualiza os dados de um agricultor cadastrado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agricultor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agricultor não encontrado")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FarmerResponseDTO> updateFarmer(
            @PathVariable UUID id,
            @Valid @RequestBody FarmerUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Farmer farmer = updateFarmerUseCase.update(id, dto);
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir agricultor", description = "Remove um agricultor do sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agricultor excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Agricultor não encontrado")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarmer(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteFarmerUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
