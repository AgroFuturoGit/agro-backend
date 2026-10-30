package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.exception.handler.GlobalExceptionHandler;
import com.ufal.smartagro.application.service.production.AttachmentUploadResult;
import com.ufal.smartagro.application.service.production.DeleteExecutionAttachmentUseCase;
import com.ufal.smartagro.application.service.production.DownloadExecutionAttachmentUseCase;
import com.ufal.smartagro.application.service.production.ListExecutionAttachmentsUseCase;
import com.ufal.smartagro.application.service.production.NewExecutionAttachment;
import com.ufal.smartagro.application.service.production.UploadExecutionAttachmentUseCase;
import com.ufal.smartagro.config.security.SecurityConfig;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.config.security.filter.JwtAuthenticationFilter;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExecutionAttachmentController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ExecutionAttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadExecutionAttachmentUseCase uploadUseCase;

    @MockitoBean
    private ListExecutionAttachmentsUseCase listUseCase;

    @MockitoBean
    private DownloadExecutionAttachmentUseCase downloadUseCase;

    @MockitoBean
    private DeleteExecutionAttachmentUseCase deleteUseCase;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setupFilter() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("Given arquivo válido When upload Then consome via stream e retorna 201 Created")
    void givenValidFile_whenUpload_thenReadsViaStreamAndReturns201() throws Exception {
        UUID executionId = UUID.randomUUID();
        UUID attachmentId = UUID.randomUUID();
        User farmer = UserTestFactory.user().farmer().build();
        UserDetailsImpl userDetails = new UserDetailsImpl(farmer, "password");

        MockMultipartFile file = new MockMultipartFile(
                "file", "colheita.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-bytes".getBytes()
        );

        ExecutionAttachment saved = new ExecutionAttachment(
                attachmentId, executionId, null, "colheita.jpg", MediaType.IMAGE_JPEG_VALUE, 16, "fake-image-bytes".getBytes(), LocalDateTime.now()
        );

        when(uploadUseCase.upload(eq(executionId), any(NewExecutionAttachment.class), any(User.class)))
                .thenReturn(new AttachmentUploadResult(saved, true));

        mockMvc.perform(multipart("/production-executions/{executionId}/attachments", executionId)
                        .file(file)
                        .with(user(userDetails)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/production-executions/" + executionId + "/attachments/" + attachmentId))
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.filename").value("colheita.jpg"));

        ArgumentCaptor<NewExecutionAttachment> captor = ArgumentCaptor.forClass(NewExecutionAttachment.class);
        verify(uploadUseCase).upload(eq(executionId), captor.capture(), any(User.class));
        assertThat(captor.getValue().content()).isEqualTo("fake-image-bytes".getBytes());
    }

    @Test
    @DisplayName("Given imagem maior que 5 MB no caso de uso When upload Then retorna 413 Payload Too Large")
    void givenOversizedImage_whenUpload_thenReturns413() throws Exception {
        UUID executionId = UUID.randomUUID();
        User farmer = UserTestFactory.user().farmer().build();
        UserDetailsImpl userDetails = new UserDetailsImpl(farmer, "password");

        MockMultipartFile file = new MockMultipartFile(
                "file", "grande.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[10]
        );

        when(uploadUseCase.upload(eq(executionId), any(NewExecutionAttachment.class), any(User.class)))
                .thenThrow(new IllegalArgumentException("A imagem excede o limite de 5 MB."));

        mockMvc.perform(multipart("/production-executions/{executionId}/attachments", executionId)
                        .file(file)
                        .with(user(userDetails)))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.message").value("A imagem excede o limite de 5 MB."));
    }
}
