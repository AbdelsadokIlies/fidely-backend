package com.fidely.backend.infrastructure.adapters;

import com.fidely.backend.application.port.out.IMerchantRepository;
import com.fidely.backend.domain.models.Merchants.Merchant;
import com.fidely.backend.infrastructure.SpringDataRepositories.SpringDataMerchantRepository;
import com.fidely.backend.infrastructure.entities.mappers.merchants.MerchantMapper;
import com.fidely.backend.infrastructure.entities.models.merchants.MerchantEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptateur de persistance des marchands.
 */
@Component
public class MerchantRepositoryAdapter
        implements IMerchantRepository {

    private final SpringDataMerchantRepository repository;
    private final MerchantMapper mapper;

    public MerchantRepositoryAdapter(
            SpringDataMerchantRepository repository,
            MerchantMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Merchant> findById(UUID merchantId) {
        if (merchantId == null) {
            throw new IllegalArgumentException(
                    "Merchant ID cannot be null"
            );
        }

        return repository.findById(merchantId)
                .map(mapper::toDomain);
    }

    @Override
    public Merchant save(Merchant merchant) {
        if (merchant == null) {
            throw new IllegalArgumentException(
                    "Merchant cannot be null"
            );
        }

        MerchantEntity entity = mapper.toEntity(merchant);

        return mapper.toDomain(
                repository.save(entity)
        );
    }
}