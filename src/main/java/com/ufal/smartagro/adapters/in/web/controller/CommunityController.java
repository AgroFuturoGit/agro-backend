package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.community.FindAllCommunitiesUseCase;
import com.ufal.smartagro.application.service.community.FindCommunityByIdUseCase;
import com.ufal.smartagro.application.service.community.UpdateCommunityUseCase;
import com.ufal.smartagro.application.service.community.DeleteCommunityUseCase;
import com.ufal.smartagro.application.service.farmer.RegisterFarmerUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
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

@Tag(name = "Comunidades", description = "Gerenciamento de comunidades agrícolas")
@RequiredArgsConstructor
@RestController
@RequestMapping("/communities")
public class CommunityController {

    private final RegisterFarmerUseCase registerFarmerUseCase;
    private final FindCommunityByIdUseCase findCommunityByIdUseCase;
    private final FindAllCommunitiesUseCase findAllCommunitiesUseCase;
    private final UpdateCommunityUseCase updateCommunityUseCase;
    private final DeleteCommunityUseCase deleteCommunityUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Cadastrar agricultor na comunidade", description = "Registra um novo agricultor e o vincula à comunidade informada.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agricultor cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Comunidade não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}/farmers")
    public ResponseEntity<FarmerResponseDTO> registerFarmer(
            @PathVariable UUID id,
            @Valid @RequestBody FarmerRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Farmer farmer = registerFarmerUseCase.register(id, dto, loggedUser);
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar comunidades", description = "Retorna todas as comunidades, com opção de filtro por ID de organização (orgId).", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CommunityResponseDTO>> findAllCommunities(
            @RequestParam(required = false) UUID orgId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        java.util.List<Community> communities = findAllCommunitiesUseCase.findAll(orgId);
        java.util.List<CommunityResponseDTO> response = communities.stream()
                .map(Mapper::toCommunityResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar comunidade por ID", description = "Retorna os detalhes de uma comunidade específica.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comunidade encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Comunidade não encontrada")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponseDTO> findCommunityById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Community community = findCommunityByIdUseCase.findById(id);
        CommunityResponseDTO response = Mapper.toCommunityResponseDTO(community);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar comunidade", description = "Atualiza as informações de uma comunidade.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comunidade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Comunidade não encontrada")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CommunityResponseDTO> updateCommunity(
            @PathVariable UUID id,
            @Valid @RequestBody CommunityUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Community community = updateCommunityUseCase.update(id, dto);
        CommunityResponseDTO response = Mapper.toCommunityResponseDTO(community);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir comunidade", description = "Remove uma comunidade do sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comunidade excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Comunidade não encontrada")
    })
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteCommunityUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
