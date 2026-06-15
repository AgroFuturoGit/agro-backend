package com.ufal.smartagro.application.service.producer;

import com.ufal.smartagro.adapters.in.web.dto.community.CommunityResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.organization.OrganizationResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerRegisterDTO;
import com.ufal.smartagro.adapters.in.web.dto.producer.ProducerResponseDTO;
import com.ufal.smartagro.adapters.in.web.dto.user.UserRegisterDTO;
import com.ufal.smartagro.adapters.in.web.mapper.Mapper;
import com.ufal.smartagro.domain.exception.AccessDeniedException;
import com.ufal.smartagro.domain.model.Community;
import com.ufal.smartagro.domain.model.Producer;
import com.ufal.smartagro.domain.model.User;
import com.ufal.smartagro.domain.model.enums.Role;
import com.ufal.smartagro.application.service.user.UserRegisterUseCase;
import com.ufal.smartagro.domain.port.out.CommunityRepository;
import com.ufal.smartagro.domain.port.out.ProducerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegisterProducerUseCase {

    private final ProducerRepository producerRepository;
    private final CommunityRepository communityRepository;
    private final UserRegisterUseCase userRegisterUseCase;

    @Transactional
    public ProducerResponseDTO register(UUID communityId, ProducerRegisterDTO dto, User loggedUser) {
        if (loggedUser.getRole() != Role.ADMIN && loggedUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException();
        }

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("Comunidade não encontrada"));

        UserRegisterDTO userDto = new UserRegisterDTO(
                dto.fullName(), dto.email(), dto.password(), dto.cpf(), dto.dateOfBirth(), Role.PRODUCER
        );
        User savedUser = userRegisterUseCase.createBaseUser(userDto);

        Producer producer = new Producer(
                null,
                savedUser,
                community,
                dto.aliasName(),
                true,
                null,
                null,
                null
        );

        Producer savedProducer = producerRepository.save(producer);

        return new ProducerResponseDTO(
                savedProducer.getId(),
                Mapper.toUserResponseDTO(savedUser),
                new CommunityResponseDTO(
                        community.getId(),
                        community.getName(),
                        new OrganizationResponseDTO(
                                community.getOrganization().getId(),
                                community.getOrganization().getName(),
                                community.getOrganization().getTaxId(),
                                community.getOrganization().getType(),
                                community.getOrganization().getCreatedAt(),
                                community.getOrganization().getUpdatedAt()
                        ),
                        community.getCreatedAt(),
                        community.getUpdatedAt()
                ),
                savedProducer.getAliasName(),
                savedProducer.getIsCompliant(),
                savedProducer.getCreatedAt(),
                savedProducer.getUpdatedAt()
        );
    }
}
