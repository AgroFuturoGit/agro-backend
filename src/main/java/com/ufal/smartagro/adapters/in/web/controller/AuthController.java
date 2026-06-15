package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginDTO;
import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.auth.MyInfoResponseDTO;
import com.ufal.smartagro.application.service.auth.LoginUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(loginUseCase.login(loginDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<MyInfoResponseDTO> me(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.ufal.smartagro.config.security.details.UserDetailsImpl userDetails) {
        return ResponseEntity.ok(new MyInfoResponseDTO(
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getUsername(),
                userDetails.getUser().getRole()
        ));
    }
}
