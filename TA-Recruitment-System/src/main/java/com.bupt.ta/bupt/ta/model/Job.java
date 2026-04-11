package com.bupt.ta.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Job {
    private String id;
    private String title;
    private String courseName;
    private String workload;
    private int totalSlots;
    private int filledSlots;
    private String deadline;
    private String teacherId;
    private String teacherName;

    public Job() {
    }

    public Job(String id, String title, String courseName, String workload, int totalSlots, int filledSlots,
               String deadline, String teacherId, String teacherName) {
        this.id = id;
        this.title = title;
        this.courseName = courseName;
        this.workload = workload;
        this.totalSlots = totalSlots;
        this.filledSlots = filledSlots;
        this.deadline = deadline;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    public static Job fromMap(Map<String, Object> map) {
        return new Job(
                String.valueOf(map.get("id")),
                String.valueOf(map.get("title")),
                String.valueOf(map.get("courseName")),
                String.valueOf(map.get("workload")),
                ((Number) map.get("totalSlots")).intValue(),
                ((Number) map.get("filledSlots")).intValue(),
                String.valueOf(map.get("deadline")),
                String.valueOf(map.get("teacherId")),
                String.valueOf(map.get("teacherName"))
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", id);
        map.put("title", title);
        map.put("courseName", courseName);
        map.put("workload", workload);
        map.put("totalSlots", totalSlots);
        map.put("filledSlots", filledSlots);
        map.put("deadline", deadline);
        map.put("teacherId", teacherId);
        map.put("teacherName", teacherName);
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
}
