package com.bupt.ta.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Job {
    private String id;
    private String title;
    private String moduleCode;
    private String courseName;
    private String workload;
    private int totalSlots;
    private int filledSlots;
    private String deadline;
    private String teacherId;
    private String teacherName;
    /** When false, teacher has paused new applications; position remains visible to TAs. */
    private boolean acceptingApplications = true;
    /** When true, position is withdrawn and hidden from the TA job list. */
    private boolean cancelled = false;

    public Job() {
    }

    public Job(String id, String title, String courseName, String workload, int totalSlots, int filledSlots,
               String deadline, String teacherId, String teacherName, boolean acceptingApplications) {
        this(id, title, "", courseName, workload, totalSlots, filledSlots, deadline, teacherId, teacherName,
                acceptingApplications, false);
    }

    public Job(String id, String title, String moduleCode, String courseName, String workload, int totalSlots, int filledSlots,
               String deadline, String teacherId, String teacherName, boolean acceptingApplications,
               boolean cancelled) {
        this.id = id;
        this.title = title;
        this.moduleCode = moduleCode == null ? "" : moduleCode.trim();
        this.courseName = courseName;
        this.workload = workload;
        this.totalSlots = totalSlots;
        this.filledSlots = filledSlots;
        this.deadline = deadline;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.acceptingApplications = acceptingApplications;
        this.cancelled = cancelled;
    }

    public static Job fromMap(Map<String, Object> map) {
        boolean accepting = true;
        Object acceptingRaw = map.get("acceptingApplications");
        if (acceptingRaw instanceof Boolean) {
            accepting = ((Boolean) acceptingRaw).booleanValue();
        } else if (acceptingRaw != null) {
            accepting = Boolean.parseBoolean(String.valueOf(acceptingRaw));
        }
        boolean cancelled = false;
        Object cancelledRaw = map.get("cancelled");
        if (cancelledRaw instanceof Boolean) {
            cancelled = ((Boolean) cancelledRaw).booleanValue();
        } else if (cancelledRaw != null) {
            cancelled = Boolean.parseBoolean(String.valueOf(cancelledRaw));
        }
        return new Job(
                String.valueOf(map.get("id")),
                String.valueOf(map.get("title")),
                map.get("moduleCode") == null ? "" : String.valueOf(map.get("moduleCode")),
                String.valueOf(map.get("courseName")),
                String.valueOf(map.get("workload")),
                ((Number) map.get("totalSlots")).intValue(),
                ((Number) map.get("filledSlots")).intValue(),
                String.valueOf(map.get("deadline")),
                String.valueOf(map.get("teacherId")),
                String.valueOf(map.get("teacherName")),
                accepting,
                cancelled
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", id);
        map.put("title", title);
        map.put("moduleCode", moduleCode);
        map.put("courseName", courseName);
        map.put("workload", workload);
        map.put("totalSlots", totalSlots);
        map.put("filledSlots", filledSlots);
        map.put("deadline", deadline);
        map.put("teacherId", teacherId);
        map.put("teacherName", teacherName);
        map.put("acceptingApplications", acceptingApplications);
        map.put("cancelled", cancelled);
        return map;
    }

    public int getRemainingSlots() {
        return totalSlots - filledSlots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getFilledSlots() {
        return filledSlots;
    }

    public void setFilledSlots(int filledSlots) {
        this.filledSlots = filledSlots;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public boolean isAcceptingApplications() {
        return acceptingApplications;
    }

    public void setAcceptingApplications(boolean acceptingApplications) {
        this.acceptingApplications = acceptingApplications;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
