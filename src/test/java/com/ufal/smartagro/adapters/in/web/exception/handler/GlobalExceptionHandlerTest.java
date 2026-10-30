package com.ufal.smartagro.adapters.in.web.exception.handler;

import com.ufal.smartagro.adapters.in.web.exception.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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
}
