package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerResponseDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.community.FindAllCommunitiesUseCase;
import com.ufal.smartagro.application.service.community.FindCommunityByIdUseCase;
import com.ufal.smartagro.application.service.community.RegisterCommunityUseCase;
import com.ufal.smartagro.application.service.producer.RegisterProducerUseCase;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class CommunityController {

    private final RegisterCommunityUseCase registerCommunityUseCase;
    private final RegisterProducerUseCase registerProducerUseCase;
    private final FindCommunityByIdUseCase findCommunityByIdUseCase;
    private final FindAllCommunitiesUseCase findAllCommunitiesUseCase;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/organizations/{orgId}/communities")
    public ResponseEntity<CommunityResponseDTO> registerCommunity(
            @PathVariable UUID orgId,
            @Valid @RequestBody CommunityRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Community community = registerCommunityUseCase.register(orgId, dto, loggedUser);
        CommunityResponseDTO response = Mapper.toCommunityResponseDTO(community);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/communities/{id}/producers")
    public ResponseEntity<ProducerResponseDTO> registerProducer(
            @PathVariable UUID id,
            @Valid @RequestBody ProducerRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Producer producer = registerProducerUseCase.register(id, dto, loggedUser);
        ProducerResponseDTO response = Mapper.toProducerResponseDTO(producer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<java.util.List<CommunityResponseDTO>> findAllCommunities(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        java.util.List<Community> communities = findAllCommunitiesUseCase.findAll();
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
}
