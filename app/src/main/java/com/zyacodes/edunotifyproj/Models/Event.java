package com.zyacodes.edunotifyproj.Models;

import com.google.firebase.database.PropertyName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Event {
    private String id;
    private long dateInMillis;
    private String title;
    private String description;
    private String time;
    private String venue;
    private boolean isClosed; // keep the name isClosed
    private Map<String, Map<String, Boolean>> attendance; // sectionCode -> (studentNumber -> true/false)

    public Event() {}

    public Event(String id, long dateInMillis, String title, String description, String time, String venue,
                 boolean isClosed, Map<String, Map<String, Boolean>> attendance) {
        this.id = id;
        this.dateInMillis = dateInMillis;
        this.title = title;
        this.description = description;
        this.time = time;
        this.venue = venue;
        this.isClosed = isClosed;
        this.attendance = attendance;
    }

    // ---------------- Getters ----------------
    public String getId() { return id; }
    public long getDateInMillis() { return dateInMillis; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTime() { return time; }
    public String getVenue() { return venue; }

    @PropertyName("isClosed")
    public boolean isClosed() { return isClosed; }

    public Map<String, Map<String, Boolean>> getAttendance() { return attendance; }

    // ---------------- Setters ----------------
    public void setId(String id) { this.id = id; }
    public void setDateInMillis(long dateInMillis) { this.dateInMillis = dateInMillis; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTime(String time) { this.time = time; }
    public void setVenue(String venue) { this.venue = venue; }

    @PropertyName("isClosed")
    public void setIsClosed(boolean isClosed) { this.isClosed = isClosed; }

    public void setAttendance(Map<String, Map<String, Boolean>> attendance) { this.attendance = attendance; }

    // ---------------- Helper ----------------
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(dateInMillis));
    }

    // Optional placeholder method
    public void setStatus(String notRecorded) {
        // Can implement if needed
    }
}
