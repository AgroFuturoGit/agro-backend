package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.FinancingEntity;
import com.ufal.smartagro.domain.model.Farmer;
import com.ufal.smartagro.domain.model.Financing;
import com.ufal.smartagro.domain.model.enums.FinancingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinancingMapperTest {

    @Mock
    private FarmerMapper farmerMapper;

    private FinancingMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FinancingMapper(farmerMapper);
    }

    @Test
    void deveConverterEntityParaDomain() {
        UUID id = UUID.randomUUID();
        FarmerEntity farmerEntity = new FarmerEntity();
        farmerEntity.setId(UUID.randomUUID());
        Farmer farmerDomain = Farmer.builder().id(farmerEntity.getId()).build();

        FinancingEntity entity = new FinancingEntity();
        entity.setId(id);
        entity.setFarmer(farmerEntity);
        entity.setDescription("Custeio Milho");
        entity.setStatus(FinancingStatus.APPROVED);
        entity.setAmount(new BigDecimal("15000.00"));
        entity.setCreatedAt(LocalDateTime.now());

        when(farmerMapper.toDomain(farmerEntity)).thenReturn(farmerDomain);

        Financing domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getDescription()).isEqualTo("Custeio Milho");
        assertThat(domain.getStatus()).isEqualTo(FinancingStatus.APPROVED);
        assertThat(domain.getAmount()).isEqualByComparingTo("15000.00");
    }

    @Test
    void deveConverterDomainParaEntity() {
        UUID id = UUID.randomUUID();
        FarmerEntity farmerEntity = new FarmerEntity();
        farmerEntity.setId(UUID.randomUUID());
        Farmer farmerDomain = Farmer.builder().id(farmerEntity.getId()).build();

        Financing domain = Financing.builder()
                .id(id)
                .farmer(farmerDomain)
                .description("Investimento Trator")
                .status(FinancingStatus.IN_ANALYSIS)
                .amount(new BigDecimal("80000.00"))
                .build();

        when(farmerMapper.toEntity(farmerDomain)).thenReturn(farmerEntity);

        FinancingEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getDescription()).isEqualTo("Investimento Trator");
        assertThat(entity.getStatus()).isEqualTo(FinancingStatus.IN_ANALYSIS);
        assertThat(entity.getAmount()).isEqualByComparingTo("80000.00");
    }
}
