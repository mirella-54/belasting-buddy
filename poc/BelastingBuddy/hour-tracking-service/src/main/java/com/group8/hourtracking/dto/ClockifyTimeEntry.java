package com.group8.hourtracking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for Clockify time entry response (from their api)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClockifyTimeEntry {
    /** id. */
    private String id;
    /** description. */
    private String description;
    /** projectId. */
    private String projectId;
    /** projectName. */
    private String projectName;
    /** time interval. */
    private ClockifyTimeInterval timeInterval;

    /**
     * get id.
     * @return id.
     */
    public String getId() {
        return id;
    }
    /**
     * set id.
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * get description.
     * @return description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * set description.
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * get project id.
     * @return project id.
     */
    public String getProjectId() {
        return projectId;
    }
    /**
     * set project id.
     * @param projectId
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    /**
     * get project name.
     * @return project name.
     */
    public String getProjectName() {
        return projectName;
    }
    /**
     * set project name.
     * @param projectName
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    /**
     * get time interval.
     * @return time interval.
     */
    public ClockifyTimeInterval getTimeInterval() {
        return timeInterval;
    }
    /**
     * set time interval.
     * @param timeInterval
     */
    public void setTimeInterval(ClockifyTimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClockifyTimeInterval {
        /**
         * start time.
         */
        private String start;
        /** end time. */
        private String end;
        /** duration. */
        private String duration; // ISO 8601 duration format

        /**
         * get start time.
         * @return start time.
         */
        public String getStart() {
            return start;
        }
        /**
         * set start time.
         * @param start
         */
        public void setStart(String start) {
            this.start = start;
        }
        /**
         * get end time.
         * @return end time.
         */
        public String getEnd() {
            return end;
        }
        /**
         * set end time.
         * @param end
         */
        public void setEnd(String end) {
            this.end = end;
        }
        /**
         * get duration.
         * @return duration.
         */
        public String getDuration() {
            return duration;
        }
        /**
         * set duration.
         * @param duration
         */
        public void setDuration(String duration) {
            this.duration = duration;
        }
    }
}
