package com.bupt.ta.util;

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

    public static String nowStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String nowDisplay() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
