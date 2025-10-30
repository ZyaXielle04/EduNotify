package com.zyacodes.edunotifyproj.ParentDirectory;

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
import com.zyacodes.edunotifyproj.ParentDirectory.ParentEventsActivity;
import com.zyacodes.edunotifyproj.ParentDirectory.ParentHomeActivity;

public class ParentDashboardActivity extends AppCompatActivity {

    private TextView tvStudents, tvActiveEvents, tvAnnouncements, tvAttendance;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, eventsRef, postsRef;
    private String currentUid;
    private ScrollView mainScrollView;

    private LinearLayout navDashboard, navHome, navEvents, navSettings;
    private Button btnSeePost, btnSeeEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase db = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");
        usersRef = db.getReference("users");
        eventsRef = db.getReference("events");
        postsRef = db.getReference("posts");

        // Bind UI elements
        tvStudents = findViewById(R.id.tvStudents);
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

        // Load parent and child info
        loadChildInfo();
        loadEventsCount();
        loadPostsCount();

        // Quick actions
        btnSeePost.setOnClickListener(v -> startActivity(new Intent(this, ParentHomeActivity.class)));
        btnSeeEvents.setOnClickListener(v -> startActivity(new Intent(this, ParentEventsActivity.class)));

        // Navigation bar
        setupNavigation();
    }

    // Load child info
    private void loadChildInfo() {
        // Fetch "parent" field under current parent's UID
        usersRef.child(currentUid).child("student").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String parentNumber = snapshot.getValue(String.class);
                if (parentNumber == null) {
                    tvStudents.setText("No Child Linked");
                    tvAttendance.setText("0%");
                    return;
                }
                tvStudents.setText(parentNumber);
                loadChildAttendance(parentNumber); // fetch attendance for this parent number
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvStudents.setText("Error");
            }
        });
    }

    // Load attendance of child
    private void loadChildAttendance(String parentNumber) {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalEvents = 0;
                int attendedCount = 0;

                for (DataSnapshot eventSnap : snapshot.getChildren()) {
                    DataSnapshot attendanceSnap = eventSnap.child("attendance");
                    if (attendanceSnap.exists()) {
                        totalEvents++;
                        Boolean attended = attendanceSnap.child(parentNumber).getValue(Boolean.class);
                        if (attended != null && attended) attendedCount++;
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
    }

    // Load total events
    private void loadEventsCount() {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvActiveEvents.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvActiveEvents.setText("0");
            }
        });
    }

    // Load total posts
    private void loadPostsCount() {
        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvAnnouncements.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvAnnouncements.setText("0");
            }
        });
    }

    // Navigation
    private void setupNavigation() {
        navDashboard.setOnClickListener(v -> {
            if (mainScrollView != null) mainScrollView.smoothScrollTo(0, 0);
            Toast.makeText(this, "Back to top of Dashboard", Toast.LENGTH_SHORT).show();
        });
        navHome.setOnClickListener(v -> startActivity(new Intent(this, ParentHomeActivity.class)));
        navEvents.setOnClickListener(v -> startActivity(new Intent(this, ParentEventsActivity.class)));
        navSettings.setOnClickListener(v -> startActivity(new Intent(this, ParentSettingsActivity.class)));
    }
}
