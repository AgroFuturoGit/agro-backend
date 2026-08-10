package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.technician.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
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
@RequestMapping("/technicians")
public class TechnicianController {

    private final RegisterTechnicianUseCase registerTechnicianUseCase;
    private final FindTechnicianByIdUseCase findTechnicianByIdUseCase;
    private final FindTechnicianByUserUseCase findTechnicianByUserUseCase;
    private final FindAllTechniciansUseCase findAllTechniciansUseCase;
    private final UpdateTechnicianUseCase updateTechnicianUseCase;
    private final DeleteTechnicianUseCase deleteTechnicianUseCase;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TechnicianResponseDTO> registerTechnician(
            @Valid @RequestBody TechnicianRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Technician technician = registerTechnicianUseCase.register(dto, loggedUser);
        TechnicianResponseDTO response = Mapper.toTechnicianResponseDTO(technician);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/me")
    public ResponseEntity<TechnicianResponseDTO> findMyTechnicianData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Technician technician = findTechnicianByUserUseCase.findByUserId(loggedUserDetails.getId());
        TechnicianResponseDTO response = Mapper.toTechnicianResponseDTO(technician);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<List<TechnicianResponseDTO>> findAllTechnicians(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        List<Technician> technicians = findAllTechniciansUseCase.findAll(loggedUser);
        List<TechnicianResponseDTO> response = technicians.stream()
                .map(Mapper::toTechnicianResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponseDTO> findTechnicianById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Technician technician = findTechnicianByIdUseCase.findById(id, loggedUser);
        TechnicianResponseDTO response = Mapper.toTechnicianResponseDTO(technician);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<TechnicianResponseDTO> updateTechnician(
            @PathVariable UUID id,
            @Valid @RequestBody TechnicianUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        Technician technician = updateTechnicianUseCase.update(id, dto, loggedUser);
        TechnicianResponseDTO response = Mapper.toTechnicianResponseDTO(technician);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTechnician(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        deleteTechnicianUseCase.delete(id, loggedUser);
        return ResponseEntity.noContent().build();
    }
}
