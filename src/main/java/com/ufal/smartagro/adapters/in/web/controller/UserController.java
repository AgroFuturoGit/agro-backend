package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.user.AdminUserUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserUpdateDTO;
import com.ufal.smartagro.application.service.user.AdminUserUpdateUseCase;
import com.ufal.smartagro.application.service.user.FindAllUsersUseCase;
import com.ufal.smartagro.application.service.user.UserDeleteUseCase;
import com.ufal.smartagro.application.service.user.FindUserByIdUseCase;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.application.service.user.UserUpdateUseCase;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Usuários", description = "Gerenciamento de usuários da plataforma")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRegisterUseCase userRegisterUseCase;
    private final UserDeleteUseCase userDeleteUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final UserUpdateUseCase userUpdateUseCase;
    private final AdminUserUpdateUseCase adminUserUpdateUseCase;
    private final FindAllUsersUseCase findAllUsersUseCase;
    private final UserRepository userRepository;

    @Operation(summary = "Registrar usuário (deprecado)", description = "Cria um novo usuário na base de dados. Deprecado em favor de fluxos específicos.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @Deprecated(since = "2.0", forRemoval = true)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        if (dto.role() == com.ufal.smartagro.domain.model.enums.Role.MANAGER || dto.role() == com.ufal.smartagro.domain.model.enums.Role.FARMER) {
            throw new IllegalArgumentException("Para criar gestores ou agricultores, utilize as rotas específicas de Organização e Comunidade.");
        }

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = userRegisterUseCase.register(dto, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Excluir usuário", description = "Remove logicamente ou fisicamente um usuário do sistema pelo ID.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        userDeleteUseCase.delete(id, loggedUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar dados próprios", description = "Permite que o usuário autenticado atualize seus próprios dados.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    @PatchMapping
    public ResponseEntity<UserResponseDTO> update(
            @Valid @RequestBody UserUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = userUpdateUseCase.update(dto, loggedUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Atualizar usuário (admin)", description = "Permite que administradores atualizem dados de outros usuários.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> adminUpdate(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUserUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = adminUserUpdateUseCase.update(id, dto, loggedUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar usuário por ID", description = "Recupera os detalhes de um usuário específico.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = findUserByIdUseCase.findById(id, loggedUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista de todos os usuários cadastrados no sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<UserResponseDTO> response = findAllUsersUseCase.findAll(loggedUser);
        return ResponseEntity.ok(response);
    }
}
