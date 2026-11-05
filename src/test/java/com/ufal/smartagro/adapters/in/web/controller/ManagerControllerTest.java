package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.out.persistence.entity.UserEntity;
import com.ufal.smartagro.application.service.manager.DeleteManagerUseCase;
import com.ufal.smartagro.application.service.manager.FindAllManagersUseCase;
import com.ufal.smartagro.application.service.manager.FindManagerByIdUseCase;
import com.ufal.smartagro.application.service.manager.FindManagerByUserUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.model.Manager;
import com.ufal.smartagro.domain.model.Organization;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
@Import(SecurityConfig.class)
@DisplayName("ManagerController — endpoints de consulta e exclusão de gestores")
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindManagerByUserUseCase findManagerByUserUseCase;

    @MockitoBean
    private FindAllManagersUseCase findAllManagersUseCase;

    @MockitoBean
    private FindManagerByIdUseCase findManagerByIdUseCase;

    @MockitoBean
    private DeleteManagerUseCase deleteManagerUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User adminUser;
    private User managerUser;
    private Manager managerProfile;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());

        adminUser = UserTestFactory.user().role(Role.ADMIN).build();
        managerUser = UserTestFactory.user().role(Role.MANAGER).build();
        Organization org = UserTestFactory.organization().build();
        managerProfile = UserTestFactory.managerProfile().user(managerUser).organization(org).build();
    }

    private UserDetailsImpl userDetailsFor(User domainUser) {
        UserEntity entity = new UserEntity();
        entity.setId(domainUser.getId());
        entity.setFullName(domainUser.getFullName());
        entity.setEmail(domainUser.getEmail());
        entity.setPassword(domainUser.getPassword());
        entity.setCpf(domainUser.getCpf());
        entity.setRole(domainUser.getRole());
        return new UserDetailsImpl(entity);
    }

    @Test
    @DisplayName("GET /managers como ADMIN retorna 200 OK com lista de gestores")
    void findAll_asAdmin_returns200() throws Exception {
        when(findAllManagersUseCase.findAll(eq(null), any(User.class))).thenReturn(List.of(managerProfile));

        mockMvc.perform(get("/managers").with(user(userDetailsFor(adminUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(managerProfile.getId().toString()));
    }

    @Test
    @DisplayName("GET /managers como MANAGER retorna 403 Forbidden")
    void findAll_asManager_returns403() throws Exception {
        mockMvc.perform(get("/managers").with(user(userDetailsFor(managerUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /managers/{id} como ADMIN retorna 204 No Content")
    void delete_asAdmin_returns204() throws Exception {
        UUID id = managerProfile.getId();
        doNothing().when(deleteManagerUseCase).delete(eq(id), any(User.class));

        mockMvc.perform(delete("/managers/{id}", id).with(user(userDetailsFor(adminUser))))
                .andExpect(status().isNoContent());

        verify(deleteManagerUseCase).delete(eq(id), any(User.class));
    }

    @Test
    @DisplayName("DELETE /managers/{id} como MANAGER retorna 403 Forbidden")
    void delete_asManager_returns403() throws Exception {
        mockMvc.perform(delete("/managers/{id}", managerProfile.getId()).with(user(userDetailsFor(managerUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /managers/{id} para gestor inexistente retorna 404 Not Found")
    void delete_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Gestor não encontrado."))
                .when(deleteManagerUseCase).delete(eq(id), any(User.class));

        mockMvc.perform(delete("/managers/{id}", id).with(user(userDetailsFor(adminUser))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Gestor não encontrado."));
    }
}
