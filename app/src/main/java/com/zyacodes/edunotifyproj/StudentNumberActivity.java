package com.zyacodes.edunotifyproj;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentNumberActivity extends AppCompatActivity {

    private TextInputEditText editStudentNumber, editSectionCode;
    private View sectionCodeLayout;
    private Button buttonSubmitStudentNo;
    private TextView textBack, textTitle, textSubtitle;
    private ImageView iconBell;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String selectedRole;

    private AlertDialog loadingDialog;
    private TextView tvLoadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_number);

        // ✅ Get selected role from RoleActivity
        selectedRole = getIntent().getStringExtra("selectedRole");

        // ✅ Firebase setup
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // ✅ Bind UI components
        editStudentNumber = findViewById(R.id.editStudentNumber);
        editSectionCode = findViewById(R.id.editSectionCode);
        sectionCodeLayout = (View) editSectionCode.getParent().getParent();
        buttonSubmitStudentNo = findViewById(R.id.buttonSubmitStudentNo);
        textBack = findViewById(R.id.textBack);
        textTitle = findViewById(R.id.textTitle);
        textSubtitle = findViewById(R.id.textSubtitle);
        iconBell = findViewById(R.id.iconBell);

        // ✅ Update displayed text based on selected role
        if (selectedRole != null) {
            if (selectedRole.equalsIgnoreCase("Student")) {
                textTitle.setText("Student Number");
                textSubtitle.setText("Please enter your Student No. and Section Code to continue");
                editStudentNumber.setHint("Enter your Student No.");
                sectionCodeLayout.setVisibility(View.VISIBLE); // show section code
            } else if (selectedRole.equalsIgnoreCase("Parent")) {
                textTitle.setText("Student Number");
                textSubtitle.setText("Please enter a Student No. to continue");
                editStudentNumber.setHint("Enter the Student No.");
                sectionCodeLayout.setVisibility(View.GONE); // hide section code
            } else {
                textSubtitle.setText("Please enter the Student ID to continue");
                sectionCodeLayout.setVisibility(View.GONE);
            }
        }

        // ✅ Submit button listener
        buttonSubmitStudentNo.setOnClickListener(v -> validateStudentNumber());

        // ✅ Go back to role selection
        textBack.setOnClickListener(v -> {
            Intent intent = new Intent(StudentNumberActivity.this, RoleActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // ==============================
    // 🔹 VALIDATION LOGIC
    // ==============================
    private void validateStudentNumber() {
        String studentNumber = editStudentNumber.getText() != null
                ? editStudentNumber.getText().toString().trim()
                : "";
        String sectionCode = editSectionCode.getText() != null
                ? editSectionCode.getText().toString().trim()
                : "";

        if (studentNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a student number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRole == null) {
            Toast.makeText(this, "Role not found. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRole.equalsIgnoreCase("Student") && sectionCode.isEmpty()) {
            Toast.makeText(this, "Please enter a section code", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Checking student number...");

        // ✅ Student: check if student number already exists
        if (selectedRole.equalsIgnoreCase("Student")) {
            usersRef.orderByChild("studentNumber").equalTo(studentNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            hideLoading();
                            if (snapshot.exists()) {
                                Toast.makeText(StudentNumberActivity.this,
                                        "This student number is already registered.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                saveStudentData(studentNumber, sectionCode);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            hideLoading();
                            Toast.makeText(StudentNumberActivity.this,
                                    "Error checking student number: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            // ✅ Parent: check if student number exists
        } else if (selectedRole.equalsIgnoreCase("Parent")) {
            usersRef.orderByChild("studentNumber").equalTo(studentNumber)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            hideLoading();
                            if (snapshot.exists()) {
                                saveStudentData(studentNumber, null);
                            } else {
                                Toast.makeText(StudentNumberActivity.this,
                                        "Student number not found. Please check and try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            hideLoading();
                            Toast.makeText(StudentNumberActivity.this,
                                    "Error checking student number: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // ✅ Other roles (if any)
            hideLoading();
            saveStudentData(studentNumber, sectionCode);
        }
    }

    // ==============================
    // 🔹 SAVE USER DATA TO FIREBASE
    // ==============================
    private void saveStudentData(String studentNumber, String sectionCode) {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Saving student data...");

        DatabaseReference userRef = usersRef.child(uid);

        // Get parent's username first if role == "Parent"
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.exists() ? snapshot.getValue(String.class) : "Parent";

                // Case 1: Student registration
                if (selectedRole.equalsIgnoreCase("Student")) {
                    userRef.child("role").setValue("Student");
                    userRef.child("studentNumber").setValue(studentNumber);
                    userRef.child("sectionCode").setValue(sectionCode);
                    userRef.child("isApproved").setValue(false)
                            .addOnSuccessListener(aVoid -> {
                                hideLoading();
                                Toast.makeText(StudentNumberActivity.this, "Student data saved successfully!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(StudentNumberActivity.this, PendingPageActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                hideLoading();
                                Toast.makeText(StudentNumberActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                // Case 2: Parent registration
                else if (selectedRole.equalsIgnoreCase("Parent")) {
                    userRef.child("role").setValue("Parent");
                    userRef.child("student").setValue(studentNumber);
                    userRef.child("isApproved").setValue(false)
                            .addOnSuccessListener(aVoid -> {
                                // Link parent username to student’s record
                                usersRef.orderByChild("studentNumber").equalTo(studentNumber)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    for (DataSnapshot studentSnap : snapshot.getChildren()) {
                                                        studentSnap.getRef().child("parent").setValue(username);
                                                    }
                                                }
                                                hideLoading();
                                                Toast.makeText(StudentNumberActivity.this, "Parent linked successfully!", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(StudentNumberActivity.this, PendingPageActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                hideLoading();
                                                Toast.makeText(StudentNumberActivity.this, "Error linking parent: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                hideLoading();
                                Toast.makeText(StudentNumberActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                // Case 3: Other roles
                else {
                    userRef.child("role").setValue(selectedRole);
                    userRef.child("studentNumber").setValue(studentNumber);
                    userRef.child("sectionCode").setValue(sectionCode);
                    userRef.child("isApproved").setValue(false)
                            .addOnSuccessListener(aVoid -> {
                                hideLoading();
                                Toast.makeText(StudentNumberActivity.this, "Data saved successfully!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(StudentNumberActivity.this, PendingPageActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                hideLoading();
                                Toast.makeText(StudentNumberActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(StudentNumberActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ==============================
    // 🔹 LOADING DIALOG HELPERS
    // ==============================
    private void showLoading(String message) {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
            tvLoadingText = view.findViewById(R.id.tvLoadingText);
            ProgressBar progressBar = view.findViewById(R.id.progressBar);

            tvLoadingText.setText(message);
            builder.setView(view);
            builder.setCancelable(false);
            loadingDialog = builder.create();
        } else {
            tvLoadingText.setText(message);
        }
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
