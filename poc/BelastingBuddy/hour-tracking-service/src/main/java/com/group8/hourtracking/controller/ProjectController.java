package com.group8.hourtracking.controller;

import com.group8.hourtracking.model.Project;
import com.group8.hourtracking.service.HourTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for project management operations.
 */
@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProjectController {
    /** hour tracking service variable. */
    private final HourTrackingService hourTrackingService;
    /**
     * init project controller
     * @param hourTrackingService
     */
    public ProjectController(HourTrackingService hourTrackingService) {
        this.hourTrackingService = hourTrackingService;
    }

    /**
     * Get all projects for a user.
     * @param userId
     * @return ok if project was found successfully.
     */
    @GetMapping
    public ResponseEntity<List<Project>> getUserProjects(@RequestParam UUID userId) {
        List<Project> projects = hourTrackingService.getUserProjects(userId);
        return ResponseEntity.ok(projects);
    }
    /**
     * get particular project data.
     * @param projectId
     * @return return that data of a project.
     */
    @GetMapping("/{project_id}")
    public ResponseEntity<Project> getProject(@PathVariable("project_id") UUID projectId) {
        return hourTrackingService.getProject(projectId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    /**
     * create a new project.
     * @param project
     * @return return if project was created successfully.
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        Project created = hourTrackingService.createProject(project);
        return ResponseEntity.ok(created);
    }

    /**
     * Delete a project and all its time entries.
     * @param projectId
     * @return no content response
     */
    @DeleteMapping("/{project_id}")
    public ResponseEntity<Void> deleteProject(@PathVariable("project_id") UUID projectId) {
        hourTrackingService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
