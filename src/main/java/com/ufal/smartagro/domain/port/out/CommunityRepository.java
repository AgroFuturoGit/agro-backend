package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Community;
import java.util.Optional;
import java.util.UUID;

public interface CommunityRepository {
    Community save(Community community);
    Optional<Community> findById(UUID id);
}
