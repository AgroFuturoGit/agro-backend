package com.ufal.smartagro.application.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshOrgHarvestSummaryJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshOrgHarvestSummaryJob.class);
    private static final long ADVISORY_LOCK_ID = 88234710293847L;

    private final JdbcTemplate jdbcTemplate;

    public RefreshOrgHarvestSummaryJob(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedDelayString = "${app.analytics.refresh.interval-ms:300000}")
    public void refreshMaterializedView() {
        Boolean lockAcquired = jdbcTemplate.queryForObject(
                "SELECT pg_try_advisory_lock(?)", Boolean.class, ADVISORY_LOCK_ID);

        if (Boolean.FALSE.equals(lockAcquired)) {
            log.debug("Outra instância já está atualizando a view materializada. Ciclo ignorado.");
            return;
        }

        long start = System.currentTimeMillis();
        try {
            log.info("Iniciando REFRESH MATERIALIZED VIEW CONCURRENTLY mv_org_harvest_summary...");
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_org_harvest_summary");
            log.info("Refresh da mv_org_harvest_summary concluído com sucesso em {} ms.", System.currentTimeMillis() - start);
        } catch (Exception ex) {
            log.error("Erro durante o refresh concorrente da mv_org_harvest_summary: {}", ex.getMessage(), ex);
        } finally {
            jdbcTemplate.queryForObject("SELECT pg_advisory_unlock(?)", Boolean.class, ADVISORY_LOCK_ID);
        }
    }
}
