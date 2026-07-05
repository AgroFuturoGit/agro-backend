package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Producer;
import java.util.Optional;
import java.util.UUID;

public interface ProducerRepository {
    Producer save(Producer producer);
    Optional<Producer> findById(UUID id);
    Optional<Producer> findByUserId(UUID userId);
    java.util.List<Producer> findAll();
    java.util.List<Producer> findAllByCommunityId(UUID communityId);
    void delete(UUID id);
}
