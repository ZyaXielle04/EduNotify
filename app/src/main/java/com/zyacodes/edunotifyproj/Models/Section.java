package com.zyacodes.edunotifyproj.Models;

public class Section {
    private String college;
    private String yearLevel;
    private String program;
    private String section;
    private String code;

    // Empty constructor (required for Firebase)
    public Section() {
    }

    // Full constructor
    public Section(String college, String yearLevel, String program, String section, String code) {
        this.college = college;
        this.yearLevel = yearLevel;
        this.program = program;
        this.section = section;
        this.code = code;
    }

    // Getters
    public String getCollege() { return college; }
    public String getYearLevel() { return yearLevel; }
    public String getProgram() { return program; }
    public String getSection() { return section; }
    public String getCode() { return code; }

    // Setters
    public void setCollege(String college) { this.college = college; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }
    public void setProgram(String program) { this.program = program; }
    public void setSection(String section) { this.section = section; }
    public void setCode(String code) { this.code = code; } // 👈 this fixes your error
}
