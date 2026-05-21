package com.group8.hourtracking.service;

import com.group8.hourtracking.dto.HoursSummary;
import com.group8.hourtracking.dto.TimeEntryRequest;
import com.group8.hourtracking.model.EntrySource;
import com.group8.hourtracking.model.Project;
import com.group8.hourtracking.model.TimeEntry;
import com.group8.hourtracking.repository.ProjectRepository;
import com.group8.hourtracking.repository.TimeEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service layer for hour tracking operations.
 */
@Service
public class HourTrackingService {
    /** project repository variable. */
    private final ProjectRepository projectRepository;
    /** time entry repository variable. */
    private final TimeEntryRepository timeEntryRepository;
    /**
     * initialize hours tracking service.
     * @param projectRepository
     * @param timeEntryRepository
     */
    public HourTrackingService(ProjectRepository projectRepository,
        TimeEntryRepository timeEntryRepository) {
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
    }
    /**
     * get hours summary
     * @param projectId
     * @return gets new hours summary entry.
     */
    public HoursSummary getHoursSummary(UUID projectId) {
        List<TimeEntry> entries = timeEntryRepository.findByProjectId(projectId);
        int totalMinutes = entries.stream()
            .mapToInt(TimeEntry::getMinutes)
            .sum();
        return new HoursSummary(entries, totalMinutes);
    }

    /**
     * Get hours summary for a project within a date range.
     * @param projectId
     * @param startDate
     * @param endDate
     * @return returns new hours summary entry.
     */
    public HoursSummary getHoursSummary(UUID projectId, LocalDate startDate, LocalDate endDate) {
        List<TimeEntry> entries = timeEntryRepository
            .findByProjectIdAndDateBetween(projectId, startDate, endDate);
        int totalMinutes = entries.stream()
            .mapToInt(TimeEntry::getMinutes)
            .sum();
        return new HoursSummary(entries, totalMinutes);
    }

    /**
     * Update or create a time entry for a project.
     * @param projectId
     * @param request
     * @return returns time entry for request.
     */
    @Transactional
    public TimeEntry updateHours(UUID projectId, TimeEntryRequest request) {
        // For manual entries, always create a new entry (or update existing manual
        // entry)
        // for now, we'll create a new one each time for simplicity
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setProjectId(projectId);
        entry.setDate(request.getDay());
        entry.setMinutes((int) (request.getHours() * 60));
        entry.setDescription(request.getDescription());
        entry.setSource(EntrySource.MANUAL);
        entry.setSourceProvider("manual");
        entry.setExternalId(null); // Manual entries have no external ID
        entry.setCreatedDate(LocalDateTime.now());
        entry.setModifiedDate(LocalDateTime.now());

        return timeEntryRepository.save(entry);
    }

    /**
     * Delete all hours for a specific project.
     * @param projectId
     */
    @Transactional
    public void deleteProjectHours(UUID projectId) {
        timeEntryRepository.deleteByProjectId(projectId);
    }
    /**
     *  get projects for user.
     * @param userId
     * @return projects for user.
     */
    public List<Project> getUserProjects(UUID userId) {
        return projectRepository.findByUserId(userId);
    }
    /**
     * create project.
     * @param project
     * @return if project was saved correctly.
     */
    public Project createProject(Project project) {
        project.setId(UUID.randomUUID());
        project.setCreatedDate(LocalDateTime.now());
        project.setArchived(false);
        return projectRepository.save(project);
    }
    /**
     * get project id.
     * @param projectId
     * @return project id.
     */
    public Optional<Project> getProject(UUID projectId) {
        return projectRepository.findById(projectId);
    }

    /**
     * Delete a project and all its associated time entries.
     * @param projectId
     */
    @Transactional
    public void deleteProject(UUID projectId) {
        // First delete all time entries for this project
        timeEntryRepository.deleteByProjectId(projectId);
        // Then delete the project itself
        projectRepository.deleteById(projectId);
    }
}
