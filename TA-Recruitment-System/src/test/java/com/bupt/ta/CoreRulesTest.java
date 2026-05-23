package com.bupt.ta;

import com.bupt.ta.model.ApplicationRecord;
import com.bupt.ta.model.Job;
import com.bupt.ta.util.PasswordUtil;
import com.bupt.ta.util.PositionStatus;
import com.bupt.ta.util.ValidationUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight test program for core business rules.
 *
 * <p>This project does not use JUnit yet, so this class can be compiled by Maven
 * and executed directly with {@code java}. It exits with an AssertionError when
 * any rule is broken.</p>
 */
public class CoreRulesTest {
    private static final DateTimeFormatter DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static void main(String[] args) {
        testPasswordStrength();
        testPasswordHashing();
        testPositionAvailabilityRules();
        testPausedPositionBlocksApplications();
        testCancelledPositionBlocksApplications();
        testJobMapRoundTrip();
        testApplicationRecordReviewedAtRoundTrip();

        System.out.println("All core rule tests passed.");
    }

    private static void testPasswordStrength() {
        assertTrue(ValidationUtil.isStrongPassword("Strong123"), "strong password should pass");
        assertFalse(ValidationUtil.isStrongPassword("Short1"), "short password should fail");
        assertFalse(ValidationUtil.isStrongPassword("lowercase123"), "password without uppercase should fail");
        assertFalse(ValidationUtil.isStrongPassword("UPPERCASE123"), "password without lowercase should fail");
        assertFalse(ValidationUtil.isStrongPassword("NoDigitsHere"), "password without digit should fail");
    }

    private static void testPasswordHashing() {
        String hashA = PasswordUtil.hash("Strong123");
        String hashB = PasswordUtil.hash("Strong123");

        assertEquals(hashA, hashB, "same password should produce same hash");
        assertEquals(64, hashA.length(), "SHA-256 hex hash should be 64 characters");
        assertFalse("Strong123".equals(hashA), "hash should not equal plaintext");
    }

    private static void testPositionAvailabilityRules() {
        Job available = newJob(3, 1, futureDeadline(), true, false);
        assertEquals(PositionStatus.AVAILABLE, ValidationUtil.getPositionStatus(available),
                "future job with vacancy should be available");
        assertTrue(ValidationUtil.isJobOpenForApplications(available), "available job should accept applications");

        Job noVacancy = newJob(2, 2, futureDeadline(), true, false);
        assertEquals(PositionStatus.NO_VACANCY, ValidationUtil.getPositionStatus(noVacancy),
                "full job should show no vacancy");
        assertFalse(ValidationUtil.isJobOpenForApplications(noVacancy), "full job should block applications");

        Job expired = newJob(3, 0, pastDeadline(), true, false);
        assertEquals(PositionStatus.CLOSED, ValidationUtil.getPositionStatus(expired),
                "expired job should be closed");
        assertFalse(ValidationUtil.isJobOpenForApplications(expired), "expired job should block applications");
    }

    private static void testPausedPositionBlocksApplications() {
        Job paused = newJob(3, 0, futureDeadline(), false, false);

        assertEquals(PositionStatus.AVAILABLE, ValidationUtil.getPositionStatus(paused),
                "paused job can still have available deadline and vacancy");
        assertFalse(ValidationUtil.isJobOpenForApplications(paused),
                "paused job should block new applications");
    }

    private static void testCancelledPositionBlocksApplications() {
        Job cancelled = newJob(3, 0, futureDeadline(), true, true);

        assertEquals(PositionStatus.CLOSED, ValidationUtil.getPositionStatus(cancelled),
                "cancelled job should be closed");
        assertFalse(ValidationUtil.isJobOpenForApplications(cancelled),
                "cancelled job should block applications");
    }

    private static void testJobMapRoundTrip() {
        Job original = new Job("J1", "TA for Programming", "CS101", "Programming Fundamentals",
                "6 hours/week", 3, 1, futureDeadline(), "T1", "Dr. Zhang", false, true);

        Map<String, Object> map = original.toMap();
        Job restored = Job.fromMap(map);

        assertEquals("J1", restored.getId(), "job id should survive map round trip");
        assertEquals("CS101", restored.getModuleCode(), "module code should survive map round trip");
        assertEquals("Programming Fundamentals", restored.getCourseName(), "course name should survive map round trip");
        assertFalse(restored.isAcceptingApplications(), "acceptingApplications should survive map round trip");
        assertTrue(restored.isCancelled(), "cancelled flag should survive map round trip");
    }

    private static void testApplicationRecordReviewedAtRoundTrip() {
        ApplicationRecord original = new ApplicationRecord("A1", "J1", "231200001", "Alice",
                "alice@mail.bupt.edu.cn", "Java", "Mon-Fri", "Lab tutor", "uploads/cv.pdf",
                "ACCEPTED", "2026-05-23 10:00:00");
        original.setReviewedAt("2026-05-23 11:00:00");

        ApplicationRecord restored = ApplicationRecord.fromMap(original.toMap());

        assertEquals("ACCEPTED", restored.getStatus(), "application status should survive map round trip");
        assertEquals("2026-05-23 11:00:00", restored.getReviewedAt(),
                "reviewedAt should survive map round trip");

        Map<String, Object> legacyMap = new LinkedHashMap<String, Object>();
        legacyMap.put("id", "A2");
        legacyMap.put("jobId", "J2");
        legacyMap.put("studentId", "231200002");
        legacyMap.put("studentName", "Bob");
        legacyMap.put("studentEmail", "bob@mail.bupt.edu.cn");
        legacyMap.put("skills", "Testing");
        legacyMap.put("availability", "Weekend");
        legacyMap.put("experience", "None");
        legacyMap.put("resumePath", "uploads/bob.pdf");
        legacyMap.put("status", "PENDING");
        legacyMap.put("submittedAt", "2026-05-23 12:00:00");
        ApplicationRecord legacy = ApplicationRecord.fromMap(legacyMap);
        assertEquals("", legacy.getReviewedAt(), "legacy application without reviewedAt should default to empty string");
    }

    private static Job newJob(int totalSlots, int filledSlots, String deadline,
                              boolean acceptingApplications, boolean cancelled) {
        return new Job("JTEST", "TA Test", "TEST101", "Testing Module", "4 hours/week",
                totalSlots, filledSlots, deadline, "TTEST", "Dr. Test",
                acceptingApplications, cancelled);
    }

    private static String futureDeadline() {
        return LocalDateTime.now().plusDays(7).format(DEADLINE_FORMAT);
    }

    private static String pastDeadline() {
        return LocalDateTime.now().minusDays(1).format(DEADLINE_FORMAT);
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " | expected: " + expected + ", actual: " + actual);
        }
    }
}
