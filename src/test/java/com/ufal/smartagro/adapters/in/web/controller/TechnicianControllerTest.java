package com.ufal.smartagro.adapters.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufal.smartagro.adapters.in.web.dto.technicalassistance.TechnicalAssistanceRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.technician.TechnicianUpdateDTO;
import com.ufal.smartagro.application.service.technician.AssignCommunityToTechnicianUseCase;
import com.ufal.smartagro.application.service.technician.DeleteTechnicianUseCase;
import com.ufal.smartagro.application.service.technician.FindAllTechniciansUseCase;
import com.ufal.smartagro.application.service.technician.FindTechnicianByIdUseCase;
import com.ufal.smartagro.application.service.technician.FindTechnicianByUserUseCase;
import com.ufal.smartagro.application.service.technician.GetAssignedCommunitiesUseCase;
import com.ufal.smartagro.application.service.technician.GetAssignedFarmersUseCase;
import com.ufal.smartagro.application.service.technician.RegisterTechnicianUseCase;
import com.ufal.smartagro.application.service.technician.RemoveCommunityFromTechnicianUseCase;
import com.ufal.smartagro.application.service.technician.UpdateTechnicianUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.TechnicalAssistance;
import com.ufal.smartagro.domain.model.Technician;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TechnicianController.class)
@Import(SecurityConfig.class)
class TechnicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterTechnicianUseCase registerTechnicianUseCase;
    @MockitoBean
    private FindTechnicianByIdUseCase findTechnicianByIdUseCase;
    @MockitoBean
    private FindTechnicianByUserUseCase findTechnicianByUserUseCase;
    @MockitoBean
    private FindAllTechniciansUseCase findAllTechniciansUseCase;
    @MockitoBean
    private UpdateTechnicianUseCase updateTechnicianUseCase;
    @MockitoBean
    private DeleteTechnicianUseCase deleteTechnicianUseCase;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AssignCommunityToTechnicianUseCase assignCommunityToTechnicianUseCase;
    @MockitoBean
    private RemoveCommunityFromTechnicianUseCase removeCommunityFromTechnicianUseCase;
    @MockitoBean
    private GetAssignedCommunitiesUseCase getAssignedCommunitiesUseCase;
    @MockitoBean
    private GetAssignedFarmersUseCase getAssignedFarmersUseCase;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void makeJwtFilterContinueTheSecurityChain() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void assignAssistance_asAdmin_createsAssistanceSuccessfully() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        Community community = UserTestFactory.community().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance()
                .technician(technician)
                .community(community)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(assignCommunityToTechnicianUseCase.assign(any(), any(), any())).thenReturn(assistance);

        TechnicalAssistanceRegisterDTO dto = new TechnicalAssistanceRegisterDTO(
                technician.getId(), community.getId(), LocalDateTime.of(2026, 3, 1, 10, 0));

        mockMvc.perform(post("/technicians/{technicianId}/assistances", technician.getId())
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicianId").value(technician.getId().toString()))
                .andExpect(jsonPath("$.communityId").value(community.getId().toString()));
    }

    @Test
    void assignAssistance_asFarmer_forbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();

        TechnicalAssistanceRegisterDTO dto = new TechnicalAssistanceRegisterDTO(
                UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.of(2026, 3, 1, 10, 0));

        mockMvc.perform(post("/technicians/{technicianId}/assistances", UUID.randomUUID())
                        .with(user(userDetails(farmerUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeAssistance_asTechnician_returnsSuccess() throws Exception {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();
        Community community = UserTestFactory.community().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance()
                .technician(technician)
                .community(community)
                .ended()
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(techUser));
        when(removeCommunityFromTechnicianUseCase.remove(any(), any(), any())).thenReturn(assistance);

        mockMvc.perform(delete("/technicians/{technicianId}/assistances/{assistanceId}", technician.getId(), assistance.getId())
                        .with(user(userDetails(techUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityId").value(community.getId().toString()));
    }

    @Test
    void assignAssistance_whenMismatchedTechnicianId_returnsBadRequest() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID pathTechnicianId = UUID.randomUUID();
        UUID bodyTechnicianId = UUID.randomUUID();
        TechnicalAssistanceRegisterDTO dto = new TechnicalAssistanceRegisterDTO(
                bodyTechnicianId, UUID.randomUUID(), LocalDateTime.of(2026, 3, 1, 10, 0));

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(assignCommunityToTechnicianUseCase.assign(eq(pathTechnicianId), any(), any()))
                .thenThrow(new IllegalArgumentException("O ID do técnico informado na URL não coincide com o do corpo da requisição."));

        mockMvc.perform(post("/technicians/{technicianId}/assistances", pathTechnicianId)
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("O ID do técnico informado na URL não coincide com o do corpo da requisição."));
    }

    @Test
    void assignAssistance_withoutTechnicianIdInBody_usesPathVariableSuccessfully() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        Community community = UserTestFactory.community().build();
        TechnicalAssistance assistance = UserTestFactory.technicalAssistance()
                .technician(technician)
                .community(community)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(assignCommunityToTechnicianUseCase.assign(eq(technician.getId()), any(), any())).thenReturn(assistance);

        TechnicalAssistanceRegisterDTO dto = new TechnicalAssistanceRegisterDTO(
                null, community.getId(), LocalDateTime.of(2026, 3, 1, 10, 0));

        mockMvc.perform(post("/technicians/{technicianId}/assistances", technician.getId())
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicianId").value(technician.getId().toString()))
                .andExpect(jsonPath("$.communityId").value(community.getId().toString()));
    }

    @Test
    void removeAssistance_whenNotFound_returnsNotFound404() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID technicianId = UUID.randomUUID();
        UUID assistanceId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(removeCommunityFromTechnicianUseCase.remove(any(), any(), any()))
                .thenThrow(new com.ufal.smartagro.domain.exception.EntityNotFoundException("Assistência técnica não encontrada."));

        mockMvc.perform(delete("/technicians/{technicianId}/assistances/{assistanceId}", technicianId, assistanceId)
                        .with(user(userDetails(adminUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Assistência técnica não encontrada."));
    }

    @Test
    void getMyCommunities_asTechnician_returnsList() throws Exception {
        User techUser = UserTestFactory.user().technician().build();
        Community community = UserTestFactory.community().name("Comunidade Esperança").build();

        when(userRepository.findById(any())).thenReturn(Optional.of(techUser));
        when(getAssignedCommunitiesUseCase.getAssignedCommunities(techUser)).thenReturn(List.of(community));

        mockMvc.perform(get("/technicians/me/communities")
                        .with(user(userDetails(techUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(community.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Comunidade Esperança"));
    }

    @Test
    void getTechnicianCommunities_asManager_returnsList() throws Exception {
        User managerUser = UserTestFactory.user().manager().build();
        Community community = UserTestFactory.community().name("Comunidade Sol").build();
        UUID technicianId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.of(managerUser));
        when(getAssignedCommunitiesUseCase.getAssignedCommunities(technicianId, managerUser)).thenReturn(List.of(community));

        mockMvc.perform(get("/technicians/{id}/communities", technicianId)
                        .with(user(userDetails(managerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(community.getId().toString()))
                .andExpect(jsonPath("$[0].name").value("Comunidade Sol"));
    }

    @Test
    void getTechnicianCommunities_asFarmer_forbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();

        mockMvc.perform(get("/technicians/{id}/communities", UUID.randomUUID())
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyFarmers_asTechnician_returnsList() throws Exception {
        User techUser = UserTestFactory.user().technician().build();
        com.ufal.smartagro.domain.model.Farmer farmer = UserTestFactory.farmerProfile().build();

        when(userRepository.findById(any())).thenReturn(Optional.of(techUser));
        when(getAssignedFarmersUseCase.getAssignedFarmers(techUser)).thenReturn(List.of(farmer));

        mockMvc.perform(get("/technicians/me/farmers")
                        .with(user(userDetails(techUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(farmer.getId().toString()));
    }

    @Test
    void getTechnicianFarmers_asManager_returnsList() throws Exception {
        User managerUser = UserTestFactory.user().manager().build();
        com.ufal.smartagro.domain.model.Farmer farmer = UserTestFactory.farmerProfile().build();
        UUID technicianId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.of(managerUser));
        when(getAssignedFarmersUseCase.getAssignedFarmers(technicianId, managerUser)).thenReturn(List.of(farmer));

        mockMvc.perform(get("/technicians/{id}/farmers", technicianId)
                        .with(user(userDetails(managerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(farmer.getId().toString()));
    }

    @Test
    void findMyTechnicianData_asTechnician_returnsSuccess() throws Exception {
        User techUser = UserTestFactory.user().technician().build();
        Technician technician = UserTestFactory.technicianProfile().user(techUser).build();

        when(userRepository.findById(any())).thenReturn(Optional.of(techUser));
        when(findTechnicianByUserUseCase.findByUser(techUser)).thenReturn(technician);

        mockMvc.perform(get("/technicians/me")
                        .with(user(userDetails(techUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(technician.getId().toString()))
                .andExpect(jsonPath("$.professionalId").value(technician.getProfessionalId()));
    }

    @Test
    void findMyTechnicianData_whenNotFound_returnsNotFound404() throws Exception {
        User techUser = UserTestFactory.user().technician().build();

        when(userRepository.findById(any())).thenReturn(Optional.of(techUser));
        when(findTechnicianByUserUseCase.findByUser(techUser))
                .thenThrow(new com.ufal.smartagro.domain.exception.EntityNotFoundException("Técnico não encontrado."));

        mockMvc.perform(get("/technicians/me")
                        .with(user(userDetails(techUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Técnico não encontrado."));
    }

    @Test
    void findMyTechnicianData_asFarmer_forbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();

        mockMvc.perform(get("/technicians/me")
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerTechnician_asAdmin_returnsCreated() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        TechnicianRegisterDTO dto = new TechnicianRegisterDTO(
                "João Técnico", "joao.tech@smartagro.test", "12345678", UserTestFactory.VALID_CPF,
                java.time.LocalDate.of(1990, 5, 15), "CRBio-12345", "Agronomia");

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(registerTechnicianUseCase.register(any(), eq(adminUser))).thenReturn(technician);

        mockMvc.perform(post("/technicians")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(technician.getId().toString()));
    }

    @Test
    void findAllTechnicians_asAdmin_returnsList() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(findAllTechniciansUseCase.findAll(adminUser)).thenReturn(List.of(technician));

        mockMvc.perform(get("/technicians")
                        .with(user(userDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(technician.getId().toString()));
    }

    @Test
    void findTechnicianById_asAdmin_returnsTechnician() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(findTechnicianByIdUseCase.findById(technician.getId(), adminUser)).thenReturn(technician);

        mockMvc.perform(get("/technicians/{id}", technician.getId())
                        .with(user(userDetails(adminUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(technician.getId().toString()));
    }

    @Test
    void updateTechnician_asAdmin_returnsUpdatedTechnician() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        Technician technician = UserTestFactory.technicianProfile().build();
        TechnicianUpdateDTO dto = new TechnicianUpdateDTO("CRBio-99999", "Solo");

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(updateTechnicianUseCase.update(eq(technician.getId()), any(), eq(adminUser))).thenReturn(technician);

        mockMvc.perform(put("/technicians/{id}", technician.getId())
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(technician.getId().toString()));
    }

    @Test
    void deleteTechnician_asAdmin_returnsNoContent() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID technicianId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));

        mockMvc.perform(delete("/technicians/{id}", technicianId)
                        .with(user(userDetails(adminUser))))
                .andExpect(status().isNoContent());
    }

    private UserDetailsImpl userDetails(User user) {
        com.ufal.smartagro.adapters.out.persistence.entity.UserEntity entity = new com.ufal.smartagro.adapters.out.persistence.entity.UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        return new UserDetailsImpl(entity);
    }
}
