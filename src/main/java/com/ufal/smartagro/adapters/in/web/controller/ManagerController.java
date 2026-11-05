package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.manager.DeleteManagerUseCase;
import com.ufal.smartagro.application.service.manager.FindAllManagersUseCase;
import com.ufal.smartagro.application.service.manager.FindManagerByIdUseCase;
import com.ufal.smartagro.application.service.manager.FindManagerByUserUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Gestores", description = "Operações e dados do perfil de gestores")
@RequiredArgsConstructor
@RestController
@RequestMapping("/managers")
public class ManagerController {

    private final FindManagerByUserUseCase findManagerByUserUseCase;
    private final FindAllManagersUseCase findAllManagersUseCase;
    private final FindManagerByIdUseCase findManagerByIdUseCase;
    private final DeleteManagerUseCase deleteManagerUseCase;

    @Operation(summary = "Buscar dados do gestor autenticado", description = "Retorna os dados cadastrais e vínculos do gestor logado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do gestor retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil MANAGER)"),
            @ApiResponse(responseCode = "404", description = "Gestor não encontrado")
    })
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/me")
    public ResponseEntity<ManagerResponseDTO> findMyManagerData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        if (loggedUserDetails == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        Manager manager = findManagerByUserUseCase.findByUserId(loggedUserDetails.getId());
        ManagerResponseDTO response = Mapper.toManagerResponseDTO(manager);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar gestores", description = "Retorna a lista de gestores ativos do sistema, opcionalmente filtrada por organização.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de gestores retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil ADMIN)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ManagerResponseDTO>> findAll(
            @RequestParam(required = false) UUID organizationId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = resolveLoggedUser(loggedUserDetails);
        List<ManagerResponseDTO> response = findAllManagersUseCase.findAll(organizationId, loggedUser).stream()
                .map(Mapper::toManagerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar gestor por ID", description = "Retorna os dados de um gestor específico pelo ID do perfil ou do usuário vinculado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gestor encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Gestor não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ManagerResponseDTO> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = resolveLoggedUser(loggedUserDetails);
        Manager manager = findManagerByIdUseCase.findById(id, loggedUser);
        return ResponseEntity.ok(Mapper.toManagerResponseDTO(manager));
    }

    @Operation(summary = "Excluir gestor", description = "Realiza a exclusão lógica do perfil de gestor e da conta de usuário vinculada.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Gestor excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Gestor não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = resolveLoggedUser(loggedUserDetails);
        deleteManagerUseCase.delete(id, loggedUser);
        return ResponseEntity.noContent().build();
    }

    private User resolveLoggedUser(UserDetailsImpl loggedUserDetails) {
        if (loggedUserDetails == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }
        return loggedUserDetails.toDomainUser();
    }
}
