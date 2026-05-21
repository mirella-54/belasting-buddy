package com.group8.hourtracking.service;

import com.group8.hourtracking.dto.ClickUpTimeEntry;
import com.group8.hourtracking.dto.ClockifyTimeEntry;
import com.group8.hourtracking.dto.ClockifyUser;
import com.group8.hourtracking.dto.SyncResult;
import com.group8.hourtracking.model.EntrySource;
import com.group8.hourtracking.model.Project;
import com.group8.hourtracking.model.SyncLog;
import com.group8.hourtracking.model.SyncStatus;
import com.group8.hourtracking.model.ThirdPartyAccount;
import com.group8.hourtracking.model.TimeEntry;
import com.group8.hourtracking.repository.ProjectRepository;
import com.group8.hourtracking.repository.SyncLogRepository;
import com.group8.hourtracking.repository.ThirdPartyAccountRepository;
import com.group8.hourtracking.repository.TimeEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for syncing hours from third-party tools.
 */
@Service
public class SyncService {
    /** clockify service variable. */
    private final ClockifyService clockifyService;
    /** clickup service variable. */
    private final ClickUpService clickUpService;
    /** project repository variable. */
    private final ProjectRepository projectRepository;
    /** time entry repository variable. */
    private final TimeEntryRepository timeEntryRepository;
    /** sync log repository. */
    private final SyncLogRepository syncLogRepository;
    /** third party account repository. */
    private final ThirdPartyAccountRepository thirdPartyAccountRepository;

    public SyncService(ClockifyService clockifyService,
        ClickUpService clickUpService,
        ProjectRepository projectRepository,
        TimeEntryRepository timeEntryRepository,
        SyncLogRepository syncLogRepository,
        ThirdPartyAccountRepository thirdPartyAccountRepository) {
        this.clockifyService = clockifyService;
        this.clickUpService = clickUpService;
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.syncLogRepository = syncLogRepository;
        this.thirdPartyAccountRepository = thirdPartyAccountRepository;
    }

    /**
     * Sync hours from Clockify.
     * 
     * @param userId    the user ID
     * @param startDate optional start date
     * @param endDate   optional end date
     * @return sync result
     */
    public SyncResult syncFromClockify(UUID userId, LocalDate startDate, LocalDate endDate) {
        SyncLog log = new SyncLog();
        log.setId(UUID.randomUUID());
        log.setUserId(userId);
        log.setProvider("clockify");
        log.setSyncDate(LocalDateTime.now());

        try {
            // Get user's Clockify account from database
            Optional<ThirdPartyAccount> accountOpt = thirdPartyAccountRepository.findByUserIdAndProvider(userId,
                "clockify");

            if (accountOpt.isEmpty() || accountOpt.get().getAuthToken() == null
                || accountOpt.get().getAuthToken().isEmpty()) {
                log.setStatus(SyncStatus.FAILED);
                log.setErrorMessage("No Clockify account connected for user");
                syncLogRepository.save(log);
                return new SyncResult("clockify", 0, "ERROR",
                    "No Clockify account connected. Please add a ThirdPartyAccount with provider='clockify' "
                    +"and your API key in authToken.");
            }

            ThirdPartyAccount account = accountOpt.get();
            String apiKey = account.getAuthToken();

            // get Clockify user info
            ClockifyUser user = clockifyService.getCurrentUser(apiKey);
            String workspaceId = user.getDefaultWorkspace();
            String clockifyUserId = user.getId();

            // (service) get time entries from Clockify
            List<ClockifyTimeEntry> clockifyEntries = clockifyService.getTimeEntries(
                apiKey, workspaceId, clockifyUserId, startDate, endDate);

            int syncedCount = 0;

          // process every time entry
            for (ClockifyTimeEntry clockifyEntry : clockifyEntries) {
                if (clockifyEntry.getTimeInterval() == null
                    || clockifyEntry.getTimeInterval().getDuration() == null) {
                    continue; // skip entries without duration
                }

                // Find or create project
                Project project = findOrCreateProject(userId, clockifyEntry);

                // Convert Clockify entry to our ownn TimeEntry model
                TimeEntry timeEntry = convertToTimeEntry(clockifyEntry, project.getId());

                // Check if this specific Clockify entry already exists
                LocalDate date = timeEntry.getDate();
                String externalId = clockifyEntry.getId();
                Optional<TimeEntry> existing = timeEntryRepository
                    .findByProjectIdAndDateAndSourceProviderAndExternalId(
                        project.getId(), date, "clockify", externalId);

                if (existing.isEmpty()) {
                    // Create new entry
                    timeEntryRepository.save(timeEntry);
                    syncedCount++;
                } else {
                    // Update existing Clockify entry
                    TimeEntry existingEntry = existing.get();
                    existingEntry.setMinutes(timeEntry.getMinutes());
                    existingEntry.setDescription(timeEntry.getDescription());
                    existingEntry.setModifiedDate(LocalDateTime.now());
                    timeEntryRepository.save(existingEntry);
                    syncedCount++;
                }
            }

            log.setStatus(SyncStatus.SUCCESS);
            log.setEntriesImported(syncedCount);
            syncLogRepository.save(log);

            return new SyncResult("clockify", syncedCount, "SUCCESS",
                "Successfully synced " + syncedCount + " entries from Clockify");

        } catch (Exception e) {
            log.setStatus(SyncStatus.FAILED);
            log.setErrorMessage(e.getMessage());
            syncLogRepository.save(log);

            return new SyncResult("clockify", 0, "ERROR",
                "Failed to sync from Clockify: " + e.getMessage());
        }
    }

    /**
     * Find existing project by Clockify project ID or create new one.
     * @param userId
     * @param clockifyEntry
     * @return return created/found project.
     */
    private Project findOrCreateProject(UUID userId, ClockifyTimeEntry clockifyEntry) {
        String projectName = clockifyEntry.getProjectName() != null
            ? clockifyEntry.getProjectName()
            : "Clockify Project";

        // Try to find existing project by name
        List<Project> existing = projectRepository.findByUserId(userId);
        for (Project p : existing) {
            if (p.getName().equals(projectName)) {
                return p;
            }
        }

        // Create new project
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUserId(userId);
        project.setName(projectName);
        project.setDescription("[Synced from Clockify]");
        project.setCreatedDate(LocalDateTime.now());
        project.setArchived(false);

        return projectRepository.save(project);
    }

    /**
     * Converts Clockify time entry to our own TimeEntry model
     * @param clockifyEntry
     * @param projectId
     * @return returns the time entry from clockify entry.
     */
    private TimeEntry convertToTimeEntry(ClockifyTimeEntry clockifyEntry, UUID projectId) {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);

        // Extract date from start time
        String startStr = clockifyEntry.getTimeInterval().getStart();
        ZonedDateTime start = ZonedDateTime.parse(startStr);
        entry.setDate(start.toLocalDate());

        // Convert duration from ISO8601 (apparently clockify uses this format) to
        // minutes
        String durationStr = clockifyEntry.getTimeInterval().getDuration();
        if (durationStr != null && !durationStr.isEmpty()) {
            try {
                Duration duration = Duration.parse(durationStr);
                entry.setMinutes((int) duration.toMinutes());
            } catch (Exception e) {
                // If parsing fails => set to 0
                entry.setMinutes(0);
            }
        }

        entry.setDescription(clockifyEntry.getDescription());
        entry.setSource(EntrySource.SYNCED);
        entry.setSourceProvider("clockify");
        entry.setExternalId(clockifyEntry.getId());
        entry.setCreatedDate(LocalDateTime.now());
        entry.setModifiedDate(LocalDateTime.now());

        return entry;
    }

    /**
     * Sync hours from ClickUp to our own database.
     * 
     * @param userId    the user ID
     * @param startDate optional start date
     * @param endDate   optional end date
     * @return sync result
     */
    public SyncResult syncFromClickUp(UUID userId, LocalDate startDate, LocalDate endDate) {
        SyncLog log = new SyncLog();
        log.setId(UUID.randomUUID());
        log.setUserId(userId);
        log.setProvider("clickup");
        log.setSyncDate(LocalDateTime.now());

        try {
            // Get user's ClickUp account from database
            Optional<ThirdPartyAccount> accountOpt = thirdPartyAccountRepository.findByUserIdAndProvider(userId,
                "clickup");

            if (accountOpt.isEmpty() || accountOpt.get().getAuthToken() == null
                || accountOpt.get().getAuthToken().isEmpty()) {
                log.setStatus(SyncStatus.FAILED);
                log.setErrorMessage("No ClickUp account connected for user");
                syncLogRepository.save(log);
                return new SyncResult("clickup", 0, "ERROR",
                    "No ClickUp account connected. Please add a ThirdPartyAccount with" + 
                    "provider='clickup' and your API key in authToken.");
            }

            ThirdPartyAccount account = accountOpt.get();
            String apiKey = extractApiKeyFromAccount(account);

            // Convert dates to Unix timestamps (milliseconds) for ClickUp API
            Long startTimestamp = startDate != null
                ? startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;
            Long endTimestamp = endDate != null
                ? endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;

            // Get team ID from account or extract from stored format
            String teamId = extractTeamIdFromAccount(account);

            List<ClickUpTimeEntry> clickupEntries = clickUpService.getTimeEntries(
                apiKey, teamId, startTimestamp, endTimestamp, null, null, null, null, null);

            int syncedCount = 0;

            // loop through every time entry
            for (ClickUpTimeEntry clickupEntry : clickupEntries) {
                if (clickupEntry.getDuration() == null || clickupEntry.getDuration().isEmpty()) {
                    continue; // skip entries without duration
                }

                // Find or create project
                Project project = findOrCreateProjectFromClickUp(userId, clickupEntry);

                // Convert to our own TimeEntry
                TimeEntry timeEntry = convertClickUpToTimeEntry(clickupEntry, project.getId());

                // Check if this specific ClickUp entry already exists
                LocalDate date = timeEntry.getDate();
                String externalId = clickupEntry.getId();
                Optional<TimeEntry> existing = timeEntryRepository
                    .findByProjectIdAndDateAndSourceProviderAndExternalId(
                        project.getId(), date, "clickup", externalId);

                if (existing.isEmpty()) {
                    // Create new entry
                    timeEntryRepository.save(timeEntry);
                    syncedCount++;
                } else {
                    // Update existing ClickUp entry
                    TimeEntry existingEntry = existing.get();
                    existingEntry.setMinutes(timeEntry.getMinutes());
                    existingEntry.setDescription(timeEntry.getDescription());
                    existingEntry.setModifiedDate(LocalDateTime.now());
                    timeEntryRepository.save(existingEntry);
                    syncedCount++;
                }
            }

            log.setStatus(SyncStatus.SUCCESS);
            log.setEntriesImported(syncedCount);
            log.setAccountId(account.getId());
            syncLogRepository.save(log);

            // Update last sync date
            account.setLastSyncDate(LocalDateTime.now());
            thirdPartyAccountRepository.save(account);

            return new SyncResult("clickup", syncedCount, "SUCCESS",
                "Successfully synced " + syncedCount + " entries from ClickUp");

        } catch (Exception e) {
            log.setStatus(SyncStatus.FAILED);
            log.setErrorMessage(e.getMessage());
            syncLogRepository.save(log);

            return new SyncResult("clickup", 0, "ERROR",
                "Failed to sync from ClickUp: " + e.getMessage());
        }
    }

    /**
     * Extract API key from ClickUp account.
     * Format: "apiKey:teamId".
     * @param account
     * @return returns the api key.
     */
    private String extractApiKeyFromAccount(ThirdPartyAccount account) {
        String token = account.getAuthToken();
        if (token.contains(":")) {
            String[] parts = token.split(":", 2);
            return parts[0]; // Return API key
        }
        return token; // Return whole token if no separator
    }

    /**
     * Extract team ID from ClickUp account.
     * Format: "apiKey:teamId"
     * If not provided, we'll need to get it from the API or configuration.
     * @param account 
     * @return returns a msg whether team id was found.
     */
    private String extractTeamIdFromAccount(ThirdPartyAccount account) {
        String token = account.getAuthToken();
        if (token.contains(":")) {
            String[] parts = token.split(":", 2);
            return parts[1]; // Return team ID
        }
        // If team ID is not in the token, throw an error
        throw new IllegalArgumentException(
            "Team ID not found in authToken. Please format authToken as 'apiKey:teamId' for ClickUp accounts.");
    }

    /**
     * Find existing project by ClickUp task name or create new one.
     * we assume that a task == a project
     * @param userId
     * @param clickupEntry
     * @return returns project if found one.
     */
    private Project findOrCreateProjectFromClickUp(UUID userId, ClickUpTimeEntry clickupEntry) {
        String projectName = clickupEntry.getTask() != null && clickupEntry.getTask().getName() != null
            ? clickupEntry.getTask().getName()
            : "ClickUp Task";

        // Try to find existing project by name
        List<Project> existing = projectRepository.findByUserId(userId);
        for (Project p : existing) {
            if (p.getName().equals(projectName)) {
                return p;
            }
        }

        // Create new project
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUserId(userId);
        project.setName(projectName);
        project.setDescription("[Synced from ClickUp]");
        project.setCreatedDate(LocalDateTime.now());
        project.setArchived(false);

        return projectRepository.save(project);
    }

    /**
     * Converts ClickUp time entry to our own TimeEntry model.
     * @param clickupEntry
     * @param projectId
     * @return returns entry.
     */
    private TimeEntry convertClickUpToTimeEntry(ClickUpTimeEntry clickupEntry, UUID projectId) {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);

        // Extract date from start time (unix timestmp ms)
        String startStr = clickupEntry.getStart();
        if (startStr != null && !startStr.isEmpty()) {
            long startMillis = Long.parseLong(startStr);
            Instant instant = Instant.ofEpochMilli(startMillis);
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            entry.setDate(date);
        }

      // Convert duration from ms to minutes
        String durationStr = clickupEntry.getDuration();
        if (durationStr != null && !durationStr.isEmpty()) {
            try {
                long durationMillis = Long.parseLong(durationStr);
                entry.setMinutes((int) (durationMillis / 60000)); // Convert ms to minutes
            } catch (Exception e) {
                entry.setMinutes(0);
            }
        }

        entry.setDescription(clickupEntry.getDescription());
        entry.setSource(EntrySource.SYNCED);
        entry.setSourceProvider("clickup");
        entry.setExternalId(clickupEntry.getId());
        entry.setCreatedDate(LocalDateTime.now());
        entry.setModifiedDate(LocalDateTime.now());

        return entry;
    }
}
