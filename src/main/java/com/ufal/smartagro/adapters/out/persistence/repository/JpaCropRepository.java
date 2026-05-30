package com.ufal.smartagro.adapters.out.persistence.repository;

import com.ufal.smartagro.adapters.out.persistence.entity.CropEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaCropRepository extends JpaRepository<CropEntity, UUID> {
    boolean existsByNameAndVariety(String name, String variety);
}
