package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.auth.LoginDTO;
import com.ufal.smartagro.adapters.in.web.dto.auth.LoginResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.auth.LoginUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.ratelimit.LoginRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Tag(name = "Autenticação", description = "Endpoints de login e dados do usuário autenticado")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final LoginRateLimiter rateLimiter;

    @Operation(summary = "Autenticar usuário", description = "Recebe credenciais (e-mail e senha) e retorna um token JWT válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais incorretas"),
            @ApiResponse(responseCode = "429", description = "Muitas tentativas de login")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String clientKey = resolveClientKey(request, loginDTO.email());
        rateLimiter.checkAndIncrement(clientKey);

        var loginResult = loginUseCase.login(loginDTO);
        return ResponseEntity.ok(Mapper.toLoginResponseDTO(loginResult.token(), loginResult.user()));
    }

    @Operation(
            summary = "Dados do usuário logado",
            description = "Retorna as informações do usuário autenticado a partir do token JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(Mapper.toUserResponseDTO(userDetails.getUser()));
    }

    private String resolveClientKey(HttpServletRequest request, String email) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        String normalizedEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
        return (ip != null ? ip : "unknown") + ":" + normalizedEmail;
    }
}
