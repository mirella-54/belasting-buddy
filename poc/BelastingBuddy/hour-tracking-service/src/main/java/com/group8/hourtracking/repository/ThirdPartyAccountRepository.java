package com.group8.hourtracking.repository;

import com.group8.hourtracking.model.ThirdPartyAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThirdPartyAccountRepository extends MongoRepository<ThirdPartyAccount, UUID> {
    List<ThirdPartyAccount> findByUserId(UUID userId);

    /**
     * Find a specific third-party account by user and provider.
     * 
     * @param userId   the user ID
     * @param provider the provider name
     * @return optional third-party account
     */
    Optional<ThirdPartyAccount> findByUserIdAndProvider(UUID userId, String provider);
}
