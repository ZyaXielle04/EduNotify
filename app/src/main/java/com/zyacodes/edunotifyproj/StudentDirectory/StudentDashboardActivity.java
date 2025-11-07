package com.zyacodes.edunotifyproj.StudentDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvStudents, tvParents, tvActiveEvents, tvAnnouncements, tvAttendance;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, eventsRef, postsRef;
    private String currentUid;
    private ScrollView mainScrollView;

    private LinearLayout navDashboard, navHome, navEvents, navSettings;
    private Button btnSeePost, btnSeeEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // 🔹 Firebase setup
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersRef = db.getReference("users");
        eventsRef = db.getReference("events");
        postsRef = db.getReference("posts");

        // 🔹 Bind UI elements
        tvStudents = findViewById(R.id.tvStudents);
        tvParents = findViewById(R.id.tvParents);
        tvActiveEvents = findViewById(R.id.tvActiveEvents);
        tvAnnouncements = findViewById(R.id.tvAnnouncements);
        tvAttendance = findViewById(R.id.tvAttendance);

        mainScrollView = findViewById(R.id.mainScrollView);

        btnSeePost = findViewById(R.id.btnSeePost);
        btnSeeEvents = findViewById(R.id.btnSeeEvents);

        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings);

        // 🔹 Load student data
        loadStudentInfo();

        loadParentInfo();
        loadEventsCount();
        loadPostsCount();

        // 🔹 Quick actions
        btnSeePost.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentHomeActivity.class);
            startActivity(intent);
        });

        btnSeeEvents.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentEventsActivity.class);
            startActivity(intent);
        });

        // 🔹 Navigation bar
        setupNavigation();
    }

    // ===================================
    // 🔹 LOAD STUDENT INFO + ATTENDANCE
    // ===================================
    private void loadStudentInfo() {
        usersRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String studentNumber = snapshot.child("studentNumber").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);

                    if (studentNumber != null) {
                        tvStudents.setText(studentNumber);
                        loadStudentAttendance(studentNumber);
                    }

                    TextView welcomeText = findViewById(R.id.textTitle);
                    if (welcomeText != null && username != null) {
                        welcomeText.setText("Welcome, " + username + "!");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDashboardActivity.this, "Failed to load student info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===================================
    // 🔹 LOAD ATTENDANCE BASED ON TRUE/FALSE
    // ===================================
    // ===================================
// 🔹 LOAD ATTENDANCE BASED ON TRUE/FALSE AND SECTION
// ===================================
    private void loadStudentAttendance(String studentNumber) {
        // First, get the student's sectionCode
        usersRef.orderByChild("studentNumber").equalTo(studentNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String sectionCodeTemp = "";
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                sectionCodeTemp = childSnap.child("sectionCode").getValue(String.class);
                                break; // only need first match
                            }

                            final String sectionCode = sectionCodeTemp; // final for inner class
                            if (sectionCode == null || sectionCode.isEmpty()) {
                                tvAttendance.setText("No section info");
                                return;
                            }

                            // Now fetch attendance from /events/{eventId}/attendance/{sectionCode}/{studentNumber}
                            eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot eventsSnapshot) {
                                    int totalEvents = 0;
                                    int attendedCount = 0;

                                    for (DataSnapshot eventSnap : eventsSnapshot.getChildren()) {
                                        DataSnapshot attendanceSnap = eventSnap.child("attendance").child(sectionCode).child(studentNumber);
                                        if (attendanceSnap.exists()) {
                                            totalEvents++;
                                            Boolean attended = attendanceSnap.getValue(Boolean.class);
                                            if (Boolean.TRUE.equals(attended)) attendedCount++;
                                        }
                                    }

                                    int percentage = totalEvents == 0 ? 0 : (int) (((double) attendedCount / totalEvents) * 100);
                                    tvAttendance.setText(percentage + "%");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    tvAttendance.setText("Error");
                                }
                            });

                        } else {
                            tvAttendance.setText("Student not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvAttendance.setText("Error");
                    }
                });
    }

    // ===================================
    // 🔹 LOAD PARENT INFO
    // ===================================
    private void loadParentInfo() {
        usersRef.child(currentUid).child("studentNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String studentNumber = snapshot.getValue(String.class);
                if (studentNumber == null) return;

                usersRef.orderByChild("student").equalTo(studentNumber)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot parentSnap : snapshot.getChildren()) {
                                        String parentName = parentSnap.child("username").getValue(String.class);
                                        tvParents.setText(parentName != null ? parentName : "N/A");
                                        return;
                                    }
                                } else {
                                    tvParents.setText("Not Linked");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                tvParents.setText("Error");
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvParents.setText("Error");
            }
        });
    }

    // ===================================
    // 🔹 LOAD EVENTS COUNT
    // ===================================
    private void loadEventsCount() {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvActiveEvents.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvActiveEvents.setText("0");
            }
        });
    }

    // ===================================
    // 🔹 LOAD POSTS COUNT
    // ===================================
    private void loadPostsCount() {
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvAnnouncements.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvAnnouncements.setText("0");
            }
        });
    }

    // ===================================
    // 🔹 NAVIGATION BAR
    // ===================================
    private void setupNavigation() {
        navEvents.setOnClickListener(v -> openActivity(StudentEventsActivity.class, "Opening Events..."));
        navHome.setOnClickListener(v -> openActivity(StudentHomeActivity.class, "Opening Home..."));
        navDashboard.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your Dashboard", Toast.LENGTH_SHORT).show();
            }
        });
        navSettings.setOnClickListener(v -> openActivity(StudentSettingsActivity.class, "Opening Settings..."));
    }

    private void openActivity(Class<?> cls, String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, cls));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
