package com.zyacodes.edunotifyproj;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private TextView txtLogin;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance(
                "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("users");

        // Bind UI
        etUsername = findViewById(R.id.editUsername);
        etEmail = findViewById(R.id.editEmail);
        etPassword = findViewById(R.id.editPassword);
        etConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        txtLogin = findViewById(R.id.txtLogin);

        // Create account button
        btnCreateAccount.setOnClickListener(v -> registerUser());

        // Redirect to LoginActivity
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // === Basic validation ===
        if (TextUtils.isEmpty(username)) { etUsername.setError("Enter username"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Enter email"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Enter password"); return; }
        if (password.length() < 6) { etPassword.setError("Password must be >= 6 chars"); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match"); return; }

        // === Check if email already exists ===
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult result = task.getResult();
                        if (result != null && !result.getSignInMethods().isEmpty()) {
                            etEmail.setError("Email already registered");
                            Toast.makeText(this, "This email is already in use.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Check username in /users
                            usersRef.orderByChild("username").equalTo(username).get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful()) {
                                            if (userTask.getResult().exists()) {
                                                etUsername.setError("Username already taken");
                                                Toast.makeText(this, "This username is already taken.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Proceed to register
                                                createFirebaseAccount(username, email, password);
                                            }
                                        } else {
                                            Toast.makeText(this, "Error checking username. Try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Error checking email. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createFirebaseAccount(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Save only username and email
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);

                        usersRef.child(uid).setValue(userMap)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        // Send email verification
                                        mAuth.getCurrentUser().sendEmailVerification()
                                                .addOnCompleteListener(verifyTask -> {
                                                    if (verifyTask.isSuccessful()) {
                                                        Toast.makeText(this,
                                                                "Registration successful! Check your email to verify your account.",
                                                                Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(this,
                                                                "Failed to send verification email.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }

                                                    // Redirect to RoleActivity instead of signing out immediately
                                                    Intent intent = new Intent(this, RoleActivity.class);
                                                    intent.putExtra("uid", uid); // optionally pass UID
                                                    startActivity(intent);
                                                    finish();
                                                });
                                    } else {
                                        Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
