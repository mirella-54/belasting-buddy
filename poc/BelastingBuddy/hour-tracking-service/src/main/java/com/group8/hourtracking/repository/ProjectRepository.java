package com.group8.hourtracking.repository;

import com.group8.hourtracking.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends MongoRepository<Project, UUID> {

    List<Project> findByUserId(UUID userId);

    /**
     * Find all (non)-archived projects for a user.
     * 
     * @param userId   the user ID
     * @param archived whether to include archived projects
     * @return list of projects
     */
    List<Project> findByUserIdAndArchived(UUID userId, boolean archived);
}
