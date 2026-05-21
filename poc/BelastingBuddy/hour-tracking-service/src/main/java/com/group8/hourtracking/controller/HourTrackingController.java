package com.group8.hourtracking.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.group8.hourtracking.dto.HoursSummary;
import com.group8.hourtracking.dto.SyncResult;
import com.group8.hourtracking.dto.TimeEntryRequest;
import com.group8.hourtracking.model.TimeEntry;
import com.group8.hourtracking.service.DownstreamService;
import com.group8.hourtracking.service.ExportService;
import com.group8.hourtracking.service.HourTrackingService;
import com.group8.hourtracking.service.SyncService;

/**
 * REST controller for hour tracking operations.
 */
@RestController
@RequestMapping("/hours")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HourTrackingController {
    /**
     * Hour tracking service variable.
     */
    private final HourTrackingService hourTrackingService;
    /**
     * Export service variable.
     */
    private final ExportService exportService;
    /**
     * Sync service variable.
     */
    private final SyncService syncService;
    /**
     * Downstream service variable.
     */
    private final DownstreamService downstreamService;

    public HourTrackingController(HourTrackingService hourTrackingService,
                                  ExportService exportService,
                                  SyncService syncService,
                                  DownstreamService downstreamService) {
        this.hourTrackingService = hourTrackingService;
        this.exportService = exportService;
        this.syncService = syncService;
        this.downstreamService =downstreamService;
    }

    /**
     * Get hours summary for a project. (between start and end date)
     *
     * @param projectId the project ID
     * @param startDate start date, optional
     * @param endDate   end date, optional
     * @return hours summary
     */
    @GetMapping("/{project_id:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<HoursSummary> getHours(
        @PathVariable("project_id") UUID projectId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        HoursSummary summary;
        if (startDate != null && endDate != null) {
            summary = hourTrackingService.getHoursSummary(projectId, startDate, endDate);
        } else {
            summary = hourTrackingService.getHoursSummary(projectId);
        }

        return ResponseEntity.ok(summary);
    }

    /**
     * Update hours for a project. (PATCH)
     *
     * @param projectId the project ID
     * @param request   the time entry request
     * @return the updated time entry
     */
    @PatchMapping("/{project_id:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<TimeEntry> updateHours(
        @PathVariable("project_id") UUID projectId,
        @RequestBody TimeEntryRequest request) {

        TimeEntry entry = hourTrackingService.updateHours(projectId, request);
        return ResponseEntity.ok(entry);
    }

    /**
     * Export all hours for a user across all their projects.
     * @param userId the user whose data to export
     * @param format export format (csv, json)
     * @param startDate optional start date
     * @param endDate optional end date
     * @return exported content with download headers
     */
    @GetMapping("/export-all")
    public ResponseEntity<String> exportAllHours(
        @RequestParam UUID userId,
        @RequestParam(defaultValue = "csv") String format,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String content;
        String contentType;
        String filename;

        switch (format.toLowerCase()) {
            case "json":
                content = exportService.exportAllAsJSON(userId, startDate, endDate);
                contentType = MediaType.APPLICATION_JSON_VALUE;
                filename = "hours_export_all.json";
                break;
            case "csv":
                content = exportService.exportAllAsCSV(userId, startDate, endDate);
                contentType = "text/csv";
                filename = "hours_export_all.csv";
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid format! Supported formats are: json, csv");
        }

        return ResponseEntity.ok()
               .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
               .header(HttpHeaders.CONTENT_TYPE, contentType)
               .body(content);
    }

    /**
     * Export hours data in various formats.
     *
     * @param projectId the project ID
     * @param format    the export format (csv, json, pdf)
     * @param startDate optional start date
     * @param endDate   optional end date
     * @return the exported data
     */
    @GetMapping("/{project_id:[0-9a-fA-F\\-]{36}}/export")
    public ResponseEntity<String> exportHours(
        @PathVariable("project_id") UUID projectId,
        @RequestParam(defaultValue = "csv") String format,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String content;
        String contentType;
        String filename;

        switch (format.toLowerCase()) {
            case "json":
                content = exportService.exportAsJSON(projectId, startDate, endDate);
                contentType = MediaType.APPLICATION_JSON_VALUE;
                filename = "hours_export.json";
                break;
            case "csv":
                content = exportService.exportAsCSV(projectId, startDate, endDate);
                contentType = "text/csv";
                filename = "hours_export.csv";
                break;
            default:
                // any other format is not supported
                return ResponseEntity.badRequest().body("Invalid format! Supported formats are: json, csv");
        }

        return ResponseEntity.ok()
               .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
               .header(HttpHeaders.CONTENT_TYPE, contentType)
               .body(content);
    }

    /**
     * Delete all hours for a specific project.
     * @param projectId the project ID
     * @return no content response
     */
    @DeleteMapping("/{project_id:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<Void> deleteProjectHours(@PathVariable("project_id") UUID projectId) {
        hourTrackingService.deleteProjectHours(projectId);
        return ResponseEntity.noContent().build(); // (204, no content)
    }

    /**
     * Sync hours from third-party productivity tools (currently supports Clockify).
     *
     * @param provider  the third-party provider name
     * @param userId    the user ID to sync for
     * @param startDate optional start date filter
     * @param endDate   optional end date filter
     * @return sync result
     */
    @PostMapping("/sync/{provider}")
    public ResponseEntity<SyncResult> syncHours(
        @PathVariable String provider,
        @RequestParam UUID userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if ("clockify".equalsIgnoreCase(provider)) {
            SyncResult result = syncService.syncFromClockify(userId, startDate, endDate);
            return ResponseEntity.ok(result);
        } else if ("clickup".equalsIgnoreCase(provider)) {
            SyncResult result = syncService.syncFromClickUp(userId, startDate, endDate);
            return ResponseEntity.ok(result);
        }

        // Other providers not yet supported
        SyncResult result = new SyncResult(provider, 0, "ERROR",
                                           "Provider '" + provider 
                                           + "' not supported. Currently 'clockify' and 'clickup' are available.");
        return ResponseEntity.badRequest().body(result);
    }


    /**
     * Test endpoint to call doc-processing service.
     * @param jwt the authenticated user's JWT
     * @return response from doc-processing service
     */
    @PostMapping("/docproc")
    public ResponseEntity<String> docProcessingTest(@AuthenticationPrincipal Jwt jwt) {
        String response = downstreamService.callDocProcessing(jwt);
        return ResponseEntity.ok(response);
    }

    /**
     * Internal stub endpoint for service-to-service authentication testing.
     * @param jwt the authenticated service's JWT
     * @return confirmation message
     */
    @PostMapping("/internal/hourresponse")
    @PreAuthorize("hasAuthority('SVC_doc-processing')")
    public ResponseEntity<String> internalStubHours(@AuthenticationPrincipal Jwt jwt) {
        String caller = jwt.getClaimAsString("azp");
        System.out.println("Internal stub called by service: " + caller);
        return ResponseEntity.ok("Authenticated as service: " + caller + "   " + jwt.getSubject());
    }

}
