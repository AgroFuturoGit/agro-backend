package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.manager.FindManagerByUserUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.Manager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gestores", description = "Operações e dados do perfil de gestores")
@RequiredArgsConstructor
@RestController
@RequestMapping("/managers")
public class ManagerController {

    private final FindManagerByUserUseCase findManagerByUserUseCase;

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

        Manager manager = findManagerByUserUseCase.findByUserId(loggedUserDetails.getId());
        ManagerResponseDTO response = Mapper.toManagerResponseDTO(manager);
        return ResponseEntity.ok(response);
    }
}
