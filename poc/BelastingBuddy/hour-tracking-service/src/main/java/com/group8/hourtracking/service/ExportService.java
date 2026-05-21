package com.group8.hourtracking.service;

import com.group8.hourtracking.model.TimeEntry;
import com.group8.hourtracking.repository.TimeEntryRepository;
import com.group8.hourtracking.model.Project;
import com.group8.hourtracking.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

  /**
   * Service for exporting hours data in various formats.
   */
@Service
public class ExportService {
    /** variable for an entry. */
    private final TimeEntryRepository timeEntryRepository;
    /** project repository to resolve user's projects. */
    private final ProjectRepository projectRepository;
    /**
     * a declaration of Export service.
     * @param timeEntryRepository the repository for time entries
     * @param projectRepository the repository for projects
     */
    public ExportService(TimeEntryRepository timeEntryRepository, ProjectRepository projectRepository) {
        this.timeEntryRepository = timeEntryRepository;
        this.projectRepository = projectRepository;
    }
    /**
     * Function to export data as csv.
     * @param projectId
     * @param startDate
     * @param endDate
     * @return returns a csv in string.
     */
    public String exportAsCSV(UUID projectId, LocalDate startDate, LocalDate endDate) {
        List<TimeEntry> entries = getEntries(projectId, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Hours,Minutes,Description,Source\n");

        for (TimeEntry entry : entries) {
            csv.append(entry.getDate()).append(",")
                .append(entry.getMinutes() / 60.0).append(",")
                .append(entry.getMinutes()).append(",")
                .append(entry.getDescription() != null ? entry.getDescription() : "").append(",")
                .append(entry.getSource()).append("\n");
        }

        return csv.toString();
    }

    public String exportAsJSON(UUID projectId, LocalDate startDate, LocalDate endDate) {
        List<TimeEntry> entries = getEntries(projectId, startDate, endDate);

        String jsonEntries = entries.stream()
            .map(entry -> String.format(
                "{\"date\":\"%s\",\"hours\":%.2f,\"minutes\":%d,\"description\":\"%s\",\"source\":\"%s\"}",
                entry.getDate(),
                entry.getMinutes() / 60.0,
                entry.getMinutes(),
                entry.getDescription() != null ? entry.getDescription() : "",
                entry.getSource()))
            .collect(Collectors.joining(","));

        return "[" + jsonEntries + "]";
    }

    /**
     * Export all hours for a user across all their projects in CSV format.
     * @param userId the user whose projects to export
     * @param startDate optional start date
     * @param endDate optional end date
     * @return CSV string
     */
    public String exportAllAsCSV(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Project> projects = projectRepository.findByUserId(userId);

        StringBuilder csv = new StringBuilder();
        csv.append("Project,Date,Hours,Minutes,Description,Source\n");

        for (Project project : projects) {
            List<TimeEntry> entries = getEntries(project.getId(), startDate, endDate);
            for (TimeEntry entry : entries) {
                csv.append(escapeCsv(project.getName())).append(",")
                    .append(entry.getDate()).append(",")
                    .append(entry.getMinutes() / 60.0).append(",")
                    .append(entry.getMinutes()).append(",")
                    .append(escapeCsv(entry.getDescription() != null ? entry.getDescription() : "")).append(",")
                    .append(entry.getSource())
                    .append("\n");
            }
        }

        return csv.toString();
    }

    /**
     * Export all hours for a user across all their projects in JSON format.
     * @param userId the user whose projects to export
     * @param startDate optional start date
     * @param endDate optional end date
     * @return JSON array string
     */
    public String exportAllAsJSON(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Project> projects = projectRepository.findByUserId(userId);

        String jsonEntries = projects.stream()
            .flatMap(project -> getEntries(project.getId(), startDate, endDate).stream()
                .map(entry -> String.format(
                    "{\"projectId\":\"%s\",\"projectName\":\"%s\",\"date\":\"%s\","
                    + "\"hours\":%.2f,\"minutes\":%d,\"description\":\"%s\",\"source\":\"%s\"}",
                    project.getId(),
                    escapeJson(project.getName()),
                    entry.getDate(),
                    entry.getMinutes() / 60.0,
                    entry.getMinutes(),
                    escapeJson(entry.getDescription() != null ? entry.getDescription() : ""),
                    entry.getSource())))
            .collect(Collectors.joining(","));

        return "[" + jsonEntries + "]";
    }

    /**
     * Helper method to get time entries with optional date filtering.
     * @param projectId
     * @param startDate
     * @param endDate
     * @return returns the project time.
     */
    private List<TimeEntry> getEntries(UUID projectId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return timeEntryRepository.findByProjectIdAndDateBetween(projectId, startDate, endDate);
        } else {
            return timeEntryRepository.findByProjectId(projectId);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        boolean hasCommaOrQuote = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return hasCommaOrQuote ? "\"" + escaped + "\"" : escaped;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}