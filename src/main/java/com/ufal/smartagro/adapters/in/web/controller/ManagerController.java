package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.manager.ManagerResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.manager.FindManagerByUserUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/managers")
public class ManagerController {

    private final FindManagerByUserUseCase findManagerByUserUseCase;

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/me")
    public ResponseEntity<ManagerResponseDTO> findMyManagerData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Manager manager = findManagerByUserUseCase.findByUserId(loggedUserDetails.getId());
        ManagerResponseDTO response = Mapper.toManagerResponseDTO(manager);
        return ResponseEntity.ok(response);
    }
}
