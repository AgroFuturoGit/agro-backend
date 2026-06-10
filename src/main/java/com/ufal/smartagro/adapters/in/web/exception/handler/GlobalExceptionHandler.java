package com.ufal.smartagro.adapters.in.web.exception.handler;

import com.ufal.smartagro.adapters.in.web.exception.dto.ApiError;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.exception.CpfAlreadyExistsException;
import com.ufal.smartagro.domain.exception.EmailAlreadyExistsException;
import com.ufal.smartagro.domain.exception.HarvestAlreadyExistsException;
import com.ufal.smartagro.domain.exception.HarvestNotFoundException;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Email ou senha inválidos.",
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
    public ResponseEntity<ApiError> handleSpringAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado. Você não tem permissão para realizar esta ação.",
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest httpServletRequest){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                httpServletRequest.getRequestURI(),
                LocalDateTime.now()
        ));
    }
}
