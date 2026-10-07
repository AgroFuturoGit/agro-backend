package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.application.service.technician.*;
import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.farmer.FarmerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Técnicos & Assistência Técnica", description = "Gerenciamento de técnicos agrícolas e atendimentos a comunidades")
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
    private final AssignCommunityToTechnicianUseCase assignCommunityToTechnicianUseCase;
    private final RemoveCommunityFromTechnicianUseCase removeCommunityFromTechnicianUseCase;
    private final GetAssignedCommunitiesUseCase getAssignedCommunitiesUseCase;
    private final GetAssignedFarmersUseCase getAssignedFarmersUseCase;

    @Operation(summary = "Cadastrar técnico", description = "Registra um novo técnico agrícola no sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Técnico cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
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

    @Operation(summary = "Buscar dados do técnico autenticado", description = "Retorna os dados cadastrais do técnico logado a partir do token.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil TECHNICIAN)"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/me")
    public ResponseEntity<TechnicianResponseDTO> findMyTechnicianData(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        Technician technician = findTechnicianByUserUseCase.findByUser(loggedUser);
        TechnicianResponseDTO response = Mapper.toTechnicianResponseDTO(technician);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os técnicos", description = "Retorna a lista de técnicos agrícolas cadastrados.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
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

    @Operation(summary = "Buscar técnico por ID", description = "Recupera os detalhes de um técnico específico.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico encontrado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
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

    @Operation(summary = "Atualizar técnico", description = "Atualiza os dados cadastrais de um técnico agrícola.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Técnico atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
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

    @Operation(summary = "Excluir técnico", description = "Remove um técnico do sistema.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Técnico excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
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

    @Operation(summary = "Registrar assistência técnica", description = "Vincula um atendimento de assistência técnica entre um técnico e uma comunidade.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Assistência registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico ou comunidade não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("/{technicianId}/assistances")
    public ResponseEntity<TechnicalAssistanceResponseDTO> assignAssistance(
            @PathVariable UUID technicianId,
            @Valid @RequestBody TechnicalAssistanceRegisterDTO dto,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);

        TechnicalAssistance assistance = assignCommunityToTechnicianUseCase.assign(technicianId, dto, loggedUser);
        TechnicalAssistanceResponseDTO response = Mapper.toTechnicalAssistanceResponseDTO(assistance);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Remover assistência técnica", description = "Remove um atendimento de assistência técnica registrado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assistência técnica removida com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Assistência técnica não encontrada")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TECHNICIAN')")
    @DeleteMapping("/{technicianId}/assistances/{assistanceId}")
    public ResponseEntity<TechnicalAssistanceResponseDTO> removeAssistance(
            @PathVariable UUID technicianId,
            @PathVariable UUID assistanceId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        TechnicalAssistance assistance = removeCommunityFromTechnicianUseCase.remove(technicianId, assistanceId, loggedUser);
        TechnicalAssistanceResponseDTO response = Mapper.toTechnicalAssistanceResponseDTO(assistance);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar comunidades assistidas pelo técnico logado", description = "Retorna as comunidades vinculadas ao técnico autenticado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil TECHNICIAN)")
    })
    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/me/communities")
    public ResponseEntity<List<CommunityResponseDTO>> getMyCommunities(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        List<Community> communities = getAssignedCommunitiesUseCase.getAssignedCommunities(loggedUser);
        List<CommunityResponseDTO> response = communities.stream()
                .map(Mapper::toCommunityResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar comunidades de determinado técnico", description = "Permite a gestores e administradores visualizar as comunidades vinculadas a um técnico específico.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}/communities")
    public ResponseEntity<List<CommunityResponseDTO>> getTechnicianCommunities(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        List<Community> communities = getAssignedCommunitiesUseCase.getAssignedCommunities(id, loggedUser);
        List<CommunityResponseDTO> response = communities.stream()
                .map(Mapper::toCommunityResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agricultores assistidos pelo técnico logado", description = "Retorna os agricultores vinculados ao técnico autenticado.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer perfil TECHNICIAN)")
    })
    @PreAuthorize("hasRole('TECHNICIAN')")
    @GetMapping("/me/farmers")
    public ResponseEntity<List<FarmerResponseDTO>> getMyFarmers(
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        List<Farmer> farmers = getAssignedFarmersUseCase.getAssignedFarmers(loggedUser);
        List<FarmerResponseDTO> response = farmers.stream()
                .map(Mapper::toFarmerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar agricultores de determinado técnico", description = "Permite a gestores e administradores visualizar os agricultores vinculados a um técnico específico.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Técnico não encontrado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}/farmers")
    public ResponseEntity<List<FarmerResponseDTO>> getTechnicianFarmers(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
        List<Farmer> farmers = getAssignedFarmersUseCase.getAssignedFarmers(id, loggedUser);
        List<FarmerResponseDTO> response = farmers.stream()
                .map(Mapper::toFarmerResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }
}

