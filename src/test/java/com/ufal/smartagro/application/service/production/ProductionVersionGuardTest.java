package com.ufal.smartagro.application.service.production;

import com.ufal.smartagro.domain.exception.ConcurrentUpdateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionVersionGuardTest {

    private final ProductionVersionGuard guard = new ProductionVersionGuard();

    private static final LocalDateTime CRIADO = LocalDateTime.of(2026, 9, 1, 8, 0, 0);
    private static final LocalDateTime ATUALIZADO = LocalDateTime.of(2026, 9, 5, 14, 30, 0);
    private static final Object RECURSO = "recurso-do-servidor";

    private void verificar(LocalDateTime base, LocalDateTime atualizadoEm, LocalDateTime criadoEm) {
        guard.ensureUpToDate(base, atualizadoEm, criadoEm, RECURSO, "conflito");
    }

    @Nested
    @DisplayName("quando o cliente não envia a versão-base")
    class SemVersaoBase {

        @Test
        @DisplayName("não verifica nada, preservando o comportamento de quem ainda não adotou o campo")
        void naoVerifica() {
            assertThatCode(() -> verificar(null, ATUALIZADO, CRIADO)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("quando a versão-base coincide com a do servidor")
    class VersaoEmDia {

        @Test
        @DisplayName("aceita a escrita")
        void aceita() {
            assertThatCode(() -> verificar(ATUALIZADO, ATUALIZADO, CRIADO)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("usa a data de criação quando o registro nunca foi atualizado")
        void aceitaRegistroNuncaAtualizado() {
            assertThatCode(() -> verificar(CRIADO, null, CRIADO)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ignora diferença de precisão abaixo do milissegundo")
        void ignoraNanossegundos() {
            LocalDateTime doServidor = ATUALIZADO.withNano(123_456_789);
            LocalDateTime queVoltouDoCliente = ATUALIZADO.withNano(123_000_000);

            assertThatCode(() -> verificar(queVoltouDoCliente, doServidor, CRIADO))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("quando o servidor já avançou além da versão-base")
    class VersaoDefasada {

        @Test
        @DisplayName("recusa a escrita para não sobrescrever a alteração de outro usuário")
        void recusa() {
            assertThatThrownBy(() -> verificar(CRIADO, ATUALIZADO, CRIADO))
                    .isInstanceOf(ConcurrentUpdateException.class)
                    .hasMessage("conflito");
        }

        @Test
        @DisplayName("devolve a versão do servidor junto do erro, para o cliente reconciliar")
        void devolveVersaoDoServidor() {
            assertThatThrownBy(() -> verificar(CRIADO, ATUALIZADO, CRIADO))
                    .asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.type(ConcurrentUpdateException.class))
                    .satisfies(ex -> assertThat(ex.getCurrent()).isEqualTo(RECURSO));
        }

        @Test
        @DisplayName("recusa também quando a base é mais recente que a do servidor")
        void recusaBaseAdiantada() {
            assertThatThrownBy(() -> verificar(ATUALIZADO.plusDays(1), ATUALIZADO, CRIADO))
                    .isInstanceOf(ConcurrentUpdateException.class);
        }
    }

    @Nested
    @DisplayName("quando o servidor não tem versão alguma")
    class SemVersaoNoServidor {

        @Test
        @DisplayName("não bloqueia a escrita")
        void naoBloqueia() {
            assertThatCode(() -> verificar(ATUALIZADO, null, null)).doesNotThrowAnyException();
        }
    }
}
