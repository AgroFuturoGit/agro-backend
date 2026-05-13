package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserUpdateDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRegisterUseCase userRegisterUseCase;
    private final UserDeleteUseCase userDeleteUseCase;
    private final FindUserByIdUseCase findUserByIdUseCase;
    private final UserUpdateUseCase userUpdateUseCase;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = userRegisterUseCase.register(dto, loggedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        UserResponseDTO response = findUserByIdUseCase.findById(id, loggedUser);
        return ResponseEntity.ok(response);
    }
}
