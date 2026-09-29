package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IWheelRepository;
import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelParticipation;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataWheelParticipationRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataWheelPrizeRepository;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataWheelRepository;
import com.fidely.backend.infrastructure.entities.mappers.wheels.WheelMapper;
import com.fidely.backend.infrastructure.entities.mappers.wheels.WheelParticipationMapper;
import com.fidely.backend.infrastructure.entities.mappers.wheels.WheelPrizeMapper;
import com.fidely.backend.infrastructure.entities.models.wheels.WheelParticipationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur permettant au domaine d'accéder aux données des roues
 * de récompenses à travers le port {@link IWheelRepository}.
 *
 * <p>Les différents repositories Spring Data restent des détails
 * d'infrastructure et ne sont pas exposés au domaine.</p>
 */
@Component
public class WheelRepositoryAdapter implements IWheelRepository {

    private final SpringDataWheelRepository wheelRepository;
    private final SpringDataWheelPrizeRepository prizeRepository;
    private final SpringDataWheelParticipationRepository participationRepository;

    private final WheelMapper wheelMapper;
    private final WheelPrizeMapper prizeMapper;
    private final WheelParticipationMapper participationMapper;

    /**
     * Crée un nouvel adaptateur de repository.
     *
     * @param wheelRepository repository Spring Data des roues
     * @param prizeRepository repository Spring Data des lots
     * @param participationRepository repository Spring Data des participations
     */
    public WheelRepositoryAdapter(
            SpringDataWheelRepository wheelRepository,
            SpringDataWheelPrizeRepository prizeRepository,
            SpringDataWheelParticipationRepository participationRepository
    ) {
        this.wheelRepository = wheelRepository;
        this.prizeRepository = prizeRepository;
        this.participationRepository = participationRepository;

        this.wheelMapper = new WheelMapper();
        this.prizeMapper = new WheelPrizeMapper();
        this.participationMapper = new WheelParticipationMapper();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Wheel> findById(UUID wheelId) {
        return wheelRepository.findById(wheelId)
                .map(wheelMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Wheel> findByMerchantId(UUID merchantId) {
        return wheelRepository.findByMerchantId(merchantId)
                .map(wheelMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wheel saveWheel(Wheel wheel) {
        return wheelMapper.toDomain(
                wheelRepository.save(wheelMapper.toEntity(wheel))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WheelPrize> findPrizesByWheelId(UUID wheelId) {
        return prizeRepository.findByWheelId(wheelId)
                .stream()
                .map(prizeMapper::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WheelPrize> findPrizeById(UUID prizeId) {
        return prizeRepository.findById(prizeId)
                .map(prizeMapper::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WheelPrize savePrize(WheelPrize prize) {
        return prizeMapper.toDomain(
                prizeRepository.save(prizeMapper.toEntity(prize))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePrizeById(UUID prizeId) {
        prizeRepository.deleteById(prizeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WheelParticipation> findParticipationById(UUID participationId) {
        return participationRepository.findById(participationId)
                .map(this::toDomainParticipation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WheelParticipation> findParticipationsByCustomerId(UUID customerId) {
        return participationRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toDomainParticipation)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<WheelParticipation> findLatestParticipation(
            UUID customerId,
            UUID wheelId
    ) {
        return participationRepository
                .findTopByCustomerIdAndWheelIdOrderByPlayedAtDesc(customerId, wheelId)
                .map(this::toDomainParticipation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WheelParticipation saveParticipation(WheelParticipation participation) {
        return participationMapper.toDomain(
                participationRepository.save(
                        participationMapper.toEntity(participation)
                ),
                participation.getWheelPrize()
        );
    }

    /**
     * Reconstruit une participation du domaine à partir de son entité JPA.
     *
     * <p>Le lot est stocké uniquement sous la forme de son identifiant
     * dans l'entité de participation. Il est donc chargé séparément.</p>
     *
     * @param entity entité JPA de la participation
     * @return participation du domaine
     */
    private WheelParticipation toDomainParticipation(
            WheelParticipationEntity entity
    ) {
        WheelPrize wheelPrize = null;

        if (entity.getWheelPrizeId() != null) {
            wheelPrize = prizeRepository.findById(entity.getWheelPrizeId())
                    .map(prizeMapper::toDomain)
                    .orElse(null);
        }

        return participationMapper.toDomain(entity, wheelPrize);
    }
}