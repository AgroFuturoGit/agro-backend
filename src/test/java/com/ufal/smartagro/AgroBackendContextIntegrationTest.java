package com.ufal.smartagro;

import com.ufal.smartagro.application.service.crop.CropUpdateUseCase;
import com.ufal.smartagro.application.service.harvest.HarvestUpdateUseCase;
import com.ufal.smartagro.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Smoke Test — Inicialização Completa do ApplicationContext")
class AgroBackendContextIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CropUpdateUseCase cropUpdateUseCase;

    @Autowired
    private HarvestUpdateUseCase harvestUpdateUseCase;

    @Test
    @DisplayName("Contexto do Spring deve subir com sucesso e instanciar os use cases principais")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(cropUpdateUseCase).isNotNull();
        assertThat(harvestUpdateUseCase).isNotNull();
    }
}
