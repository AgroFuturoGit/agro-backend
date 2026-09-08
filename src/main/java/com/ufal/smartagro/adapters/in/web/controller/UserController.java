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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PatchMapping
    public ResponseEntity<UserResponseDTO> update(
            @Valid @RequestBody UserUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = userUpdateUseCase.update(dto, loggedUser);
        return ResponseEntity.ok(response);
    }

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
