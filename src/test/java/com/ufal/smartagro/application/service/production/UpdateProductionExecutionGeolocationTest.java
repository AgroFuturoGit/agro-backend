package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.adapters.in.web.dto.production.ProductionExecutionUpdateDTO;
import com.ufal.smartagro.domain.model.ProductionExecution;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("coordenadas na atualização do apontamento")
class UpdateProductionExecutionGeolocationTest {

    @Mock
    private ProductionExecutionRepository repository;

    @Mock
    private ProductionAccessValidator accessValidator;

    private UpdateProductionExecutionUseCase useCase;

    private static final UUID ID = UUID.randomUUID();
    private static final BigDecimal LAT_ORIGINAL = new BigDecimal("-9.752100");
    private static final BigDecimal LON_ORIGINAL = new BigDecimal("-36.661200");
    private static final BigDecimal ACCURACY_ORIGINAL = new BigDecimal("8.00");
    private static final LocalDateTime CAPTURA_ORIGINAL =
            LocalDateTime.of(2026, 8, 1, 9, 30);
    private static final LocalDateTime NOVA_CAPTURA =
            LocalDateTime.of(2026, 9, 17, 14, 0);

    @BeforeEach
    void setUp() {
        useCase = new UpdateProductionExecutionUseCase(repository, accessValidator);
    }

    /** O acesso é validado por um colaborador mockado; o papel aqui é indiferente. */
    private User usuario() {
        return new User(UUID.randomUUID(), "Fulano", "fulano@ufal.br", "senha",
                "00000000000", LocalDate.of(1990, 1, 1), Role.FARMER);
    }

    private ProductionExecution existente(BigDecimal latitude, BigDecimal longitude) {
        return new ProductionExecution(
                ID, null, new BigDecimal("30.00"), LocalDate.of(2026, 8, 1),
                latitude, longitude, ACCURACY_ORIGINAL, CAPTURA_ORIGINAL,
                null, null, null
        );
    }

    private ProductionExecution atualizar(
            ProductionExecution atual,
            ProductionExecutionUpdateDTO dto
    ) {
        when(repository.findById(ID)).thenReturn(Optional.of(atual));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.update(ID, dto, usuario());

        ArgumentCaptor<ProductionExecution> captor =
                ArgumentCaptor.forClass(ProductionExecution.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("uma edição sem GPS não apaga a posição já registrada em campo")
    void preservaCoordenadaQuandoEdicaoNaoTrazGps() {
        ProductionExecution resultado = atualizar(
                existente(LAT_ORIGINAL, LON_ORIGINAL),
                new ProductionExecutionUpdateDTO(new BigDecimal("45.00"), null, null, null, null, null, null)
        );

        assertThat(resultado.getLatitude()).isEqualByComparingTo(LAT_ORIGINAL);
        assertThat(resultado.getLongitude()).isEqualByComparingTo(LON_ORIGINAL);
        assertThat(resultado.getLocationAccuracy()).isEqualByComparingTo(ACCURACY_ORIGINAL);
        assertThat(resultado.getLocationRecordedAt()).isEqualTo(CAPTURA_ORIGINAL);
        assertThat(resultado.getActualYield()).isEqualByComparingTo("45.00");
    }

    @Test
    @DisplayName("uma edição com GPS substitui a posição anterior")
    void substituiCoordenadaQuandoEdicaoTrazGps() {
        BigDecimal novaLat = new BigDecimal("-9.800000");
        BigDecimal novaLon = new BigDecimal("-36.700000");

        ProductionExecution resultado = atualizar(
                existente(LAT_ORIGINAL, LON_ORIGINAL),
                new ProductionExecutionUpdateDTO(null, null, novaLat, novaLon, new BigDecimal("5.00"), NOVA_CAPTURA, null)
        );

        assertThat(resultado.getLatitude()).isEqualByComparingTo(novaLat);
        assertThat(resultado.getLongitude()).isEqualByComparingTo(novaLon);
        // A recaptura substitui a leitura inteira: manter a precisão e o carimbo
        // antigos descreveria a nova posição com a confiança da anterior.
        assertThat(resultado.getLocationAccuracy()).isEqualByComparingTo("5.00");
        assertThat(resultado.getLocationRecordedAt()).isEqualTo(NOVA_CAPTURA);
    }

    @Test
    @DisplayName("clearLocation apaga a posição inteira, e não só o par de coordenadas")
    void apagaLocalizacaoQuandoPedido() {
        ProductionExecution resultado = atualizar(
                existente(LAT_ORIGINAL, LON_ORIGINAL),
                new ProductionExecutionUpdateDTO(null, null, null, null, null, null, true)
        );

        assertThat(resultado.getLatitude()).isNull();
        assertThat(resultado.getLongitude()).isNull();
        assertThat(resultado.getLocationAccuracy()).isNull();
        assertThat(resultado.getLocationRecordedAt()).isNull();
    }

    @Test
    @DisplayName("clearLocation não interfere nos demais campos da edição")
    void apagarLocalizacaoPreservaOResto() {
        ProductionExecution resultado = atualizar(
                existente(LAT_ORIGINAL, LON_ORIGINAL),
                new ProductionExecutionUpdateDTO(
                        new BigDecimal("50.00"), null, null, null, null, null, true)
        );

        assertThat(resultado.getActualYield()).isEqualByComparingTo("50.00");
        assertThat(resultado.getLatitude()).isNull();
    }

    @Test
    @DisplayName("apontamento que nunca teve GPS continua sem coordenada")
    void mantemAusenciaDeCoordenada() {
        ProductionExecution resultado = atualizar(
                existente(null, null),
                new ProductionExecutionUpdateDTO(new BigDecimal("10.00"), null, null, null, null, null, null)
        );

        assertThat(resultado.getLatitude()).isNull();
        assertThat(resultado.getLongitude()).isNull();
    }
}
