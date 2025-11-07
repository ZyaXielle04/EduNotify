package com.zyacodes.edunotifyproj.TeacherDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zyacodes.edunotifyproj.R;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvStudents, tvParents, tvActiveEvents, tvAnnouncements, tvAttendance;
    private LinearLayout cardStudents, cardParents, cardEvents, cardPosts, cardAttendance;
    private LinearLayout navDashboard, navHome, navEvents, navSettings;

    private Button btnAddEvent;
    private ScrollView mainScrollView;

    private FirebaseDatabase db;
    private DatabaseReference usersRef, postsRef, eventsRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Firebase setup
        db = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersRef = db.getReference("users");
        postsRef = db.getReference("posts");
        eventsRef = db.getReference("events");
        auth = FirebaseAuth.getInstance();

        // Initialize layout components
        mainScrollView = findViewById(R.id.mainScrollView);
        btnAddEvent = findViewById(R.id.btnAddEvent);

        // Cards
        cardStudents = findViewById(R.id.cardStudents);
        cardParents = findViewById(R.id.cardParents);
        cardEvents = findViewById(R.id.cardEvents);
        cardPosts = findViewById(R.id.cardPosts);
        cardAttendance = findViewById(R.id.cardAttendance);

        // TextViews
        tvStudents = findViewById(R.id.tvStudents);
        tvParents = findViewById(R.id.tvParents);
        tvActiveEvents = findViewById(R.id.tvActiveEvents);
        tvAnnouncements = findViewById(R.id.tvAnnouncements);
        tvAttendance = findViewById(R.id.tvAttendance);

        // Navigation
        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings);

        // Load dashboard data
        loadUserData();
        loadEventData();
        loadPostData();

        // Quick Action
        btnAddEvent.setOnClickListener(v -> {
            Toast.makeText(this, "Opening event manager...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherEventsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Bottom Navbar Actions
        setupNavigation();
    }

    private void setupNavigation() {
        navDashboard.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your dashboard", Toast.LENGTH_SHORT).show();
            }
        });

        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Opening your News Feed...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherHomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navEvents.setOnClickListener(v -> {
            Toast.makeText(this, "Opening events...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherEventsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Opening settings...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TeacherSettingsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void loadUserData() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int studentCount = 0;
                int parentCount = 0;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String role = userSnap.child("role").getValue(String.class);
                    Boolean isApproved = userSnap.child("isApproved").getValue(Boolean.class);

                    if (Boolean.TRUE.equals(isApproved) && role != null) {
                        if (role.equalsIgnoreCase("Student")) studentCount++;
                        else if (role.equalsIgnoreCase("Parent")) parentCount++;
                    }
                }

                tvStudents.setText(String.valueOf(studentCount));
                tvParents.setText(String.valueOf(parentCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherDashboardActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventData() {
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long activeEvents = snapshot.getChildrenCount();
                tvActiveEvents.setText(String.valueOf(activeEvents));

                int totalAttendanceCount = 0;
                int totalPresentCount = 0;

                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    DataSnapshot attendanceSnap = eventSnap.child("attendance");
                    if (attendanceSnap.exists()) {
                        // Iterate over sections
                        for (DataSnapshot sectionSnap : attendanceSnap.getChildren()) {
                            // Iterate over students in section
                            for (DataSnapshot studentSnap : sectionSnap.getChildren()) {
                                Boolean attended = studentSnap.getValue(Boolean.class);
                                if (attended != null && attended) totalPresentCount++;
                                totalAttendanceCount++;
                            }
                        }
                    }
                }

                // Calculate percentage
                if (totalAttendanceCount > 0) {
                    int percentage = (int) ((totalPresentCount * 100.0) / totalAttendanceCount);
                    tvAttendance.setText(percentage + "%");
                } else {
                    tvAttendance.setText("0%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherDashboardActivity.this, "Failed to load event data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData() {
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long postCount = 0;
                String currentUID = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    String postedBy = postSnap.child("postedBy").getValue(String.class);
                    if (postedBy != null && postedBy.equals(currentUID)) {
                        postCount++;
                    }
                }

                tvAnnouncements.setText(String.valueOf(postCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherDashboardActivity.this, "Failed to load post data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
