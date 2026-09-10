package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.role.AssignRoleDTO;
import com.ufal.smartagro.adapters.in.web.dto.role.RoleResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.application.service.role.AssignRoleUseCase;
import com.ufal.smartagro.application.service.role.ListRolesUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Papéis (Roles)", description = "Gerenciamento e atribuição de papéis e permissões")
@RequiredArgsConstructor
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final ListRolesUseCase listRolesUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Listar todos os papéis", description = "Retorna a lista de papéis (roles) disponíveis no sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> listAll() {
        return ResponseEntity.ok(listRolesUseCase.listAll());
    }

    @Operation(summary = "Atribuir papel a usuário", description = "Associa um papel específico (role) a um usuário.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Papel atribuído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: role inexistente)"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> assignRole(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {
        
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        return ResponseEntity.ok(assignRoleUseCase.assignRole(userId, dto.role(), loggedUser));
    }
}
