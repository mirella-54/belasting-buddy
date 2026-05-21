package com.group8.hourtracking.repository;

import com.group8.hourtracking.model.SyncLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SyncLogRepository extends MongoRepository<SyncLog, UUID> {
    /**
     * Find all syc logs for a specific third-party account.
     * 
     * @param accountId the account ID
     * @return list of sync logs
     */
    List<SyncLog> findByAccountId(UUID accountId);
}
