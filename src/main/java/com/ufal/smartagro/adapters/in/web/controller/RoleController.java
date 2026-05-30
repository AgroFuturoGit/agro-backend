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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/roles")
public class RoleController {

    private final ListRolesUseCase listRolesUseCase;
    private final AssignRoleUseCase assignRoleUseCase;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> listAll() {
        return ResponseEntity.ok(listRolesUseCase.listAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {
        
        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        return ResponseEntity.ok(assignRoleUseCase.assignRole(userId, dto.role(), loggedUser));
    }
}
