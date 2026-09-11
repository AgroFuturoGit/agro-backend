package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.community.RegisterCommunityUseCase;
import com.ufal.smartagro.application.service.manager.RegisterManagerUseCase;
import com.ufal.smartagro.application.service.organization.FindAllOrganizationsUseCase;
import com.ufal.smartagro.application.service.organization.FindOrganizationByIdUseCase;
import com.ufal.smartagro.application.service.organization.RegisterOrganizationUseCase;
import com.ufal.smartagro.application.service.organization.UpdateOrganizationUseCase;
import com.ufal.smartagro.application.service.organization.DeleteOrganizationUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
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

@Tag(name = "Organizações", description = "Gerenciamento de cooperativas e organizações de produtores")
@RequiredArgsConstructor
@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    private final RegisterOrganizationUseCase registerOrganizationUseCase;
    private final RegisterManagerUseCase registerManagerUseCase;
    private final RegisterCommunityUseCase registerCommunityUseCase;
    private final FindOrganizationByIdUseCase findOrganizationByIdUseCase;
    private final FindAllOrganizationsUseCase findAllOrganizationsUseCase;
    private final UpdateOrganizationUseCase updateOrganizationUseCase;
    private final DeleteOrganizationUseCase deleteOrganizationUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Cadastrar organização", description = "Cria uma nova organização/cooperativa no sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organização cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> registerOrganization(
            @Valid @RequestBody OrganizationRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Organization organization = registerOrganizationUseCase.register(dto, loggedUser);
        OrganizationResponseDTO response = Mapper.toOrganizationResponseDTO(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cadastrar gestor na organização", description = "Cria e vincula um gestor a uma organização existente.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Gestor cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/managers")
    public ResponseEntity<ManagerResponseDTO> registerManager(
            @PathVariable UUID id,
            @Valid @RequestBody ManagerRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Manager manager = registerManagerUseCase.register(id, dto, loggedUser);
        ManagerResponseDTO response = Mapper.toManagerResponseDTO(manager);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cadastrar comunidade na organização", description = "Cria e vincula uma nova comunidade agrícola a uma organização.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comunidade cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{orgId}/communities")
    public ResponseEntity<CommunityResponseDTO> registerCommunity(
            @PathVariable UUID orgId,
            @Valid @RequestBody CommunityRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Community community = registerCommunityUseCase.register(orgId, dto, loggedUser);
        CommunityResponseDTO response = Mapper.toCommunityResponseDTO(community);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar todas as organizações", description = "Retorna a lista com todas as organizações cadastradas.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> findAllOrganizations(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        java.util.List<Organization> organizations = findAllOrganizationsUseCase.findAll();
        java.util.List<OrganizationResponseDTO> response = organizations.stream()
                .map(Mapper::toOrganizationResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar organização por ID", description = "Retorna os detalhes de uma organização específica.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização encontrada"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> findOrganizationById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Organization organization = findOrganizationByIdUseCase.findById(id);
        OrganizationResponseDTO response = Mapper.toOrganizationResponseDTO(organization);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar organização", description = "Atualiza os dados cadastrais de uma organização.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(
            @PathVariable UUID id,
            @Valid @RequestBody com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Organization organization = updateOrganizationUseCase.update(id, dto);
        OrganizationResponseDTO response = Mapper.toOrganizationResponseDTO(organization);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir organização", description = "Remove uma organização do sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organização excluída com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteOrganizationUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
