package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.model.ExecutionAttachment;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.domain.port.out.ExecutionAttachmentRepository;
import com.ufal.smartagro.domain.port.out.ProductionExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("anexo fotográfico do apontamento")
class UploadExecutionAttachmentTest {

    @Mock
    private ExecutionAttachmentRepository attachmentRepository;

    @Mock
    private ProductionExecutionRepository executionRepository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private UploadExecutionAttachmentUseCase useCase;

    private static final UUID EXECUTION_ID = UUID.randomUUID();
    private static final UUID CLIENT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new UploadExecutionAttachmentUseCase(
                attachmentRepository, executionRepository, accessValidator);
    }

    private User usuario() {
        return new User(UUID.randomUUID(), "Fulano", "fulano@ufal.br", "senha",
                "00000000000", LocalDate.of(1990, 1, 1), Role.FARMER);
    }

    /**
     * O plano vai nulo: quem decide o acesso é o validador, aqui mockado, e
     * montar a cadeia plano → agricultor só afastaria o teste do que ele mede.
     */
    private ProductionExecution apontamento() {
        return new ProductionExecution(EXECUTION_ID, null, new BigDecimal("500"),
                LocalDate.of(2026, 9, 15), null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now(), null);
    }

    private NewExecutionAttachment foto(UUID clientId, String contentType, int bytes) {
        return new NewExecutionAttachment(clientId, "colheita.jpg", contentType, new byte[bytes]);
    }

    private void apontamentoExiste() {
        when(executionRepository.findById(EXECUTION_ID)).thenReturn(Optional.of(apontamento()));
    }

    private ExecutionAttachment gravado(UUID clientId) {
        return new ExecutionAttachment(UUID.randomUUID(), EXECUTION_ID, clientId,
                "colheita.jpg", "image/jpeg", 1024, null, LocalDateTime.now());
    }

    @Test
    @DisplayName("grava a foto e devolve o anexo criado")
    void gravaFoto() {
        apontamentoExiste();
        when(attachmentRepository.findAllByExecutionId(EXECUTION_ID)).thenReturn(List.of());
        when(attachmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AttachmentUploadResult resultado = useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "image/jpeg", 1024), usuario());

        assertThat(resultado.created()).isTrue();

        ArgumentCaptor<ExecutionAttachment> captor = ArgumentCaptor.forClass(ExecutionAttachment.class);
        verify(attachmentRepository).save(captor.capture());
        assertThat(captor.getValue().getSizeBytes()).isEqualTo(1024);
        assertThat(captor.getValue().getClientId()).isEqualTo(CLIENT_ID);
        assertThat(captor.getValue().getProductionExecutionId()).isEqualTo(EXECUTION_ID);
    }

    @Test
    @DisplayName("o reenvio da fila devolve o anexo já gravado, sem duplicar")
    void reenvioNaoDuplica() {
        apontamentoExiste();
        ExecutionAttachment existente = gravado(CLIENT_ID);
        when(attachmentRepository.findByExecutionIdAndClientId(EXECUTION_ID, CLIENT_ID))
                .thenReturn(Optional.of(existente));

        AttachmentUploadResult resultado = useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "image/jpeg", 1024), usuario());

        assertThat(resultado.created()).isFalse();
        assertThat(resultado.attachment().getId()).isEqualTo(existente.getId());
        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("sem clientId não há como deduplicar, e cada envio grava")
    void semClientIdGravaSempre() {
        apontamentoExiste();
        when(attachmentRepository.findAllByExecutionId(EXECUTION_ID)).thenReturn(List.of());
        when(attachmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AttachmentUploadResult resultado = useCase.upload(EXECUTION_ID, foto(null, "image/jpeg", 1024), usuario());

        assertThat(resultado.created()).isTrue();
        verify(attachmentRepository, never()).findByExecutionIdAndClientId(any(), any());
    }

    @Test
    @DisplayName("recusa formato que o produto não sabe exibir")
    void recusaFormato() {
        apontamentoExiste();

        assertThatThrownBy(() -> useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "application/pdf", 1024), usuario()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Formato não aceito");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa arquivo vazio")
    void recusaVazio() {
        apontamentoExiste();

        assertThatThrownBy(() -> useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "image/jpeg", 0), usuario()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    @DisplayName("recusa imagem acima do teto de 5 MB")
    void recusaGrande() {
        apontamentoExiste();
        int acimaDoTeto = (int) UploadExecutionAttachmentUseCase.MAX_SIZE_BYTES + 1;

        assertThatThrownBy(() -> useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "image/jpeg", acimaDoTeto), usuario()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 MB");
    }

    @Test
    @DisplayName("recusa o sexto anexo do mesmo apontamento")
    void recusaAcimaDoTeto() {
        apontamentoExiste();
        when(attachmentRepository.findAllByExecutionId(EXECUTION_ID))
                .thenReturn(List.of(gravado(null), gravado(null), gravado(null), gravado(null), gravado(null)));

        assertThatThrownBy(() -> useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "image/jpeg", 1024), usuario()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("máximo permitido");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("recusa anexo para apontamento que não existe")
    void recusaApontamentoInexistente() {
        when(executionRepository.findById(EXECUTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.upload(EXECUTION_ID, foto(CLIENT_ID, "image/jpeg", 1024), usuario()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Apontamento de colheita não encontrado");
    }
}
