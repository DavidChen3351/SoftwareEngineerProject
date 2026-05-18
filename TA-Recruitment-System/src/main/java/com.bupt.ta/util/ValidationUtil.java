package com.bupt.ta.util;

import com.bupt.ta.model.Job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class ValidationUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private ValidationUtil() {
    }

    public static boolean isActiveDeadline(String deadline) {
        try {
            return LocalDateTime.parse(deadline, DATE_TIME_FORMATTER).isAfter(LocalDateTime.now());
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public static boolean isFutureOrToday(String deadline) {
        try {
            return !LocalDateTime.parse(deadline, DATE_TIME_FORMATTER).toLocalDate().isBefore(LocalDate.now());
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    /**
     * Display / apply eligibility from deadline and vacancy.
     * Closed if past deadline; otherwise No Vacancy when no seats remain; else Available.
     */
    public static PositionStatus getPositionStatus(Job job) {
        if (job.isCancelled()) {
            return PositionStatus.CLOSED;
        }
        if (!isActiveDeadline(job.getDeadline())) {
            return PositionStatus.CLOSED;
        }
        if (job.getRemainingSlots() <= 0) {
            return PositionStatus.NO_VACANCY;
        }
        return PositionStatus.AVAILABLE;
    }

    /** TA may apply only when the position is not cancelled, not paused, and still available. */
    public static boolean isJobOpenForApplications(Job job) {
        return !job.isCancelled()
                && job.isAcceptingApplications()
                && getPositionStatus(job) == PositionStatus.AVAILABLE;
    }

    public static String nowStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String nowDisplay() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
