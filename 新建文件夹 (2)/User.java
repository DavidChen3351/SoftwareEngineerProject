package com.bupt.ta.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class User {
    private String id;
    private String studentId;
    private String email;
    private String name;
    private String passwordHash;
    private String role;
    private boolean enabled;

    public User() {
    }

    public User(String id, String studentId, String email, String name, String passwordHash, String role, boolean enabled) {
        this.id = id;
        this.studentId = studentId;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public static User fromMap(Map<String, Object> map) {
        return new User(
                String.valueOf(map.get("id")),
                map.get("studentId") == null ? "" : String.valueOf(map.get("studentId")),
                String.valueOf(map.get("email")),
                String.valueOf(map.get("name")),
                String.valueOf(map.get("passwordHash")),
                String.valueOf(map.get("role")),
                Boolean.TRUE.equals(map.get("enabled"))
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", id);
        map.put("studentId", studentId);
        map.put("email", email);
        map.put("name", name);
        map.put("passwordHash", passwordHash);
        map.put("role", role);
        map.put("enabled", enabled);
        return map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
