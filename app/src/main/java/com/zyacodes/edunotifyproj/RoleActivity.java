package com.zyacodes.edunotifyproj;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoleActivity extends AppCompatActivity {

    private ImageView imageStudent, imageParent, imageTeacher;
    private Button buttonRegister;
    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role);

        // Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        // Bind UI
        imageStudent = findViewById(R.id.imageStudent);
        imageParent = findViewById(R.id.imageParent);
        imageTeacher = findViewById(R.id.imageTeacher);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Select role listeners
        imageStudent.setOnClickListener(v -> {
            selectedRole = "Student";
            highlightSelection("Student");
        });

        imageParent.setOnClickListener(v -> {
            selectedRole = "Parent";
            highlightSelection("Parent");
        });

        imageTeacher.setOnClickListener(v -> {
            selectedRole = "Teacher";
            highlightSelection("Teacher");
        });

        // Continue button
        buttonRegister.setOnClickListener(v -> {
            if (selectedRole.isEmpty()) {
                Toast.makeText(RoleActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                return;
            }

            // For teachers: save role immediately and mark pending approval
            if (selectedRole.equals("Teacher")) {
                String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                if (uid != null) {
                    usersRef.child(uid).child("role").setValue("Teacher");
                    usersRef.child(uid).child("isApproved").setValue(false);
                    Toast.makeText(this, "Role saved: Teacher (Pending approval)", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(RoleActivity.this, PendingPageActivity.class);
                startActivity(intent);
                finish();
            } else {
                // For Student or Parent → proceed to StudentNumberActivity
                Intent intent = new Intent(RoleActivity.this, StudentNumberActivity.class);
                intent.putExtra("selectedRole", selectedRole);
                startActivity(intent);
                finish();
            }
        });
    }

    private void highlightSelection(String role) {
        // Reset backgrounds first
        imageStudent.setBackgroundResource(android.R.color.white);
        imageParent.setBackgroundResource(android.R.color.white);
        imageTeacher.setBackgroundResource(android.R.color.white);

        // Highlight selected
        switch (role) {
            case "Student":
                imageStudent.setBackgroundResource(R.drawable.selector_role);
                break;
            case "Parent":
                imageParent.setBackgroundResource(R.drawable.selector_role);
                break;
            case "Teacher":
                imageTeacher.setBackgroundResource(R.drawable.selector_role);
                break;
        }
    }
}
