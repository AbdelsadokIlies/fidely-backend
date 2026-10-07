package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IWheelService;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.application.port.out.IWheelRepository;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import com.fidely.backend.domain.models.wheels.Wheel;
import com.fidely.backend.domain.models.wheels.WheelPrize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service métier permettant de gérer les roues de récompenses.
 */
@Service
public class WheelService implements IWheelService {

    private final IWheelRepository wheelRepository;
    private final IUserRepository userRepository;

    /**
     * Crée un nouveau service de gestion des roues.
     *
     * @param wheelRepository repository des roues
     * @param userRepository repository des utilisateurs
     */
    public WheelService(
            IWheelRepository wheelRepository,
            IUserRepository userRepository
    ) {
        this.wheelRepository = wheelRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wheel getWheelByMerchantId(UUID merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("merchantId must not be null");
        }

        return wheelRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Wheel not found"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wheel getWheelForManager(UUID userId) {
        UUID merchantId = getMerchantIdForManager(userId);

        return getWheelByMerchantId(merchantId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wheel updateWheelForManager(
            UUID userId,
            String name,
            boolean active,
            int minIntervalMinutes,
            boolean requiresValidatedPurchase
    ) {
        UUID merchantId = getMerchantIdForManager(userId);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if (minIntervalMinutes < 0) {
            throw new IllegalArgumentException(
                    "minIntervalMinutes must not be negative"
            );
        }

        Wheel existingWheel = wheelRepository.findByMerchantId(merchantId)
                .orElse(null);

        Wheel wheel;

        if (existingWheel == null) {
            wheel = new Wheel(
                    UUID.randomUUID(),
                    merchantId,
                    name,
                    active,
                    minIntervalMinutes,
                    requiresValidatedPurchase,
                    LocalDateTime.now()
            );
        } else {
            wheel = new Wheel(
                    existingWheel.getId(),
                    merchantId,
                    name,
                    active,
                    minIntervalMinutes,
                    requiresValidatedPurchase,
                    existingWheel.getCreatedAt()
            );
        }

        return wheelRepository.saveWheel(wheel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<WheelPrize> getPrizes(UUID wheelId) {
        if (wheelId == null) {
            throw new IllegalArgumentException("wheelId must not be null");
        }

        return wheelRepository.findPrizesByWheelId(wheelId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WheelPrize savePrizeForManager(
            UUID userId,
            String label,
            int probabilityWeight
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId must not be null"
            );
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "label must not be blank"
            );
        }

        if (probabilityWeight < 0) {
            throw new IllegalArgumentException(
                    "probabilityWeight must not be negative"
            );
        }

        UUID merchantId = getMerchantIdForManager(userId);

        Wheel wheel = wheelRepository.findByMerchantId(merchantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wheel not found")
                );

        WheelPrize prize = new WheelPrize(
                UUID.randomUUID(),
                wheel.getId(),
                label,
                probabilityWeight,
                LocalDateTime.now()
        );

        return wheelRepository.savePrize(prize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WheelPrize updatePrizeForManager(
            UUID userId,
            UUID prizeId,
            String label,
            int probabilityWeight
    ) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId must not be null"
            );
        }

        if (prizeId == null) {
            throw new IllegalArgumentException(
                    "prizeId must not be null"
            );
        }

        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException(
                    "label must not be blank"
            );
        }

        if (probabilityWeight < 0) {
            throw new IllegalArgumentException(
                    "probabilityWeight must not be negative"
            );
        }

        UUID merchantId = getMerchantIdForManager(userId);

        Wheel wheel = wheelRepository.findByMerchantId(merchantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Wheel not found")
                );

        WheelPrize existingPrize = wheelRepository
                .findPrizesByWheelId(wheel.getId())
                .stream()
                .filter(prize -> prize.getId().equals(prizeId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Prize not found")
                );

        WheelPrize updatedPrize = new WheelPrize(
                existingPrize.getId(),
                existingPrize.getWheelId(),
                label,
                probabilityWeight,
                existingPrize.getCreatedAt()
        );

        return wheelRepository.savePrize(updatedPrize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePrizeForManager(UUID userId, UUID prizeId) {
        if (prizeId == null) {
            throw new IllegalArgumentException("prizeId must not be null");
        }

        UUID merchantId = getMerchantIdForManager(userId);

        Wheel wheel = wheelRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Wheel not found"));

        WheelPrize prize = wheelRepository.findPrizeById(prizeId)
                .orElseThrow(() -> new IllegalArgumentException("Prize not found"));

        if (!wheel.getId().equals(prize.getWheelId())) {
            throw new IllegalArgumentException(
                    "Prize does not belong to the manager's wheel"
            );
        }

        wheelRepository.deletePrizeById(prizeId);
    }

    /**
     * Récupère l'identifiant du marchand associé à un manager.
     *
     * @param userId identifiant de l'utilisateur
     * @return identifiant du marchand
     */
    private UUID getMerchantIdForManager(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!(user instanceof MerchantManager merchantManager)) {
            throw new IllegalArgumentException(
                    "User is not a merchant manager"
            );
        }

        return merchantManager.getMerchantId();
    }

    @Override
    public WheelPrize spin(UUID merchantId) {

        Wheel wheel = getWheelByMerchantId(merchantId);

        if (wheel == null) {
            throw new IllegalArgumentException("Wheel not found");
        }

        if (!wheel.isActive()) {
            throw new IllegalArgumentException("Wheel is not active");
        }

        List<WheelPrize> prizes = wheelRepository.findPrizesByWheelId(wheel.getId());

        if (prizes.isEmpty()) {
            throw new IllegalArgumentException("Wheel has no prizes");
        }

        int totalWeight = prizes.stream()
                .mapToInt(WheelPrize::getProbabilityWeight)
                .sum();

        if (totalWeight <= 0) {
            throw new IllegalArgumentException(
                    "Wheel prizes must have a positive total probability weight"
            );
        }

        int randomValue = ThreadLocalRandom.current()
                .nextInt(totalWeight);

        int cumulativeWeight = 0;

        for (WheelPrize prize : prizes) {
            cumulativeWeight += prize.getProbabilityWeight();

            if (randomValue < cumulativeWeight) {
                return prize;
            }
        }

        throw new IllegalStateException("Unable to select a wheel prize");
    }
}