package com.zyacodes.edunotifyproj.AdminDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zyacodes.edunotifyproj.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvVerifiedUsers, tvPendingUsers, tvTeachers, tvStudents, tvParents, tvActiveEvents, tvAnnouncements, tvAttendance;
    private LinearLayout cardVerifiedUsers, cardPendingUsers, cardTeachers, cardStudents, cardParents, cardEvents, cardPosts, cardAttendance, navReports, navDashboard, navHome, navUsers, navSections, navEvents, navSettings;

    private Button btnAddEvent, btnManageUsers;
    private ScrollView mainScrollView;

    private DatabaseReference usersRef, postsRef, eventsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Firebase references
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersRef = db.getReference("users");
        postsRef = db.getReference("posts");
        eventsRef = db.getReference("events");

        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navUsers = findViewById(R.id.navUsers);
        navSections = findViewById(R.id.navSections);
        navEvents = findViewById(R.id.navEvents);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);
        mainScrollView = findViewById(R.id.mainScrollView);

        // TextViews
        tvVerifiedUsers = findViewById(R.id.tvVerifiedUsers);
        tvPendingUsers = findViewById(R.id.tvPendingUsers);
        tvTeachers = findViewById(R.id.tvTeachers);
        tvStudents = findViewById(R.id.tvStudents);
        tvParents = findViewById(R.id.tvParents);
        tvActiveEvents = findViewById(R.id.tvActiveEvents);
        tvAnnouncements = findViewById(R.id.tvAnnouncements);
        tvAttendance = findViewById(R.id.tvAttendance);

        // Cards
        cardVerifiedUsers = findViewById(R.id.cardVerifiedUsers);
        cardPendingUsers = findViewById(R.id.cardPendingUsers);
        cardTeachers = findViewById(R.id.cardTeachers);
        cardStudents = findViewById(R.id.cardStudents);
        cardParents = findViewById(R.id.cardParents);
        cardEvents = findViewById(R.id.cardEvents);
        cardPosts = findViewById(R.id.cardPosts);
        cardAttendance = findViewById(R.id.cardAttendance);

        // Buttons
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnManageUsers = findViewById(R.id.btnManageUsers);

        // Fetch all data
        loadUserData();
        loadPostData();
        loadEventData();

        // Navigation intents (adjust these targets)
        btnAddEvent.setOnClickListener(v -> startActivity(new Intent(this, AdminEventsActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUsersActivity.class)));

        navDashboard.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your Dashboard", Toast.LENGTH_SHORT).show();
            }
        });

        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Opening News Feed...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminHomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Users...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminUsersActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navSections.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Section Manager...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminSectionActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navEvents.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Events / Reports...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminEventsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navReports.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Reports Manager...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminReportsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminSettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loadUserData() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int verified = 0, pending = 0, teachers = 0, students = 0, parents = 0;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    Boolean isApproved = userSnap.child("isApproved").getValue(Boolean.class);
                    String role = userSnap.child("role").getValue(String.class);

                    if (Boolean.FALSE.equals(isApproved)) {
                        pending++;
                    } else {
                        verified++;
                    }

                    if (role != null) {
                        switch (role) {
                            case "Teacher": teachers++; break;
                            case "Student": students++; break;
                            case "Parent": parents++; break;
                        }
                    }
                }

                tvVerifiedUsers.setText(String.valueOf(verified));
                tvPendingUsers.setText(String.valueOf(pending));
                tvTeachers.setText(String.valueOf(teachers));
                tvStudents.setText(String.valueOf(students));
                tvParents.setText(String.valueOf(parents));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadPostData() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long postCount = snapshot.getChildrenCount();
                tvAnnouncements.setText(String.valueOf(postCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadEventData() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long activeEvents = snapshot.getChildrenCount();
                tvActiveEvents.setText(String.valueOf(activeEvents));

                // Attendance average
                int totalAttendanceCount = 0;
                int totalPresentCount = 0;

                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    DataSnapshot attendanceSnap = eventSnap.child("attendance");
                    int total = 0;
                    int present = 0;

                    for (DataSnapshot userSnap : attendanceSnap.getChildren()) {
                        // userSnap = QF9RZ4
                        for (DataSnapshot innerSnap : userSnap.getChildren()) {
                            // innerSnap = 202235096 : true
                            Object value = innerSnap.getValue();
                            if (value instanceof Boolean) {
                                total++;
                                if ((Boolean) value) present++;
                            } else {
                                Log.w("AdminDashboard", "Unexpected attendance value: " + value);
                            }
                        }
                    }

                    totalAttendanceCount += total;
                    totalPresentCount += present;
                }

                if (totalAttendanceCount > 0) {
                    int percentage = (int) ((totalPresentCount * 100.0) / totalAttendanceCount);
                    tvAttendance.setText(percentage + "%");
                } else {
                    tvAttendance.setText("0%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
