package com.fidely.backend.application.services;

import com.fidely.backend.application.port.in.IMerchantService;
import com.fidely.backend.application.port.out.IMerchantRepository;
import com.fidely.backend.application.port.out.IUserRepository;
import com.fidely.backend.domain.models.Merchants.Merchant;
import com.fidely.backend.domain.models.users.MerchantManager;
import com.fidely.backend.domain.models.users.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MerchantService implements IMerchantService {

    private final IUserRepository userRepository;
    private final IMerchantRepository merchantRepository;

    public MerchantService(
            IUserRepository userRepository,
            IMerchantRepository merchantRepository
    ) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Merchant getMerchantForManager(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (!(user instanceof MerchantManager merchantManager)) {
            throw new IllegalArgumentException("User is not a merchant manager");
        }

        return merchantRepository.findById(merchantManager.getMerchantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Merchant not found: " + merchantManager.getMerchantId()
                ));
    }

    @Override
    @Transactional
    public Merchant updateMerchant(
            UUID userId,
            String name,
            String slug,
            String description,
            String googleReviewUrl
    ) {
        Merchant merchant = getMerchantForManager(userId);

        Merchant updatedMerchant = new Merchant(
                merchant.getId(),
                merchant.getPointRule(),
                name,
                slug,
                merchant.getLogoUrl(),
                merchant.getPrimaryColor(),
                merchant.getSecondaryColor(),
                description,
                googleReviewUrl,
                merchant.isActive(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );

        return merchantRepository.save(updatedMerchant);
    }

    @Override
    @Transactional
    public Merchant updateBranding(
            UUID userId,
            String logoUrl,
            String primaryColor,
            String secondaryColor
    ) {
        Merchant merchant = getMerchantForManager(userId);

        Merchant updatedMerchant = new Merchant(
                merchant.getId(),
                merchant.getPointRule(),
                merchant.getName(),
                merchant.getSlug(),
                logoUrl,
                primaryColor,
                secondaryColor,
                merchant.getDescription(),
                merchant.getGoogleReviewUrl(),
                merchant.isActive(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );

        return merchantRepository.save(updatedMerchant);
    }

    @Override
    @Transactional(readOnly = true)
    public Merchant getPublicMerchant(UUID merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException("Merchant ID cannot be null");
        }

        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Merchant not found: " + merchantId
                ));
    }
}
