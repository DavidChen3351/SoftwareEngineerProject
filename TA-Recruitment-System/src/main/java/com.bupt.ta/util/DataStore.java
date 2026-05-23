package com.bupt.ta.util;

import com.bupt.ta.model.ApplicationRecord;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.User;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple file-backed data access utilities.
 * Reads and writes JSON arrays for users, jobs, and applications under a persistent data directory.
 * Most public APIs are synchronized to provide basic thread-safety for concurrent web requests.
 */
public final class DataStore {
    private static final String DATA_ROOT_KEY = "com.bupt.ta.dataRoot";
    private static final String USERS_FILE = "/data/users.json";
    private static final String JOBS_FILE = "/data/jobs.json";
    private static final String APPLICATIONS_FILE = "/data/applications.json";

    private DataStore() {
    }

    /**
     * Load all users from persistent storage.
     * Returns an empty list if none.
     */
    public static synchronized List<User> loadUsers(ServletContext context) {
        List<Map<String, Object>> rows = readArray(context, USERS_FILE);
        List<User> users = new ArrayList<User>();
        for (Map<String, Object> row : rows) {
            users.add(User.fromMap(row));
        }
        return users;
    }

    /**
     * Persist the given user list to storage (overwrites).
     */
    public static synchronized void saveUsers(ServletContext context, List<User> users) {
        List<Object> rows = new ArrayList<Object>();
        for (User user : users) {
            rows.add(user.toMap());
        }
        writeArray(context, USERS_FILE, rows);
    }

    /**
     * Load all jobs from persistent storage.
     */
    public static synchronized List<Job> loadJobs(ServletContext context) {
        List<Map<String, Object>> rows = readArray(context, JOBS_FILE);
        List<Job> jobs = new ArrayList<Job>();
        for (Map<String, Object> row : rows) {
            jobs.add(Job.fromMap(row));
        }
        return jobs;
    }

    /**
     * Persist the given job list to storage (overwrites).
     */
    public static synchronized void saveJobs(ServletContext context, List<Job> jobs) {
        List<Object> rows = new ArrayList<Object>();
        for (Job job : jobs) {
            rows.add(job.toMap());
        }
        writeArray(context, JOBS_FILE, rows);
    }

    /**
     * Load all application records from persistent storage.
     */
    public static synchronized List<ApplicationRecord> loadApplications(ServletContext context) {
        List<Map<String, Object>> rows = readArray(context, APPLICATIONS_FILE);
        List<ApplicationRecord> applications = new ArrayList<ApplicationRecord>();
        for (Map<String, Object> row : rows) {
            applications.add(ApplicationRecord.fromMap(row));
        }
        return applications;
    }

    /**
     * Persist the given application records to storage (overwrites).
     */
    public static synchronized void saveApplications(ServletContext context, List<ApplicationRecord> applications) {
        List<Object> rows = new ArrayList<Object>();
        for (ApplicationRecord application : applications) {
            rows.add(application.toMap());
        }
        writeArray(context, APPLICATIONS_FILE, rows);
    }

    /**
     * Find a user by email (case-insensitive). Returns null if not found.
     */
    public static User findUserByEmail(ServletContext context, String email) {
        for (User user : loadUsers(context)) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Find a user by studentId (case-insensitive). Returns null if not found.
     */
    public static User findUserByStudentId(ServletContext context, String studentId) {
        for (User user : loadUsers(context)) {
            if (studentId != null && !studentId.isEmpty() && studentId.equalsIgnoreCase(user.getStudentId())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Accepts either studentId or internal id and returns the matching user if present.
     */
    public static User findUserByLoginId(ServletContext context, String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            return null;
        }
        String normalized = loginId.trim();
        for (User user : loadUsers(context)) {
            if (normalized.equalsIgnoreCase(user.getStudentId())
                    || normalized.equalsIgnoreCase(user.getId())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Find a job by its id. Returns null if not found.
     */
    public static Job findJob(ServletContext context, String jobId) {
        for (Job job : loadJobs(context)) {
            if (job.getId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    /**
     * Return applications that belong to the given job id.
     */
    public static List<ApplicationRecord> findApplicationsByJob(ServletContext context, String jobId) {
        List<ApplicationRecord> matches = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : loadApplications(context)) {
            if (application.getJobId().equals(jobId)) {
                matches.add(application);
            }
        }
        return matches;
    }

    /**
     * Check whether a student has applied to a job.
     */
    public static boolean hasApplied(ServletContext context, String jobId, String studentId) {
        return findApplicationByJobAndStudent(context, jobId, studentId) != null;
    }

    /**
     * Find a single application record by job id and student id.
     * Returns null when none found.
     */
    public static synchronized ApplicationRecord findApplicationByJobAndStudent(ServletContext context, String jobId,
                                                                              String studentId) {
        for (ApplicationRecord application : loadApplications(context)) {
            if (application.getJobId().equals(jobId) && application.getStudentId().equals(studentId)) {
                return application;
            }
        }
        return null;
    }

    /**
     * Return all applications for a student sorted by submittedAt descending.
     */
    public static synchronized List<ApplicationRecord> findApplicationsByStudent(ServletContext context,
                                                                                  String studentId) {
        List<ApplicationRecord> result = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : loadApplications(context)) {
            if (studentId != null && studentId.equals(application.getStudentId())) {
                result.add(application);
            }
        }
        result.sort(new Comparator<ApplicationRecord>() {
            public int compare(ApplicationRecord left, ApplicationRecord right) {
                String a = left.getSubmittedAt() == null ? "" : left.getSubmittedAt();
                String b = right.getSubmittedAt() == null ? "" : right.getSubmittedAt();
                return b.compareTo(a);
            }
        });
        return result;
    }

    /**
     * Determine if a teacher currently has any active recruitment with open slots.
     */
    public static boolean teacherHasActiveRecruitment(ServletContext context, String teacherId) {
        for (Job job : loadJobs(context)) {
            if (job.isCancelled()) {
                continue;
            }
            if (job.getTeacherId().equals(teacherId)
                    && job.getRemainingSlots() > 0
                    && ValidationUtil.isActiveDeadline(job.getDeadline())) {
                return true;
            }
        }
        return false;
    }

    /** Remove all applications for a TA and realign job filledSlots from ACCEPTED records. */
    public static synchronized void removeApplicationsForTaUser(ServletContext context, User taUser) {
        if (taUser == null || !"TA".equals(taUser.getRole())) {
            return;
        }
        String studentId = taUser.getStudentId();
        String email = taUser.getEmail() == null ? "" : taUser.getEmail().trim();
        List<ApplicationRecord> applications = loadApplications(context);
        applications.removeIf(application -> matchesTaUser(application, studentId, email));
        saveApplications(context, applications);
        recalculateFilledSlots(context);
    }

    /**
     * Remove all applications associated with a specific job id.
     */
    public static synchronized void removeApplicationsForJob(ServletContext context, String jobId) {
        if (jobId == null || jobId.trim().isEmpty()) {
            return;
        }
        List<ApplicationRecord> applications = loadApplications(context);
        applications.removeIf(application -> jobId.trim().equals(application.getJobId()));
        saveApplications(context, applications);
    }

    /** filledSlots = count of ACCEPTED applications per job (capped at totalSlots). */
    public static synchronized void recalculateFilledSlots(ServletContext context) {
        List<Job> jobs = loadJobs(context);
        List<ApplicationRecord> applications = loadApplications(context);
        for (Job job : jobs) {
            int accepted = 0;
            for (ApplicationRecord application : applications) {
                if (job.getId().equals(application.getJobId()) && "ACCEPTED".equals(application.getStatus())) {
                    accepted++;
                }
            }
            job.setFilledSlots(Math.min(accepted, job.getTotalSlots()));
        }
        saveJobs(context, jobs);
    }

    private static boolean matchesTaUser(ApplicationRecord application, String studentId, String email) {
        if (studentId != null && !studentId.isEmpty() && studentId.equals(application.getStudentId())) {
            return true;
        }
        return email != null && !email.isEmpty() && email.equalsIgnoreCase(application.getStudentEmail());
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> readArray(ServletContext context, String relativePath) {
        Path path = toPath(context, relativePath);
        ensureFile(path);
        try {
            String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return Collections.emptyList();
            }
            Object parsed = JsonUtil.parse(content);
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            if (parsed instanceof List) {
                for (Object item : (List<Object>) parsed) {
                    result.add((Map<String, Object>) item);
                }
            }
            return result;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read " + relativePath, exception);
        }
    }

    private static void writeArray(ServletContext context, String relativePath, List<Object> rows) {
        Path path = toPath(context, relativePath);
        ensureFile(path);
        try {
            Files.write(path, JsonUtil.stringify(rows).getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write " + relativePath, exception);
        }
    }

    /**
     * Persistent data lives outside the WAR so Tomcat redeploy/restart does not reset users.json.
     * Override with environment variable TA_DATA_DIR if needed.
     */
    private static Path dataRoot(ServletContext context) {
        Object cached = context.getAttribute(DATA_ROOT_KEY);
        if (cached instanceof Path) {
            return (Path) cached;
        }
        Path root = resolvePersistentRoot();
        try {
            ensureDataFiles(context, root);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize data directory " + root, exception);
        }
        context.setAttribute(DATA_ROOT_KEY, root);
        return root;
    }

    /**
     * Return the persistent data root directory used by the application.
     * Directory is created and seeded on first access.
     */
    public static synchronized Path getDataDirectory(ServletContext context) {
        return dataRoot(context);
    }

    /**
     * Map of application statuses to CSS classes for UI rendering.
     */
    public static Map<String, String> statusClassMap() {
        Map<String, String> classes = new LinkedHashMap<String, String>();
        classes.put("PENDING", "status pending");
        classes.put("ACCEPTED", "status accepted");
        classes.put("REJECTED", "status rejected");
        return classes;
    }

    private static Path resolvePersistentRoot() {
        String override = System.getenv("TA_DATA_DIR");
        if (override != null && !override.trim().isEmpty()) {
            return Paths.get(override.trim());
        }
        // Do NOT use catalina.base: IntelliJ Tomcat often uses a temp work dir wiped on restart.
        return Paths.get(System.getProperty("user.home"), ".ta-recruitment", "data");
    }

    private static void ensureDataFiles(ServletContext context, Path root) throws IOException {
        Files.createDirectories(root);
        migrateLegacyIdeaTomcatData(root);
        seedFileIfMissing(context, root, "users.json");
        seedFileIfMissing(context, root, "jobs.json");
        seedFileIfMissing(context, root, "applications.json");
    }

    /** One-time copy from old catalina.base store if IntelliJ had been using it. */
    private static void migrateLegacyIdeaTomcatData(Path root) throws IOException {
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase == null || catalinaBase.isEmpty()) {
            return;
        }
        Path legacy = Paths.get(catalinaBase, "ta-recruitment-data");
        if (!Files.isDirectory(legacy)) {
            return;
        }
        for (String fileName : new String[]{"users.json", "jobs.json", "applications.json"}) {
            Path target = root.resolve(fileName);
            Path source = legacy.resolve(fileName);
            if (!Files.exists(target) && Files.exists(source) && Files.size(source) > 2) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static void seedFileIfMissing(ServletContext context, Path root, String fileName) throws IOException {
        Path target = root.resolve(fileName);
        if (Files.exists(target)) {
            return;
        }
        Path bundled = bundledDataFile(context, fileName);
        if (bundled != null && Files.exists(bundled) && Files.size(bundled) > 2) {
            Files.copy(bundled, target, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        Files.write(target, "[]".getBytes(StandardCharsets.UTF_8));
    }

    private static Path bundledDataFile(ServletContext context, String fileName) {
        String realPath = context.getRealPath("/data/" + fileName);
        if (realPath != null) {
            return Paths.get(realPath);
        }
        return null;
    }

    private static Path toPath(ServletContext context, String relativePath) {
        String fileName = relativePath;
        if (fileName.startsWith("/data/")) {
            fileName = fileName.substring("/data/".length());
        } else if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        return dataRoot(context).resolve(fileName);
    }

    private static void ensureFile(Path path) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.write(path, "[]".getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize " + path, exception);
        }
    }
}
