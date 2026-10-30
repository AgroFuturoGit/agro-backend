package com.ufal.smartagro.adapters.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.application.service.user.*;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegisterUseCase userRegisterUseCase;
    @MockitoBean
    private UserDeleteUseCase userDeleteUseCase;
    @MockitoBean
    private FindUserByIdUseCase findUserByIdUseCase;
    @MockitoBean
    private UserUpdateUseCase userUpdateUseCase;
    @MockitoBean
    private AdminUserUpdateUseCase adminUserUpdateUseCase;
    @MockitoBean
    private FindAllUsersUseCase findAllUsersUseCase;
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

    @Test
    void registerUser_standardPost_returnsCreatedAndLocationHeader() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID newUserId = UUID.randomUUID();
        User createdUser = new User(newUserId, "Novo Usuario", "novo@smartagro.test", "senha", UserTestFactory.VALID_CPF, Role.ADMIN);

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(userRegisterUseCase.register(any(), any())).thenReturn(createdUser);

        UserRegisterDTO dto = new UserRegisterDTO("Novo Usuario", "novo@smartagro.test", "senhaForte123", UserTestFactory.VALID_CPF, Role.ADMIN);

        mockMvc.perform(post("/users")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + newUserId))
                .andExpect(jsonPath("$.id").value(newUserId.toString()))
                .andExpect(jsonPath("$.fullName").value("Novo Usuario"));
    }

    @Test
    void registerUser_legacyRegisterPost_returnsCreatedAndLocationHeader() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID newUserId = UUID.randomUUID();
        User createdUser = new User(newUserId, "Outro Usuario", "outro@smartagro.test", "senha", UserTestFactory.VALID_CPF, Role.ADMIN);

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(userRegisterUseCase.register(any(), any())).thenReturn(createdUser);

        UserRegisterDTO dto = new UserRegisterDTO("Outro Usuario", "outro@smartagro.test", "senhaForte123", UserTestFactory.VALID_CPF, Role.ADMIN);

        mockMvc.perform(post("/users/register")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/" + newUserId))
                .andExpect(jsonPath("$.id").value(newUserId.toString()));
    }

    private UserDetailsImpl userDetails(User user) {
        var entity = new com.ufal.smartagro.adapters.out.persistence.entity.UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setRole(user.getRole());
        return new UserDetailsImpl(entity);
    }
}
