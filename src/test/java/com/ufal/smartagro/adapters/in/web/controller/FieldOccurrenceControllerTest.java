package com.ufal.smartagro.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.occurrence.FieldOccurrenceUpdateDTO;
import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.production.occurrence.DeleteFieldOccurrenceUseCase;
import com.ufal.smartagro.application.service.production.occurrence.FindAllOccurrencesByExecutionUseCase;
import com.ufal.smartagro.application.service.production.occurrence.FindFieldOccurrenceByIdUseCase;
import com.ufal.smartagro.application.service.production.occurrence.RegisterFieldOccurrenceUseCase;
import com.ufal.smartagro.application.service.production.occurrence.UpdateFieldOccurrenceUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.FieldOccurrence;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.OccurrenceType;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FieldOccurrenceController.class)
@Import(SecurityConfig.class)
@DisplayName("FieldOccurrenceController — RBAC e proteção de rotas")
class FieldOccurrenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterFieldOccurrenceUseCase registerFieldOccurrenceUseCase;
    @MockitoBean
    private FindAllOccurrencesByExecutionUseCase findAllOccurrencesByExecutionUseCase;
    @MockitoBean
    private FindFieldOccurrenceByIdUseCase findFieldOccurrenceByIdUseCase;
    @MockitoBean
    private UpdateFieldOccurrenceUseCase updateFieldOccurrenceUseCase;
    @MockitoBean
    private DeleteFieldOccurrenceUseCase deleteFieldOccurrenceUseCase;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void makeJwtFilterContinueTheSecurityChain() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private UserDetailsImpl userDetailsFor(User domainUser) {
        UserEntity entity = new UserEntity();
        entity.setId(domainUser.getId());
        entity.setEmail(domainUser.getEmail());
        entity.setPassword(domainUser.getPassword());
        entity.setFullName(domainUser.getFullName());
        entity.setCpf(domainUser.getCpf());
        entity.setRole(domainUser.getRole());
        return new UserDetailsImpl(entity);
    }

    private FieldOccurrence dummyOccurrence(UUID id) {
        return FieldOccurrence.builder()
                .id(id)
                .execution(ProductionExecution.builder().id(UUID.randomUUID()).build())
                .logType(OccurrenceType.PEST)
                .description("Lagarta identificada no lote")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /production-executions/{id}/occurrences — MANAGER recebe 403 e FARMER recebe 201")
    void givenRegister_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID execId = UUID.randomUUID();
        FieldOccurrenceRegisterDTO dto = new FieldOccurrenceRegisterDTO(
                UUID.randomUUID(), OccurrenceType.PEST, "Praga detectada", null, null
        );

        when(registerFieldOccurrenceUseCase.register(eq(execId), any(), any()))
                .thenReturn(dummyOccurrence(UUID.randomUUID()));

        // FARMER permitido
        mockMvc.perform(post("/production-executions/" + execId + "/occurrences")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        // MANAGER proibido
        mockMvc.perform(post("/production-executions/" + execId + "/occurrences")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /production-executions/{id}/occurrences — permitido para FARMER e MANAGER")
    void givenFindAll_whenRolesTested_thenAllowed() throws Exception {
        UUID execId = UUID.randomUUID();
        when(findAllOccurrencesByExecutionUseCase.findAll(eq(execId), any()))
                .thenReturn(List.of(dummyOccurrence(UUID.randomUUID())));

        mockMvc.perform(get("/production-executions/" + execId + "/occurrences")
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build()))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/production-executions/" + execId + "/occurrences")
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build()))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /occurrences/{id} — MANAGER recebe 403 e TECHNICIAN recebe 200")
    void givenUpdate_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID id = UUID.randomUUID();
        FieldOccurrenceUpdateDTO dto = new FieldOccurrenceUpdateDTO(
                OccurrenceType.PEST, "Descrição atualizada", null, null
        );

        when(updateFieldOccurrenceUseCase.update(eq(id), any(), any()))
                .thenReturn(dummyOccurrence(id));

        mockMvc.perform(put("/occurrences/" + id)
                        .with(user(userDetailsFor(UserTestFactory.user().technician().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/occurrences/" + id)
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /occurrences/{id} — MANAGER recebe 403 e FARMER recebe 204")
    void givenDelete_whenRolesTested_thenEnforcesRbac() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/occurrences/" + id)
                        .with(user(userDetailsFor(UserTestFactory.user().farmer().build()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/occurrences/" + id)
                        .with(user(userDetailsFor(UserTestFactory.user().manager().build()))))
                .andExpect(status().isForbidden());
    }
}
