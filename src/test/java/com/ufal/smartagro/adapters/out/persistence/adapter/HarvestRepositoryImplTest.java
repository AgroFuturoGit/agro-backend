package com.ufal.smartagro.adapters.out.persistence.adapter;

import com.ufal.smartagro.adapters.out.persistence.entity.HarvestEntity;
import com.ufal.smartagro.adapters.out.persistence.mapper.HarvestMapper;
import com.ufal.smartagro.adapters.out.persistence.repository.JpaHarvestRepository;
import com.ufal.smartagro.domain.model.Harvest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestRepositoryImplTest {

    @Mock
    private JpaHarvestRepository jpaHarvestRepository;

    @Mock
    private HarvestMapper harvestMapper;

    @InjectMocks
    private HarvestRepositoryImpl harvestRepository;

    private UUID harvestId;
    private HarvestEntity entity;
    private Harvest domain;

    @BeforeEach
    void setUp() {
        harvestId = UUID.randomUUID();
        entity = new HarvestEntity();
        entity.setId(harvestId);
        entity.setLabel("Safra 2026");
        entity.setStartDate(LocalDate.of(2026, 1, 1));
        entity.setEndDate(LocalDate.of(2026, 12, 31));

        domain = new Harvest(harvestId, "Safra 2026", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
    }

    @Test
    @DisplayName("findActiveHarvest: deve retornar a safra ativa mais recente quando existir")
    void findActiveHarvest_quandoExiste_deveRetornarSafra() {
        LocalDate date = LocalDate.of(2026, 6, 15);
        when(jpaHarvestRepository.findActiveHarvests(date)).thenReturn(List.of(entity));
        when(harvestMapper.toDomain(entity)).thenReturn(domain);

        Optional<Harvest> result = harvestRepository.findActiveHarvest(date);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(harvestId);
        assertThat(result.get().getLabel()).isEqualTo("Safra 2026");
        verify(jpaHarvestRepository).findActiveHarvests(date);
    }

    @Test
    @DisplayName("findActiveHarvest: quando data for nula deve usar LocalDate.now()")
    void findActiveHarvest_quandoDataNula_deveUsarDataAtual() {
        when(jpaHarvestRepository.findActiveHarvests(any(LocalDate.class))).thenReturn(List.of(entity));
        when(harvestMapper.toDomain(entity)).thenReturn(domain);

        Optional<Harvest> result = harvestRepository.findActiveHarvest(null);

        assertThat(result).isPresent();
        verify(jpaHarvestRepository).findActiveHarvests(argThat(d -> d != null));
    }

    @Test
    @DisplayName("findActiveHarvest: quando nenhuma safra ativa deve retornar Optional.empty()")
    void findActiveHarvest_quandoNaoExiste_deveRetornarEmpty() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        when(jpaHarvestRepository.findActiveHarvests(date)).thenReturn(List.of());

        Optional<Harvest> result = harvestRepository.findActiveHarvest(date);

        assertThat(result).isEmpty();
        verify(jpaHarvestRepository).findActiveHarvests(date);
        verifyNoInteractions(harvestMapper);
    }

    @Test
    @DisplayName("save: deve mapear, salvar e retornar o domínio")
    void save_deveMapearESalvar() {
        when(harvestMapper.toEntity(domain)).thenReturn(entity);
        when(jpaHarvestRepository.save(entity)).thenReturn(entity);
        when(harvestMapper.toDomain(entity)).thenReturn(domain);

        Harvest result = harvestRepository.save(domain);

        assertThat(result).isEqualTo(domain);
        verify(jpaHarvestRepository).save(entity);
    }

    @Test
    @DisplayName("findById: deve retornar o domínio quando encontrado")
    void findById_quandoEncontrado_deveRetornarDominio() {
        when(jpaHarvestRepository.findById(harvestId)).thenReturn(Optional.of(entity));
        when(harvestMapper.toDomain(entity)).thenReturn(domain);

        Optional<Harvest> result = harvestRepository.findById(harvestId);

        assertThat(result).contains(domain);
    }

    @Test
    @DisplayName("findAll: deve retornar lista de safras")
    void findAll_deveRetornarLista() {
        when(jpaHarvestRepository.findAll()).thenReturn(List.of(entity));
        when(harvestMapper.toDomain(entity)).thenReturn(domain);

        List<Harvest> result = harvestRepository.findAll();

        assertThat(result).containsExactly(domain);
    }

    @Test
    @DisplayName("existsByLabel: deve delegar ao JpaHarvestRepository")
    void existsByLabel_deveDelegar() {
        when(jpaHarvestRepository.existsByLabel("Safra 2026")).thenReturn(true);

        boolean exists = harvestRepository.existsByLabel("Safra 2026");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("deleteById: deve chamar softDeleteById no jpaHarvestRepository")
    void deleteById_deveChamarSoftDelete() {
        harvestRepository.deleteById(harvestId);

        verify(jpaHarvestRepository).softDeleteById(harvestId);
    }
}
