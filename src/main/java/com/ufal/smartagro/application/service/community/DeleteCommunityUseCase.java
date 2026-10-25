package com.ufal.smartagro.application.service.community;

import com.ufal.smartagro.domain.exception.EntityInUseException;
import com.ufal.smartagro.domain.exception.EntityNotFoundException;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCommunityUseCase {

    private final CommunityRepository communityRepository;
    private final FarmerRepository farmerRepository;

    @Transactional
    public void delete(UUID id) {
        communityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comunidade não encontrada."));

        int farmerCount = farmerRepository.findAllByCommunityId(id).size();
        if (farmerCount > 0) {
            throw new EntityInUseException(
                    "Não é possível excluir uma comunidade que possui " + farmerCount + " agricultores vinculados."
            );
        }

        communityRepository.delete(id);
    }
}
