package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.technician.*;
import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerResponseDTO;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
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
    private final AssignProducerToTechnicianUseCase assignProducerToTechnicianUseCase;
    private final RemoveProducerFromTechnicianUseCase removeProducerFromTechnicianUseCase;
    private final GetAssignedProducersUseCase getAssignedProducersUseCase;

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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('PRODUCER')")
    @PostMapping("/{technicianId}/assistances")
    public ResponseEntity<TechnicalAssistanceResponseDTO> assignAssistance(
            @PathVariable UUID technicianId,
            @Valid @RequestBody TechnicalAssistanceRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        TechnicalAssistance assistance = assignProducerToTechnicianUseCase.assign(dto, loggedUser);
        TechnicalAssistanceResponseDTO response = Mapper.toTechnicalAssistanceResponseDTO(assistance);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('PRODUCER') or hasRole('TECHNICIAN')")
    @DeleteMapping("/{technicianId}/assistances/{assistanceId}")
    public ResponseEntity<TechnicalAssistanceResponseDTO> removeAssistance(
            @PathVariable UUID technicianId,
            @PathVariable UUID assistanceId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        TechnicalAssistance assistance = removeProducerFromTechnicianUseCase.remove(assistanceId, loggedUser);
        TechnicalAssistanceResponseDTO response = Mapper.toTechnicalAssistanceResponseDTO(assistance);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/me/producers")
    public ResponseEntity<List<ProducerResponseDTO>> getMyProducers(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        Technician technician = findTechnicianByUserUseCase.findByUserId(loggedUserDetails.getId());
        List<Producer> producers = getAssignedProducersUseCase.getAssignedProducers(technician.getId(), loggedUser);
        List<ProducerResponseDTO> response = producers.stream()
                .map(Mapper::toProducerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}/producers")
    public ResponseEntity<List<ProducerResponseDTO>> getTechnicianProducers(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        List<Producer> producers = getAssignedProducersUseCase.getAssignedProducers(id, loggedUser);
        List<ProducerResponseDTO> response = producers.stream()
                .map(Mapper::toProducerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }
}

