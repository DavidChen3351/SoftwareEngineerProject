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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataStore {
    private static final String USERS_FILE = "/data/users.json";
    private static final String JOBS_FILE = "/data/jobs.json";
    private static final String APPLICATIONS_FILE = "/data/applications.json";

    private DataStore() {
    }

    public static synchronized List<User> loadUsers(ServletContext context) {
        List<Map<String, Object>> rows = readArray(context, USERS_FILE);
        List<User> users = new ArrayList<User>();
        for (Map<String, Object> row : rows) {
            users.add(User.fromMap(row));
        }
        return users;
    }

    public static synchronized void saveUsers(ServletContext context, List<User> users) {
        List<Object> rows = new ArrayList<Object>();
        for (User user : users) {
            rows.add(user.toMap());
        }
        writeArray(context, USERS_FILE, rows);
    }

    public static synchronized List<Job> loadJobs(ServletContext context) {
        List<Map<String, Object>> rows = readArray(context, JOBS_FILE);
        List<Job> jobs = new ArrayList<Job>();
        for (Map<String, Object> row : rows) {
            jobs.add(Job.fromMap(row));
        }
        return jobs;
    }

    public static synchronized void saveJobs(ServletContext context, List<Job> jobs) {
        List<Object> rows = new ArrayList<Object>();
        for (Job job : jobs) {
            rows.add(job.toMap());
        }
        writeArray(context, JOBS_FILE, rows);
    }

    public static synchronized List<ApplicationRecord> loadApplications(ServletContext context) {
        List<Map<String, Object>> rows = readArray(context, APPLICATIONS_FILE);
        List<ApplicationRecord> applications = new ArrayList<ApplicationRecord>();
        for (Map<String, Object> row : rows) {
            applications.add(ApplicationRecord.fromMap(row));
        }
        return applications;
    }

    public static synchronized void saveApplications(ServletContext context, List<ApplicationRecord> applications) {
        List<Object> rows = new ArrayList<Object>();
        for (ApplicationRecord application : applications) {
            rows.add(application.toMap());
        }
        writeArray(context, APPLICATIONS_FILE, rows);
    }

    public static User findUserByEmail(ServletContext context, String email) {
        for (User user : loadUsers(context)) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public static User findUserByStudentId(ServletContext context, String studentId) {
        for (User user : loadUsers(context)) {
            if (studentId != null && !studentId.isEmpty() && studentId.equalsIgnoreCase(user.getStudentId())) {
                return user;
            }
        }
        return null;
    }

    public static Job findJob(ServletContext context, String jobId) {
        for (Job job : loadJobs(context)) {
            if (job.getId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    public static List<ApplicationRecord> findApplicationsByJob(ServletContext context, String jobId) {
        List<ApplicationRecord> matches = new ArrayList<ApplicationRecord>();
        for (ApplicationRecord application : loadApplications(context)) {
            if (application.getJobId().equals(jobId)) {
                matches.add(application);
            }
        }
        return matches;
    }

    public static boolean hasApplied(ServletContext context, String jobId, String studentId) {
        for (ApplicationRecord application : loadApplications(context)) {
            if (application.getJobId().equals(jobId) && application.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean teacherHasActiveRecruitment(ServletContext context, String teacherId) {
        for (Job job : loadJobs(context)) {
            if (job.getTeacherId().equals(teacherId)
                    && job.getRemainingSlots() > 0
                    && ValidationUtil.isActiveDeadline(job.getDeadline())) {
                return true;
            }
        }
        return false;
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

    private static Path toPath(ServletContext context, String relativePath) {
        String realPath = context.getRealPath(relativePath);
        if (realPath != null) {
            return Paths.get(realPath);
        }
        return Paths.get(System.getProperty("user.dir"), relativePath.replaceFirst("^/", ""));
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

    public static Map<String, String> statusClassMap() {
        Map<String, String> classes = new LinkedHashMap<String, String>();
        classes.put("PENDING", "status pending");
        classes.put("ACCEPTED", "status accepted");
        classes.put("REJECTED", "status rejected");
        return classes;
    }
}
