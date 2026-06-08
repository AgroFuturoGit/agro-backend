package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.harvest.HarvestResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.harvest.HarvestRegisterUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Harvest;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/harvests")
public class HarvestController {

    private final HarvestRegisterUseCase harvestRegisterUseCase;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    @PostMapping("/register")
    public ResponseEntity<HarvestResponseDTO> register(@Valid @RequestBody HarvestRegisterDTO harvestRegisterDTO, @AuthenticationPrincipal UserDetailsImpl userDetails){
        User loggedUser = userRepository.findById(userDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Harvest harvest = Mapper.toHarvest(harvestRegisterDTO);

        Harvest newHarvest = harvestRegisterUseCase.execute(harvest, loggedUser);

        HarvestResponseDTO harvestResponseDTO = Mapper.toHarvestResponseDTO(newHarvest);

        return ResponseEntity.status(HttpStatus.CREATED).body(harvestResponseDTO);
    }
}
