package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FamilyCompositionEntity;
import com.ufal.smartagro.adapters.out.persistence.entity.FarmerEntity;
import com.ufal.smartagro.domain.model.FamilyComposition;
import com.ufal.smartagro.domain.model.Farmer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyCompositionMapperTest {

    @Mock
    private FarmerMapper farmerMapper;

    private FamilyCompositionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FamilyCompositionMapper(farmerMapper);
    }

    @Test
    void deveConverterEntityParaDomain() {
        UUID id = UUID.randomUUID();
        FarmerEntity farmerEntity = new FarmerEntity();
        farmerEntity.setId(UUID.randomUUID());
        Farmer farmerDomain = Farmer.builder().id(farmerEntity.getId()).build();

        FamilyCompositionEntity entity = new FamilyCompositionEntity();
        entity.setId(id);
        entity.setFarmer(farmerEntity);
        entity.setName("Filho Um");
        entity.setKinship("Filho");
        entity.setAge(16);
        entity.setCreatedAt(LocalDateTime.now());

        when(farmerMapper.toDomain(farmerEntity)).thenReturn(farmerDomain);

        FamilyComposition domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getName()).isEqualTo("Filho Um");
        assertThat(domain.getKinship()).isEqualTo("Filho");
        assertThat(domain.getAge()).isEqualTo(16);
    }

    @Test
    void deveConverterDomainParaEntity() {
        UUID id = UUID.randomUUID();
        FarmerEntity farmerEntity = new FarmerEntity();
        farmerEntity.setId(UUID.randomUUID());
        Farmer farmerDomain = Farmer.builder().id(farmerEntity.getId()).build();

        FamilyComposition domain = FamilyComposition.builder()
                .id(id)
                .farmer(farmerDomain)
                .name("Filho Dois")
                .kinship("Filho")
                .age(18)
                .build();

        when(farmerMapper.toEntity(farmerDomain)).thenReturn(farmerEntity);

        FamilyCompositionEntity entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Filho Dois");
        assertThat(entity.getKinship()).isEqualTo("Filho");
        assertThat(entity.getAge()).isEqualTo(18);
    }
}
