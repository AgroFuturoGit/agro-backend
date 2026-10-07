package com.ufal.smartagro.adapters.in.web.controller;

import com.ufal.smartagro.adapters.in.web.dto.production.ExecutionAttachmentResponseDTO;
import com.ufal.smartagro.application.service.production.*;
import com.ufal.smartagro.config.security.details.UserDetailsImpl;
import com.ufal.smartagro.domain.exception.UserNotFoundException;
import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.port.out.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Tag(name = "Anexos de Apontamento", description = "Comprovações fotográficas anexadas às colheitas registradas em campo")
@RequiredArgsConstructor
@RestController
@RequestMapping("/production-executions/{executionId}/attachments")
public class ExecutionAttachmentController {

    private final UploadExecutionAttachmentUseCase uploadUseCase;
    private final ListExecutionAttachmentsUseCase listUseCase;
    private final DownloadExecutionAttachmentUseCase downloadUseCase;
    private final DeleteExecutionAttachmentUseCase deleteUseCase;
    private final UserRepository userRepository;

    @Operation(
            summary = "Anexar foto a um apontamento",
            description = """
                    Recebe a imagem em multipart/form-data. O campo `clientId` é o identificador
                    gerado no aparelho antes do primeiro envio: informá-lo torna o upload seguro
                    para repetir, porque um reenvio da fila offline devolve o anexo já gravado
                    em vez de criar outro.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anexo registrado"),
            @ApiResponse(responseCode = "200", description = "Anexo já existia para este clientId"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, grande demais ou em formato não aceito"),
            @ApiResponse(responseCode = "403", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Apontamento não encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExecutionAttachmentResponseDTO> upload(
            @PathVariable UUID executionId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "clientId", required = false) UUID clientId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) throws IOException {

        User loggedUser = loadUser(loggedUserDetails);

        var incoming = new NewAttachment(
                clientId,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes());

        UploadResult result = uploadUseCase.upload(executionId, incoming, loggedUser);

        return ResponseEntity
                .status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ExecutionAttachmentResponseDTO.from(result.attachment()));
    }

    @Operation(summary = "Listar anexos de um apontamento", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping
    public ResponseEntity<List<ExecutionAttachmentResponseDTO>> list(
            @PathVariable UUID executionId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loadUser(loggedUserDetails);
        List<ExecutionAttachmentResponseDTO> response =
                listUseCase.listByExecution(executionId, loggedUser).stream()
                        .map(ExecutionAttachmentResponseDTO::from)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Baixar a imagem de um anexo", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TECHNICIAN', 'FARMER')")
    @GetMapping("/{attachmentId}")
    public ResponseEntity<byte[]> download(
            @PathVariable UUID executionId,
            @PathVariable UUID attachmentId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loadUser(loggedUserDetails);
        ExecutionAttachment attachment = downloadUseCase.download(executionId, attachmentId, loggedUser);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + attachment.getFilename() + "\"")
                .body(attachment.getContent());
    }

    @Operation(summary = "Remover um anexo", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN', 'FARMER')")
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID executionId,
            @PathVariable UUID attachmentId,
            @AuthenticationPrincipal UserDetailsImpl loggedUserDetails) {

        User loggedUser = loadUser(loggedUserDetails);
        deleteUseCase.delete(executionId, attachmentId, loggedUser);
        return ResponseEntity.noContent().build();
    }

    private User loadUser(UserDetailsImpl loggedUserDetails) {
        return userRepository.findById(loggedUserDetails.getId())
                .orElseThrow(UserNotFoundException::new);
    }
}
