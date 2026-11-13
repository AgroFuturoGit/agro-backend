package com.ufal.smartagro.adapters.in.web.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.production.ProductionPlanResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserResponseDTO;
import com.ufal.smartagro.adapters.in.web.exception.dto.ApiError;
import com.ufal.smartagro.adapters.in.web.exception.dto.ConflictApiError;
import com.ufal.smartagro.domain.exception.ConcurrentUpdateException;
import com.ufal.smartagro.domain.exception.InvalidStateTransitionException;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.ProductionPlan;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.ExecutionStatus;
import com.ufal.smartagro.domain.model.enums.PlanStatus;
import com.ufal.smartagro.testsupport.UserTestFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Given MaxUploadSizeExceededException When tratado Then retorna HTTP 413 com mensagem amigável")
    void givenMaxUploadSizeExceeded_whenHandled_thenReturns413() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(5L * 1024 * 1024);
        HttpServletRequest request = new MockHttpServletRequest("POST", "/production-executions/123/attachments");

        ResponseEntity<ApiError> response = handler.handleMaxUploadSizeExceeded(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(413);
        assertThat(response.getBody().message()).isEqualTo("O arquivo enviado excede o tamanho máximo permitido de 5 MB.");
        assertThat(response.getBody().path()).isEqualTo("/production-executions/123/attachments");
    }

    @Test
    @DisplayName("Given IllegalArgumentException com mensagem de limite excedido When tratado Then retorna HTTP 413")
    void givenIllegalArgumentWithLimitMessage_whenHandled_thenReturns413() {
        IllegalArgumentException ex = new IllegalArgumentException("A imagem excede o limite de 5 MB.");
        HttpServletRequest request = new MockHttpServletRequest("POST", "/production-executions/123/attachments");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(413);
        assertThat(response.getBody().message()).isEqualTo("A imagem excede o limite de 5 MB.");
    }

    @Test
    @DisplayName("Given NoResourceFoundException When rota inexistente acessada Then retorna HTTP 404 (API-05/07)")
    void givenNoResourceFoundException_whenHandled_thenReturns404() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "rota/inexistente", "rota/inexistente");
        HttpServletRequest request = new MockHttpServletRequest("GET", "/rota/inexistente");

        ResponseEntity<ApiError> response = handler.handleNoResourceFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("Recurso ou rota não encontrada.");
        assertThat(response.getBody().path()).isEqualTo("/rota/inexistente");
    }

    @Test
    @DisplayName("Given InvalidStateTransitionException When transição de estado inválida Then retorna HTTP 409 Conflict")
    void givenInvalidStateTransitionException_whenHandled_thenReturns409() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException(
                "Apenas apontamentos com status PENDENTE podem ser rejeitados. Status atual: VALIDATED"
        );
        HttpServletRequest request = new MockHttpServletRequest("PUT", "/production-executions/123");

        ResponseEntity<ApiError> response = handler.handleInvalidStateTransition(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).contains("Apenas apontamentos com status PENDENTE podem ser rejeitados");
        assertThat(response.getBody().path()).isEqualTo("/production-executions/123");
    }

    @Test
    @DisplayName("Given ConcurrentUpdateException com objetos de domínio When tratado Then converte current para DTO e oculta password")
    void givenConcurrentUpdateWithDomainObjects_whenHandled_thenSanitizesCurrentAndNeverExposesPassword() throws Exception {
        User farmerUser = UserTestFactory.user().farmer().password("$2a$10$secretHashFarmer").build();
        User validatorUser = UserTestFactory.user().technician().password("$2a$10$secretHashValidator").build();
        Farmer farmer = Farmer.builder()
                .id(UUID.randomUUID())
                .user(farmerUser)
                .createdBy(validatorUser)
                .fullName("Agricultor Teste")
                .cpf("12345678901")
                .build();
        ProductionPlan plan = new ProductionPlan(
                UUID.randomUUID(), farmer, UserTestFactory.harvest(), null,
                new BigDecimal("2.0"), PlanStatus.ACTIVE, null, null, new BigDecimal("1000"),
                null, null, null, null, null, null, null, null
        );
        ProductionExecution execution = new ProductionExecution(
                UUID.randomUUID(), plan, new BigDecimal("10"), new BigDecimal("10"),
                LocalDate.of(2026, 5, 10), ExecutionStatus.VALIDATED, validatorUser,
                LocalDateTime.now(), "ok", null, null, null, null, null, null, null
        );

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        HttpServletRequest request = new MockHttpServletRequest("PUT", "/production-executions/123");

        ResponseEntity<ConflictApiError> execResponse = handler.handleConcurrentUpdate(
                new ConcurrentUpdateException("conflito", execution), request
        );
        ResponseEntity<ConflictApiError> planResponse = handler.handleConcurrentUpdate(
                new ConcurrentUpdateException("conflito", plan), request
        );
        ResponseEntity<ConflictApiError> userResponse = handler.handleConcurrentUpdate(
                new ConcurrentUpdateException("conflito", farmerUser), request
        );

        assertThat(execResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(execResponse.getBody().current()).isInstanceOf(ProductionExecutionResponseDTO.class);
        assertThat(planResponse.getBody().current()).isInstanceOf(ProductionPlanResponseDTO.class);
        assertThat(userResponse.getBody().current()).isInstanceOf(UserResponseDTO.class);

        String execJson = objectMapper.writeValueAsString(execResponse.getBody());
        String planJson = objectMapper.writeValueAsString(planResponse.getBody());
        String userJson = objectMapper.writeValueAsString(userResponse.getBody());
        String rawUserJson = objectMapper.writeValueAsString(farmerUser);

        assertThat(execJson).doesNotContain("password").doesNotContain("secretHash");
        assertThat(planJson).doesNotContain("password").doesNotContain("secretHash");
        assertThat(userJson).doesNotContain("password").doesNotContain("secretHash");
        assertThat(rawUserJson).doesNotContain("password").doesNotContain("secretHash");
    }
}
