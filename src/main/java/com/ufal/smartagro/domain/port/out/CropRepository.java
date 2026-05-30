package com.ufal.smartagro.domain.port.out;

import com.ufal.smartagro.domain.model.Crop;

public interface CropRepository {
    Crop save(Crop crop);
}
