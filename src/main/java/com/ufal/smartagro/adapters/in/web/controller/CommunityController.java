package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityUpdateDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.community.FindAllCommunitiesUseCase;
import com.ufal.smartagro.application.service.community.FindCommunityByIdUseCase;
import com.ufal.smartagro.application.service.community.UpdateCommunityUseCase;
import com.ufal.smartagro.application.service.community.DeleteCommunityUseCase;
import com.ufal.smartagro.application.service.farmer.RegisterFarmerUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
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
@RequestMapping("/communities")
public class CommunityController {

    private final RegisterFarmerUseCase registerFarmerUseCase;
    private final FindCommunityByIdUseCase findCommunityByIdUseCase;
    private final FindAllCommunitiesUseCase findAllCommunitiesUseCase;
    private final UpdateCommunityUseCase updateCommunityUseCase;
    private final DeleteCommunityUseCase deleteCommunityUseCase;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}/farmers")
    public ResponseEntity<FarmerResponseDTO> registerFarmer(
            @PathVariable UUID id,
            @Valid @RequestBody FarmerRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Farmer farmer = registerFarmerUseCase.register(id, dto, loggedUser);
        FarmerResponseDTO response = Mapper.toFarmerResponseDTO(farmer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CommunityResponseDTO>> findAllCommunities(
            @RequestParam(required = false) UUID orgId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        java.util.List<Community> communities = findAllCommunitiesUseCase.findAll(orgId);
        java.util.List<CommunityResponseDTO> response = communities.stream()
                .map(Mapper::toCommunityResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CommunityResponseDTO> findCommunityById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Community community = findCommunityByIdUseCase.findById(id);
        CommunityResponseDTO response = Mapper.toCommunityResponseDTO(community);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CommunityResponseDTO> updateCommunity(
            @PathVariable UUID id,
            @Valid @RequestBody CommunityUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Community community = updateCommunityUseCase.update(id, dto);
        CommunityResponseDTO response = Mapper.toCommunityResponseDTO(community);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommunity(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteCommunityUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
