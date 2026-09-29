package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.harvest.HarvestDeleteUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestFindAllUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestFindByIdUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestRegisterUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestUpdateUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
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

@Tag(name = "Colheitas (Harvests)", description = "Registro e acompanhamento de colheitas agrícolas")
@RequiredArgsConstructor
@RestController
@RequestMapping("/harvests")
public class HarvestController {

    private final HarvestRegisterUseCase harvestRegisterUseCase;
    private final HarvestUpdateUseCase harvestUpdateUseCase;
    private final HarvestFindAllUseCase harvestFindAllUseCase;
    private final HarvestFindByIdUseCase harvestFindByIdUseCase;
    private final HarvestDeleteUseCase harvestDeleteUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Registrar colheita", description = "Cadastra uma nova colheita realizada no sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Colheita registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping("/register")
    public ResponseEntity<HarvestResponseDTO> register(@Valid @RequestBody HarvestRegisterDTO harvestRegisterDTO, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User loggedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Harvest harvest = Mapper.toHarvest(harvestRegisterDTO);

        Harvest newHarvest = harvestRegisterUseCase.execute(harvest, loggedUser);

        HarvestResponseDTO harvestResponseDTO = Mapper.toHarvestResponseDTO(newHarvest);

        return ResponseEntity.status(HttpStatus.CREATED).body(harvestResponseDTO);
    }

    @Operation(summary = "Atualizar colheita", description = "Atualiza parcialmente as informações de uma colheita existente.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colheita atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Colheita não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PatchMapping("/{id}")
    public ResponseEntity<HarvestResponseDTO> update(@PathVariable UUID id,
                                                     @Valid @RequestBody HarvestUpdateDTO harvestUpdateDTO,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User loggedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Harvest harvestToUpdate = Mapper.toHarvest(harvestUpdateDTO);

        Harvest updatedHarvest = harvestUpdateUseCase.execute(id, harvestToUpdate, loggedUser);

        HarvestResponseDTO responseDTO = Mapper.toHarvestResponseDTO(updatedHarvest);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Buscar colheita por ID", description = "Recupera os detalhes de uma colheita específica.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colheita encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Colheita não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
    @GetMapping("/{id}")
    public ResponseEntity<HarvestResponseDTO> findById(@PathVariable UUID id,
                                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User loggedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Harvest harvest = harvestFindByIdUseCase.execute(id, loggedUser);

        HarvestResponseDTO responseDTO = Mapper.toHarvestResponseDTO(harvest);

        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Listar todas as colheitas", description = "Retorna a lista de todas as colheitas registradas.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'MANAGER', 'FARMER')")
    @GetMapping
    public ResponseEntity<List<HarvestResponseDTO>> findAll(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User loggedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<Harvest> harvests = harvestFindAllUseCase.execute(loggedUser);

        List<HarvestResponseDTO> responseDTOs = harvests.stream()
                .map(Mapper::toHarvestResponseDTO)
                .toList();

        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "Excluir colheita", description = "Remove um registro de colheita do sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Colheita excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Colheita não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User loggedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        harvestDeleteUseCase.execute(id, loggedUser);

        return ResponseEntity.noContent().build();
    }
}
