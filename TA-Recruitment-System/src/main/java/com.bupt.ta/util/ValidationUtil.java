package com.bupt.ta.util;

import com.bupt.ta.model.Job;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validation and time utilities.
 * Handles deadline parsing/validation and basic password strength checks.
 */
public final class ValidationUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private ValidationUtil() {
    }

    /**
     * Return true if the given deadline (formatted as yyyy-MM-dd'T'HH:mm) is in the future.
     * Returns false for parse errors.
     */
    public static boolean isActiveDeadline(String deadline) {
        try {
            return LocalDateTime.parse(deadline, DATE_TIME_FORMATTER).isAfter(LocalDateTime.now());
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    /**
     * Return true when the given deadline date is today or in the future.
     * Returns false for parse errors.
     */
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

    /** Return a compact timestamp string like yyyyMMddHHmmss for internal use. */
    public static String nowStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /** Return a human-readable current datetime string like yyyy-MM-dd HH:mm:ss. */
    public static String nowDisplay() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Basic password strength check: at least 8 chars, contains upper, lower and digit.
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (int index = 0; index < password.length(); index++) {
            char ch = password.charAt(index);
            hasUpper = hasUpper || Character.isUpperCase(ch);
            hasLower = hasLower || Character.isLowerCase(ch);
            hasDigit = hasDigit || Character.isDigit(ch);
        }
        return hasUpper && hasLower && hasDigit;
    }
}
