package com.zyacodes.edunotifyproj;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.zyacodes.edunotifyproj.AdminDirectory.AdminDashboardActivity;
import com.zyacodes.edunotifyproj.ParentDirectory.ParentDashboardActivity;
import com.zyacodes.edunotifyproj.StudentDirectory.StudentDashboardActivity;
import com.zyacodes.edunotifyproj.TeacherDirectory.TeacherDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView txtRegister;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // UI setup
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        txtRegister = findViewById(R.id.tvRegister);

        // Setup loading dialog
        setupLoadingDialog();

        // Login button
        btnLogin.setOnClickListener(v -> loginUser());

        // Register link
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
        loadingDialog.setContentView(view);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            showLoading();
            checkUserRole(currentUser.getUid());
        }
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter your username");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter your password");
            return;
        }

        showLoading();

        // Find user by username → get email for FirebaseAuth login
        Query query = usersRef.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    hideLoading();
                    Toast.makeText(LoginActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String email = userSnap.child("email").getValue(String.class);

                    if (email == null) {
                        hideLoading();
                        Toast.makeText(LoginActivity.this, "Email not found for this user.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Sign in with email and password
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            if (user.isEmailVerified()) {
                                                // ✅ Always use Firebase Auth UID
                                                checkUserRole(user.getUid());
                                            } else {
                                                hideLoading();
                                                Toast.makeText(LoginActivity.this,
                                                        "Please verify your email before logging in.",
                                                        Toast.LENGTH_LONG).show();
                                                mAuth.signOut();
                                            }
                                        }
                                    } else {
                                        hideLoading();
                                        Toast.makeText(LoginActivity.this,
                                                "Login failed: " + task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    break; // stop loop after first match
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRole(String uid) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideLoading();

                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    return;
                }

                String role = snapshot.child("role").getValue(String.class);
                Boolean isApproved = snapshot.child("isApproved").getValue(Boolean.class);

                Log.d("LOGIN_DEBUG", "role=" + role + ", isApproved=" + isApproved);

                Intent intent;

                if (role == null || role.isEmpty()) {
                    intent = new Intent(LoginActivity.this, RoleActivity.class);
                } else if ("admin".equalsIgnoreCase(role)) {
                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                } else if ("teacher".equalsIgnoreCase(role) && Boolean.TRUE.equals(isApproved)) {
                    intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                } else if ("student".equalsIgnoreCase(role) && Boolean.TRUE.equals(isApproved)) {
                    intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                } else if ("parent".equalsIgnoreCase(role) && Boolean.TRUE.equals(isApproved)) {
                    intent = new Intent(LoginActivity.this, ParentDashboardActivity.class);
                }else if (
                        ("teacher".equalsIgnoreCase(role) ||
                                "student".equalsIgnoreCase(role) ||
                                "parent".equalsIgnoreCase(role))
                                && (isApproved == null || !isApproved)
                ) {
                    intent = new Intent(LoginActivity.this, PendingPageActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                }

                startActivity(intent);
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        if (!loadingDialog.isShowing()) loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog.isShowing()) loadingDialog.dismiss();
    }
}
