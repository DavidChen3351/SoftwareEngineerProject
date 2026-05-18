package com.bupt.ta.util;

import com.bupt.ta.model.Job;

/**
 * TA-facing position availability on the job list.
 * Based on deadline and remaining vacancy; see {@link ValidationUtil#getPositionStatus(Job)}.
 */
public enum PositionStatus {
    AVAILABLE("Available", "position-status available"),
    CLOSED("Closed", "position-status closed"),
    NO_VACANCY("No Vacancy", "position-status no-vacancy");

    private final String label;
    private final String cssClass;

    PositionStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getJobCardClass() {
        return "job-card job-card--" + name().toLowerCase().replace('_', '-');
    }

    public boolean canApply() {
        return this == AVAILABLE;
    }
}
