package com.ufal.smartagro.adapters.in.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.crop.CropUpdateDTO;
import com.ufal.smartagro.application.service.crop.CropDeleteUseCase;
import com.ufal.smartagro.application.service.crop.CropFindAllUseCase;
import com.ufal.smartagro.application.service.crop.CropFindByIdUseCase;
import com.ufal.smartagro.application.service.crop.CropRegisterUseCase;
import com.ufal.smartagro.application.service.crop.CropUpdateUseCase;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

@WebMvcTest(CropController.class)
@Import(SecurityConfig.class)
@DisplayName("CropController — validações de endpoints e RBAC")
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
    @DisplayName("POST /crops — ADMIN cadastra cultura com sucesso e recebe 201 com Location")
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
    @DisplayName("POST /crops — FARMER não pode cadastrar cultura (403 Forbidden)")
    void registerCrop_asFarmer_returnsForbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        CropRegisterDTO dto = new CropRegisterDTO(
                "Milho", "Híbrido", true, 90, BigDecimal.valueOf(8000), HarvestType.SINGLE, MeasurementUnit.KG, BigDecimal.valueOf(0.25));

        mockMvc.perform(post("/crops")
                        .with(user(userDetails(farmerUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /crops/{id} — TECHNICIAN pode atualizar cultura (200 OK)")
    void updateCrop_asTechnician_returnsOk() throws Exception {
        User techUser = UserTestFactory.user().technician().build();
        UUID cropId = UUID.randomUUID();
        Crop updatedCrop = new Crop(cropId, "Milho Especial", "Híbrido", true, 95, BigDecimal.valueOf(8500), HarvestType.SINGLE, MeasurementUnit.KG, BigDecimal.valueOf(0.25));
        when(cropUpdateUseCase.execute(eq(cropId), any(), any(), any())).thenReturn(updatedCrop);

        CropUpdateDTO dto = new CropUpdateDTO("Milho Especial", null, null, 95, null, null, null, null, null);

        mockMvc.perform(patch("/crops/{id}", cropId)
                        .with(user(userDetails(techUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Milho Especial"));
    }

    @Test
    @DisplayName("PUT /crops/{id} — ADMIN pode atualizar cultura (200 OK)")
    void updateCrop_asAdmin_returnsOk() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();
        Crop updatedCrop = new Crop(cropId, "Milho", "Híbrido", true, 90, BigDecimal.valueOf(8000), HarvestType.SINGLE, MeasurementUnit.KG, BigDecimal.valueOf(0.25));
        when(cropUpdateUseCase.execute(eq(cropId), any(), any(), any())).thenReturn(updatedCrop);

        CropUpdateDTO dto = new CropUpdateDTO("Milho", null, null, null, null, null, null, null, null);

        mockMvc.perform(put("/crops/{id}", cropId)
                        .with(user(userDetails(adminUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /crops — FARMER pode consultar lista de culturas (200 OK)")
    void findAllCrops_asFarmer_returnsOk() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        Crop crop = new Crop(UUID.randomUUID(), "Milho", "Híbrido", true, 90, null, null, null, null);
        when(cropFindAllUseCase.execute(any())).thenReturn(List.of(crop));

        mockMvc.perform(get("/crops")
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Milho"));
    }

    @Test
    @DisplayName("GET /crops/{id} — FARMER pode consultar detalhes de uma cultura (200 OK)")
    void findCropById_asFarmer_returnsOk() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        UUID cropId = UUID.randomUUID();
        Crop crop = new Crop(cropId, "Milho", "Híbrido", true, 90, null, null, null, null);
        when(cropFindByIdUseCase.execute(eq(cropId), any())).thenReturn(crop);

        mockMvc.perform(get("/crops/{id}", cropId)
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cropId.toString()));
    }

    @Test
    @DisplayName("DELETE /crops/{id} — ADMIN pode excluir cultura (204 No Content)")
    void deleteCrop_asAdmin_returnsNoContent() throws Exception {
        User adminUser = UserTestFactory.user().admin().build();
        UUID cropId = UUID.randomUUID();

        mockMvc.perform(delete("/crops/{id}", cropId)
                        .with(user(userDetails(adminUser))))
                .andExpect(status().isNoContent());

        verify(cropDeleteUseCase).execute(eq(cropId), any());
    }

    @Test
    @DisplayName("DELETE /crops/{id} — FARMER não pode excluir cultura (403 Forbidden)")
    void deleteCrop_asFarmer_returnsForbidden() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().build();
        UUID cropId = UUID.randomUUID();

        mockMvc.perform(delete("/crops/{id}", cropId)
                        .with(user(userDetails(farmerUser))))
                .andExpect(status().isForbidden());
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
