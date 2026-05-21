package com.group8.hourtracking.dto;

import java.time.LocalDate;

public class TimeEntryRequest {

    /** Date for which the hours are logged */
    private LocalDate day;

    /** Number of hours worked (will be converted to minutes) */
    private Double hours;

    /** Optional description */
    private String description;
    /**
     * get day.
     * @return day.
     */
    public LocalDate getDay() {
        return day;
    }
    /**
     * set day.
     * @param day
     */
    public void setDay(LocalDate day) {
        this.day = day;
    }
    /**
     * get hours.
     * @return hours.
     */
    public Double getHours() {
        return hours;
    }
    /**
     * set hours.
     * @param hours
     */
    public void setHours(Double hours) {
        this.hours = hours;
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
}
