package com.bupt.ta.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApplicationRecord {
    private String id;
    private String jobId;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String skills;
    private String availability;
    private String experience;
    private String resumePath;
    private String status;
    private String submittedAt;

    public ApplicationRecord() {
    }

    public ApplicationRecord(String id, String jobId, String studentId, String studentName, String studentEmail,
                             String skills, String availability, String experience, String resumePath,
                             String status, String submittedAt) {
        this.id = id;
        this.jobId = jobId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.skills = skills;
        this.availability = availability;
        this.experience = experience;
        this.resumePath = resumePath;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public static ApplicationRecord fromMap(Map<String, Object> map) {
        return new ApplicationRecord(
                String.valueOf(map.get("id")),
                String.valueOf(map.get("jobId")),
                String.valueOf(map.get("studentId")),
                String.valueOf(map.get("studentName")),
                String.valueOf(map.get("studentEmail")),
                String.valueOf(map.get("skills")),
                String.valueOf(map.get("availability")),
                String.valueOf(map.get("experience")),
                String.valueOf(map.get("resumePath")),
                String.valueOf(map.get("status")),
                String.valueOf(map.get("submittedAt"))
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", id);
        map.put("jobId", jobId);
        map.put("studentId", studentId);
        map.put("studentName", studentName);
        map.put("studentEmail", studentEmail);
        map.put("skills", skills);
        map.put("availability", availability);
        map.put("experience", experience);
        map.put("resumePath", resumePath);
        map.put("status", status);
        map.put("submittedAt", submittedAt);
        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }
}
