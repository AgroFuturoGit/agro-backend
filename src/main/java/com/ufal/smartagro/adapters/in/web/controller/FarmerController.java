package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.farmer.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.Farmer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/farmers")
public class FarmerController {

    private final FindFarmerByUserUseCase findFarmerByUserUseCase;
    private final FindFarmerByIdUseCase findFarmerByIdUseCase;
    private final FindAllFarmersUseCase findAllFarmersUseCase;
    private final UpdateFarmerUseCase updateFarmerUseCase;
    private final DeleteFarmerUseCase deleteFarmerUseCase;

    @PreAuthorize("hasRole('FARMER')")
    @GetMapping("/me")
    public ResponseEntity<FarmerResponseDTO> findMyFarmerData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Farmer farmer = findFarmerByUserUseCase.findByUserId(loggedUserDetails.getId());
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<FarmerResponseDTO>> findAllFarmers(
            @RequestParam(required = false) UUID communityId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        List<Farmer> farmers = findAllFarmersUseCase.findAll(communityId);
        List<FarmerResponseDTO> response = farmers.stream()
                .map(Mapper::toFarmerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<FarmerResponseDTO> findFarmerById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Farmer farmer = findFarmerByIdUseCase.findById(id);
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FarmerResponseDTO> updateFarmer(
            @PathVariable UUID id,
            @Valid @RequestBody FarmerUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Farmer farmer = updateFarmerUseCase.update(id, dto);
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarmer(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteFarmerUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
