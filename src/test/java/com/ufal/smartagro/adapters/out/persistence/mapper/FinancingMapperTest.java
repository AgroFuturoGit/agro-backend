package com.ufal.smartagro.adapters.out.persistence.mapper;

import com.ufal.smartagro.adapters.out.persistence.entity.FinancingEntity;
import com.ufal.smartagro.domain.model.Financing;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class FinancingMapperTest {
    private final FinancingMapper mapper = new FinancingMapper(mock(FarmerMapper.class));
    @Test void mapsAmountWithoutLossOfPrecision() {
        Financing domain = Financing.builder().id(UUID.randomUUID()).description("PRONAF").status("ACTIVE").amount(new BigDecimal("12345.67")).build();
        FinancingEntity entity = mapper.toEntity(domain);
        assertThat(entity.getAmount()).isEqualByComparingTo("12345.67"); assertThat(mapper.toDomain(entity).getStatus()).isEqualTo("ACTIVE"); assertThat(mapper.toDomain(null)).isNull();
    }
}
