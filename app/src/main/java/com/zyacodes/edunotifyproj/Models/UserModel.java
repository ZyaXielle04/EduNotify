package com.zyacodes.edunotifyproj.Models;

import com.google.firebase.database.PropertyName;

public class UserModel {
    private String uid;
    private String username;
    private String email;
    private String role;
    private String studentNumber;
    private boolean isApproved;

    public UserModel() { }

    public UserModel(String uid, String username, String email, String role, String studentNumber, boolean isApproved) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.role = role;
        this.studentNumber = studentNumber;
        this.isApproved = isApproved;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    // Use @PropertyName to map Firebase field correctly
    @PropertyName("isApproved")
    public boolean isApproved() { return isApproved; }

    @PropertyName("isApproved")
    public void setApproved(boolean approved) { isApproved = approved; }
}
