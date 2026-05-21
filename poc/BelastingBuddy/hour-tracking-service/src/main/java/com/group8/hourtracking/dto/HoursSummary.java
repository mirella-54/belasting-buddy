package com.group8.hourtracking.dto;

import com.group8.hourtracking.model.TimeEntry;

import java.util.List;

public class HoursSummary {
  /** entries list. */
    private List<TimeEntry> entries;

    /** Total hours across ALL entries. */
    private double totalHours;
    /** Total minutes across ALL entries. */
    private int totalMinutes;

    /**
     * init hours summary.
     * @param entries
     * @param totalMinutes
     */
    public HoursSummary(List<TimeEntry> entries, int totalMinutes) {
        this.entries = entries;
        this.totalMinutes = totalMinutes;
        this.totalHours = totalMinutes / 60.0;
    }
    /**
     * get entries.
     * @return entries.
     */
    public List<TimeEntry> getEntries() {
        return entries;
    }
    /**
     * set entries.
     * @param entries
     */
    public void setEntries(List<TimeEntry> entries) {
        this.entries = entries;
    }
    /**
     * get total hours.
     * @return total hours.
     */
    public double getTotalHours() {
        return totalHours;
    }
    /**
     * set total hours.
     * @param totalHours
     */
    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }
    /**
     * get total minutes.
     * @return total minutes.
     */
    public int getTotalMinutes() {
        return totalMinutes;
    }
    /**
     * set total minutes.
     * @param totalMinutes
     */
    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }
}
