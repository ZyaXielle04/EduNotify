package com.zyacodes.edunotifyproj.AdminDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zyacodes.edunotifyproj.Adapters.UserAdapter;
import com.zyacodes.edunotifyproj.Models.UserModel;
import com.zyacodes.edunotifyproj.R;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserModel> userList, filteredList;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private EditText searchUsers;
    private Spinner filterRole, filterApproval;

    // Navbar items
    private LinearLayout navDashboard, navHome, navUsers, navSections, navEvents, navReports, navSettings, navLogout;
    private ScrollView mainScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance(
                        "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new UserAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);

        // Search & filters
        searchUsers = findViewById(R.id.searchUsers);
        filterRole = findViewById(R.id.filterRole);
        filterApproval = findViewById(R.id.filterApproval);

        // Navbar setup
        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navUsers = findViewById(R.id.navUsers);
        navSections = findViewById(R.id.navSections);
        navEvents = findViewById(R.id.navEvents);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);
        mainScrollView = findViewById(R.id.mainScrollView);

        // Load all users
        loadUsers();

        // Search text listener
        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Role filter listener
        filterRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) { applyFilters(); }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Approval filter listener
        filterApproval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) { applyFilters(); }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // ====================
        // NAVBAR CLICK LISTENERS
        // ====================
        navDashboard.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Dashboard...", Toast.LENGTH_SHORT).show();
             startActivity(new Intent(this, AdminDashboardActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navHome.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Home...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminHomeActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        navUsers.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of User Management Section...", Toast.LENGTH_SHORT).show();
            }
        });

        navSections.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Section Manager", Toast.LENGTH_SHORT).show();
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

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    UserModel user = userSnap.getValue(UserModel.class);
                    if (user != null) {
                        user.setUid(userSnap.getKey());

                        // Only show Teacher, Student, Parent
                        if (user.getRole() != null && (
                                user.getRole().equals("Teacher") ||
                                        user.getRole().equals("Student") ||
                                        user.getRole().equals("Parent"))) {
                            userList.add(user);
                        }
                    }
                }

                applyFilters(); // Apply filters & search after loading
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String searchText = searchUsers.getText().toString().toLowerCase();
        String selectedRole = filterRole.getSelectedItem().toString();
        String selectedApproval = filterApproval.getSelectedItem().toString();

        filteredList.clear();

        for (UserModel user : userList) {
            boolean matchesSearch = user.getUsername().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText) ||
                    (user.getStudentNumber() != null && user.getStudentNumber().toLowerCase().contains(searchText));

            boolean matchesRole = selectedRole.equals("All") || user.getRole().equals(selectedRole);

            boolean matchesApproval;
            if (selectedApproval.equals("All")) {
                matchesApproval = true;
            } else if (selectedApproval.equals("Approved")) {
                matchesApproval = user.isApproved();
            } else { // Not Approved
                matchesApproval = !user.isApproved();
            }

            if (matchesSearch && matchesRole && matchesApproval) {
                filteredList.add(user);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
