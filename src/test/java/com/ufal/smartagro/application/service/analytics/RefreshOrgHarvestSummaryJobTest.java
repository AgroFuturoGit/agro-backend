package com.ufal.smartagro.application.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshOrgHarvestSummaryJobTest {

    private static final long ADVISORY_LOCK_ID = 88234710293847L;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RefreshOrgHarvestSummaryJob job;

    @Test
    @DisplayName("Quando adquire o lock, deve executar refresh concorrente e liberar o lock no finally")
    void whenLockAcquired_shouldExecuteRefreshAndReleaseLock() {
        when(jdbcTemplate.queryForObject(eq("SELECT pg_try_advisory_lock(?)"), eq(Boolean.class), eq(ADVISORY_LOCK_ID)))
                .thenReturn(Boolean.TRUE);

        job.refreshMaterializedView();

        verify(jdbcTemplate).execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_org_harvest_summary");
        verify(jdbcTemplate).queryForObject(eq("SELECT pg_advisory_unlock(?)"), eq(Boolean.class), eq(ADVISORY_LOCK_ID));
    }

    @Test
    @DisplayName("Quando não adquire o lock, não deve executar refresh nem liberar lock")
    void whenLockNotAcquired_shouldSkipExecutionAndNotUnlock() {
        when(jdbcTemplate.queryForObject(eq("SELECT pg_try_advisory_lock(?)"), eq(Boolean.class), eq(ADVISORY_LOCK_ID)))
                .thenReturn(Boolean.FALSE);

        job.refreshMaterializedView();

        verify(jdbcTemplate, never()).execute(anyString());
        verify(jdbcTemplate, never()).queryForObject(eq("SELECT pg_advisory_unlock(?)"), eq(Boolean.class), anyLong());
    }

    @Test
    @DisplayName("Quando refresh falha com exceção, não deve propagar o erro e deve liberar o lock no finally")
    void whenRefreshFails_shouldNotRethrowAndReleaseLock() {
        when(jdbcTemplate.queryForObject(eq("SELECT pg_try_advisory_lock(?)"), eq(Boolean.class), eq(ADVISORY_LOCK_ID)))
                .thenReturn(Boolean.TRUE);
        doThrow(new DataAccessException("Connection reset") {})
                .when(jdbcTemplate).execute(anyString());

        assertThatCode(() -> job.refreshMaterializedView())
                .doesNotThrowAnyException();

        verify(jdbcTemplate).queryForObject(eq("SELECT pg_advisory_unlock(?)"), eq(Boolean.class), eq(ADVISORY_LOCK_ID));
    }
}
