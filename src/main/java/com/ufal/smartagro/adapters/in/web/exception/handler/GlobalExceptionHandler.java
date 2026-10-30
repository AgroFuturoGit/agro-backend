package com.ufal.smartagro.adapters.in.web.exception.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ufal.smartagro.adapters.in.web.exception.dto.ApiError;
import com.ufal.smartagro.adapters.in.web.exception.dto.ConflictApiError;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.ConcurrentUpdateException;
import com.ufal.smartagro.domain.exception.CpfAlreadyExistsException;
import com.ufal.smartagro.domain.exception.CropAlreadyExistsException;
import com.ufal.smartagro.domain.exception.CropNotFoundException;
import com.ufal.smartagro.domain.exception.EmailAlreadyExistsException;
import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.exception.InvalidStateTransitionException;
import com.ufal.smartagro.domain.exception.RateLimitExceededException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(
            NoSuchElementException ex, HttpServletRequest httpServletRequest){
        String message = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Recurso não encontrado.";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                HttpStatus.NOT_FOUND.value(),
                message,
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(CropNotFoundException.class)
    public ResponseEntity<ApiError> handleCropNotFound(
            CropNotFoundException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(HarvestNotFoundException.class)
    public ResponseEntity<ApiError> handleHarvestNotFound(
            HarvestNotFoundException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleSpringSecurityAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado. Você não tem permissão para realizar esta ação.",
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciais inválidas.",
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidStateTransition(
            InvalidStateTransitionException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(EntityInUseException.class)
    public ResponseEntity<ApiError> handleEntityInUse(
            EntityInUseException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCpfAlreadyExists(
            CpfAlreadyExistsException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(CropAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleCropAlreadyExists(
            CropAlreadyExistsException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(HarvestAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleHarvestAlreadyExists(
            HarvestAlreadyExistsException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(ConcurrentUpdateException.class)
    public ResponseEntity<ConflictApiError> handleConcurrentUpdate(
            ConcurrentUpdateException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ConflictApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now(),
                ex.getCurrent()
        ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(
            RateLimitExceededException ex, HttpServletRequest httpServletRequest) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(new ApiError(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        ex.getMessage(),
                        httpServletRequest.getRequestURI(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest httpServletRequest) {
        log.warn("Arquivo excede o tamanho máximo permitido ao acessar {}: {}", httpServletRequest.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiError(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "O arquivo enviado excede o tamanho máximo permitido de 5 MB.",
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest){
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                message,
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest httpServletRequest){
        String message = "Corpo da requisição inválido ou com formato incorreto.";
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            message = "Formato inválido para o campo: " + field;
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                message,
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest httpServletRequest){
        String message = String.format("Valor inválido para o parâmetro '%s'.", ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                message,
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest httpServletRequest){
        String message = String.format("Parâmetro obrigatório ausente: %s", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                message,
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest httpServletRequest){
        String msg = ex.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase(Locale.ROOT);
            if (lower.contains("não encontrad") || lower.contains("nao encontrad") || lower.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        msg,
                        httpServletRequest.getRequestURI(),
                        LocalDateTime.now()
                ));
            }
            if (lower.contains("excede o limite") || lower.contains("grande demais")) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiError(
                        HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        msg,
                        httpServletRequest.getRequestURI(),
                        LocalDateTime.now()
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                msg,
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ApiError(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest httpServletRequest){
        log.warn("Violação de integridade de dados ao acessar {}: {}", httpServletRequest.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                HttpStatus.CONFLICT.value(),
                "Operação conflita com dados existentes no sistema.",
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest httpServletRequest){
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[traceId={}] Erro interno não tratado ao acessar {}", traceId, httpServletRequest.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno inesperado no servidor. (Trace ID: " + traceId + ")",
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }
}
