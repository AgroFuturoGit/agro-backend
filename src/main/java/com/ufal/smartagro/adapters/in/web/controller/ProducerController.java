package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.producer.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.model.Producer;
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
@RequestMapping("/producers")
public class ProducerController {

    private final FindProducerByUserUseCase findProducerByUserUseCase;
    private final FindProducerByIdUseCase findProducerByIdUseCase;
    private final FindAllProducersUseCase findAllProducersUseCase;
    private final UpdateProducerUseCase updateProducerUseCase;
    private final DeleteProducerUseCase deleteProducerUseCase;

    @PreAuthorize("hasRole('PRODUCER')")
    @GetMapping("/me")
    public ResponseEntity<ProducerResponseDTO> findMyProducerData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Producer producer = findProducerByUserUseCase.findByUserId(loggedUserDetails.getId());
        ProducerResponseDTO response = Mapper.toProducerResponseDTO(producer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProducerResponseDTO>> findAllProducers(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        List<Producer> producers = findAllProducersUseCase.findAll();
        List<ProducerResponseDTO> response = producers.stream()
                .map(Mapper::toProducerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProducerResponseDTO> findProducerById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Producer producer = findProducerByIdUseCase.findById(id);
        ProducerResponseDTO response = Mapper.toProducerResponseDTO(producer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProducerResponseDTO> updateProducer(
            @PathVariable UUID id,
            @Valid @RequestBody ProducerUpdateDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        Producer producer = updateProducerUseCase.update(id, dto);
        ProducerResponseDTO response = Mapper.toProducerResponseDTO(producer);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducer(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        deleteProducerUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
