package com.ufal.smartagro.adapters.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.application.service.crop.*;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.Crop;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.HarvestType;
import com.ufal.smartagro.domain.model.enums.MeasurementUnit;
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

import java.math.BigDecimal;
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

@WebMvcTest(CropController.class)
@Import(SecurityConfig.class)
class CropControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CropRegisterUseCase cropRegisterUseCase;
    @MockitoBean
    private CropUpdateUseCase cropUpdateUseCase;
    @MockitoBean
    private CropFindByIdUseCase cropFindByIdUseCase;
    @MockitoBean
    private CropFindAllUseCase cropFindAllUseCase;
    @MockitoBean
    private CropDeleteUseCase cropDeleteUseCase;
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
    void registerCrop_standardPost_returnsCreatedAndLocationHeader() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        Crop savedCrop = new Crop(cropId, "Milho", "Híbrido", true, 90, BigDecimal.valueOf(8000), HarvestType.SINGLE, MeasurementUnit.KG, BigDecimal.valueOf(0.25));

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(cropRegisterUseCase.execute(any(), any())).thenReturn(savedCrop);

        CropRegisterDTO dto = new CropRegisterDTO(
                "Milho", "Híbrido", true, 90, BigDecimal.valueOf(8000), HarvestType.SINGLE, MeasurementUnit.KG, BigDecimal.valueOf(0.25));

        mockMvc.perform(post("/crops")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/crops/" + cropId))
                .andExpect(jsonPath("$.id").value(cropId.toString()))
                .andExpect(jsonPath("$.name").value("Milho"));
    }

    @Test
    void registerCrop_legacyRegisterPost_returnsCreatedAndLocationHeader() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        Crop savedCrop = new Crop(cropId, "Feijão", "Carioca", true, 60, BigDecimal.valueOf(2500), HarvestType.CONTINUOUS, MeasurementUnit.KG, BigDecimal.valueOf(0.1));

        when(userRepository.findById(any())).thenReturn(Optional.of(adminUser));
        when(cropRegisterUseCase.execute(any(), any())).thenReturn(savedCrop);

        CropRegisterDTO dto = new CropRegisterDTO(
                "Feijão", "Carioca", true, 60, BigDecimal.valueOf(2500), HarvestType.CONTINUOUS, MeasurementUnit.KG, BigDecimal.valueOf(0.1));

        mockMvc.perform(post("/crops/register")
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/crops/" + cropId))
                .andExpect(jsonPath("$.id").value(cropId.toString()));
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
