package com.zyacodes.edunotifyproj.AdminDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.zyacodes.edunotifyproj.Adapters.ReportsAdapter;
import com.zyacodes.edunotifyproj.Models.Event;
import com.zyacodes.edunotifyproj.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminReportsActivity extends AppCompatActivity {

    private RecyclerView recyclerReports;
    private DatabaseReference eventsRef;
    private List<Event> eventList = new ArrayList<>();
    private ReportsAdapter adapter;

    private TextView reportTitle, reportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        recyclerReports = findViewById(R.id.recyclerReports);
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));

        reportTitle = findViewById(R.id.reportTitle);
        reportText = findViewById(R.id.reportText);

        adapter = new ReportsAdapter(eventList, this::generatePdfForEvent);
        recyclerReports.setAdapter(adapter);

        eventsRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("events");

        loadEvents();
        setupBottomNav();
    }

    private void loadEvents() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventList.clear();

                int totalEvents = 0;
                int totalAttendees = 0;

                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    Event event = eventSnap.getValue(Event.class);
                    if (event != null) {
                        eventList.add(event);
                        totalEvents++;

                        // Count total attendance
                        Map<String, Map<String, Boolean>> attendance = event.getAttendance();
                        if (attendance != null) {
                            for (Map<String, Boolean> section : attendance.values()) {
                                if (section != null) {
                                    totalAttendees += section.size();
                                }
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                updateReportSummary(totalEvents, totalAttendees);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminReportsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReportSummary(int totalEvents, int totalAttendees) {
        if (totalEvents == 0) {
            reportText.setText("No event data available yet.");
            return;
        }

        double averageAttendance = (double) totalAttendees / totalEvents;

        String summary = "\uD83D\uDDD3\uFE0F Total Events: " + totalEvents +
                "\n\uD83D\uDC64 Total Attendees: " + totalAttendees +
                "\n\uD83D\uDC65 Average Attendance per Event: " + String.format("%.1f", averageAttendance);

        reportTitle.setText("Overall Attendance Summary");
        reportText.setText(summary);
    }

    private void generatePdfForEvent(Event event) {
        Document document = new Document();
        String fileName = "Event_Report_" + event.getTitle().replaceAll("\\s+", "_") + ".pdf";
        String filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            document.add(new Paragraph("Event Attendance Report"));
            document.add(new Paragraph("--------------------------------"));
            document.add(new Paragraph("Title: " + event.getTitle()));
            document.add(new Paragraph("Date: " + event.getFormattedDate()));
            document.add(new Paragraph("Time: " + event.getTime()));
            document.add(new Paragraph("Venue: " + event.getVenue()));
            document.add(new Paragraph("Status: " + (event.isClosed() ? "Closed" : "Open")));
            document.add(new Paragraph("\nDescription:\n" + event.getDescription()));
            document.add(new Paragraph("\n"));

            Map<String, Map<String, Boolean>> attendance = event.getAttendance();
            if (attendance != null && !attendance.isEmpty()) {
                document.add(new Paragraph("Attendance Summary"));
                document.add(new Paragraph("--------------------------------"));
                int totalCount = 0;

                for (Map.Entry<String, Map<String, Boolean>> sectionEntry : attendance.entrySet()) {
                    String sectionCode = sectionEntry.getKey();
                    Map<String, Boolean> students = sectionEntry.getValue();
                    int count = students != null ? students.size() : 0;
                    totalCount += count;

                    document.add(new Paragraph("Section: " + sectionCode + " → " + count + " attendee(s)"));
                    for (String studentNum : students.keySet()) {
                        document.add(new Paragraph("    • " + studentNum));
                    }
                    document.add(new Paragraph(""));
                }

                document.add(new Paragraph("Total Attendees: " + totalCount));
            } else {
                document.add(new Paragraph("No attendance records found for this event."));
            }

            document.close();
            Toast.makeText(this, "PDF saved to: " + filePath, Toast.LENGTH_LONG).show();

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        LinearLayout navDashboard = findViewById(R.id.navDashboard);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navUsers = findViewById(R.id.navUsers);
        LinearLayout navSections = findViewById(R.id.navSections);
        LinearLayout navEvents = findViewById(R.id.navEvents);
        LinearLayout navReports = findViewById(R.id.navReports);
        LinearLayout navSettings = findViewById(R.id.navSettings);

        navDashboard.setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));
        navHome.setOnClickListener(v -> startActivity(new Intent(this, AdminHomeActivity.class)));
        navUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUsersActivity.class)));
        navSections.setOnClickListener(v -> startActivity(new Intent(this, AdminSectionActivity.class)));
        navEvents.setOnClickListener(v -> startActivity(new Intent(this, AdminEventsActivity.class)));
        navReports.setOnClickListener(v -> Toast.makeText(this, "You’re already here", Toast.LENGTH_SHORT).show());
        navSettings.setOnClickListener(v -> startActivity(new Intent(this, AdminSettingsActivity.class)));
    }
}
