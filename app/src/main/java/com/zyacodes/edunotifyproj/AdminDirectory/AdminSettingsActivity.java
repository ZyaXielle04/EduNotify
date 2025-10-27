package com.zyacodes.edunotifyproj.AdminDirectory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zyacodes.edunotifyproj.R;

import java.io.File;
import java.text.DecimalFormat;

public class AdminSettingsActivity extends AppCompatActivity {

    // Profile
    private EditText edtUsername, edtEmail;
    private Button btnSaveUsername;

    // Security
    private Button btnChangePassword;

    // App
    private Button btnClearCache, btnLogout;
    private TextView txtCacheSize;

    // Bottom Navbar
    private LinearLayout navDashboard, navHome, navUsers, navEvents, navSettings;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AdminSettingsPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_settings);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users").child(uid);

        // SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnClearCache = findViewById(R.id.btnClearCache);
        btnSaveUsername = findViewById(R.id.btnSaveUsername);
        btnLogout = findViewById(R.id.btnLogout);

        // Create cache size TextView dynamically
        txtCacheSize = new TextView(this);
        txtCacheSize.setTextColor(getResources().getColor(android.R.color.darker_gray));
        txtCacheSize.setTextSize(14f);
        txtCacheSize.setPadding(8, 4, 8, 16);
        ((LinearLayout) btnClearCache.getParent()).addView(txtCacheSize);

        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navUsers = findViewById(R.id.navUsers);
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings);

        // Disable email field (not editable)
        edtEmail.setEnabled(false);
        edtEmail.setFocusable(false);
        edtEmail.setClickable(false);

        // Load from Firebase
        loadUserData(uid);

        btnLogout.setOnClickListener(v -> logout());


        // Save updated username to Firebase
        btnSaveUsername.setOnClickListener(v -> {
            String newUsername = edtUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            userRef.child("username").setValue(newUsername)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show());
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            if (currentUser.getEmail() != null) {
                mAuth.sendPasswordResetEmail(currentUser.getEmail())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Password reset email sent to " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Cache
        updateCacheSizeDisplay();
        btnClearCache.setOnClickListener(v -> {
            clearAppCache();
            Toast.makeText(this, "App cache cleared", Toast.LENGTH_SHORT).show();
            updateCacheSizeDisplay();
        });

        // Navbar navigation
        navDashboard.setOnClickListener(v -> openActivity(AdminDashboardActivity.class));
        navHome.setOnClickListener(v -> openActivity(AdminHomeActivity.class));
        navUsers.setOnClickListener(v -> openActivity(AdminUsersActivity.class));
        navEvents.setOnClickListener(v -> openActivity(AdminEventsActivity.class));
        navSettings.setOnClickListener(v -> Toast.makeText(this, "Already in Settings", Toast.LENGTH_SHORT).show());


    }

    private void loadUserData(String uid) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    edtUsername.setHint(username != null ? username : "Username");
                    edtEmail.setHint(email != null ? email : "Email");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminSettingsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void logout() {
        mAuth.signOut();
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, com.zyacodes.edunotifyproj.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- CACHE HANDLING ---
    private void clearAppCache() {
        try {
            deleteDir(getCacheDir());
            File externalCache = getExternalCacheDir();
            if (externalCache != null) deleteDir(externalCache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void updateCacheSizeDisplay() {
        long cacheSizeBytes = getDirSize(getCacheDir());
        File externalCache = getExternalCacheDir();
        if (externalCache != null) cacheSizeBytes += getDirSize(externalCache);

        String formattedSize = formatFileSize(cacheSizeBytes);
        txtCacheSize.setText("Cache Size: " + formattedSize);
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) size += file.length();
                    else size += getDirSize(file);
                }
            }
        }
        return size;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
