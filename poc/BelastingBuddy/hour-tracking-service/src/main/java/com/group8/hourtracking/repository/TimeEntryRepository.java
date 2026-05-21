package com.group8.hourtracking.repository;

import com.group8.hourtracking.model.TimeEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeEntryRepository extends MongoRepository<TimeEntry, UUID> {
    List<TimeEntry> findByProjectId(UUID projectId);

    /**
     * Find all time entries for a project within a date range.
     * 
     * @param projectId the project ID
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of time entries
     */
    List<TimeEntry> findByProjectIdAndDateBetween(UUID projectId, LocalDate startDate, LocalDate endDate);

    /**
     * Find a time entry for a specific project and date.
     * 
     * @param projectId the project ID
     * @param date      the date
     * @return optional time entry
     */
    Optional<TimeEntry> findByProjectIdAndDate(UUID projectId, LocalDate date);

    /**
     * Find a time entry by project, date, source provider and external ID.
     * Used to identify synced entries from specific third-party tools.
     * 
     * @param projectId      the project ID
     * @param date           the date
     * @param sourceProvider the source provider (e.g., "clickup", "clockify")
     * @param externalId     the external ID from the third-party tool
     * @return optional time entry
     */
    Optional<TimeEntry> findByProjectIdAndDateAndSourceProviderAndExternalId(
        UUID projectId, LocalDate date, String sourceProvider, String externalId);

    /**
     * Delete all time entries for a specific project.
     * 
     * @param projectId the project ID
     */
    void deleteByProjectId(UUID projectId);
}
