package com.zyacodes.edunotifyproj.AdminDirectory;

import android.app.Dialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zyacodes.edunotifyproj.Adapters.SectionAdapter;
import com.zyacodes.edunotifyproj.Models.Section;
import com.zyacodes.edunotifyproj.R;
import com.google.firebase.database.*;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdminSectionActivity extends AppCompatActivity {

    private Button btnAddSection;
    private DatabaseReference sectionsRef;
    private LinearLayout navDashboard, navHome, navUsers, navSections, navEvents, navReports, navSettings;
    private ScrollView mainScrollView;

    private RecyclerView recyclerSections;
    private List<Section> sectionList;
    private SectionAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_section);

        // Firebase reference: /sections
        sectionsRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("sections");

        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navUsers = findViewById(R.id.navUsers);
        navSections = findViewById(R.id.navSections);
        navEvents = findViewById(R.id.navEvents);
        navReports = findViewById(R.id.navReports);
        navSettings = findViewById(R.id.navSettings);
        mainScrollView = findViewById(R.id.mainScrollView);

        btnAddSection = findViewById(R.id.btnAddSection);
        btnAddSection.setOnClickListener(v -> showAddSectionDialog());

        recyclerSections = findViewById(R.id.recyclerSections);
        sectionList = new ArrayList<>();
        adapter = new SectionAdapter(sectionList);
        recyclerSections.setLayoutManager(new LinearLayoutManager(this));
        recyclerSections.setAdapter(adapter);

        // Load sections from Firebase
        loadSectionsFromFirebase();


        // Navigation buttons
        navDashboard.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Dashboard...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminDashboardActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your Section Management", Toast.LENGTH_SHORT).show();
            }
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

    android.os.Handler handler = new android.os.Handler();
    Runnable generateCodeRunnable = null;

    private void showAddSectionDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_section);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);

        // ✅ Resize dialog to 90% width
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.9);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Fields
        EditText etCollege = dialog.findViewById(R.id.etCollege);
        EditText etYearLevel = dialog.findViewById(R.id.etYearLevel);
        EditText etProgram = dialog.findViewById(R.id.etProgram);
        EditText etSection = dialog.findViewById(R.id.etSection);
        TextView tvGeneratedCode = dialog.findViewById(R.id.tvGeneratedCode);
        Button btnSave = dialog.findViewById(R.id.btnSaveSection);

        final String[] generatedCode = {""};

        // TextWatcher to monitor all fields
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(generateCodeRunnable);

                generateCodeRunnable = () -> {
                    String college = etCollege.getText().toString().trim();
                    String year = etYearLevel.getText().toString().trim();
                    String program = etProgram.getText().toString().trim();
                    String section = etSection.getText().toString().trim();

                    boolean allFilled = !college.isEmpty() && !year.isEmpty() && !program.isEmpty() && !section.isEmpty();

                    if (allFilled) {
                        generateUniqueCode(tvGeneratedCode, generatedCode);
                    } else {
                        tvGeneratedCode.setText("Section Code: ");
                        generatedCode[0] = "";
                    }
                };

                handler.postDelayed(generateCodeRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etCollege.addTextChangedListener(watcher);
        etYearLevel.addTextChangedListener(watcher);
        etProgram.addTextChangedListener(watcher);
        etSection.addTextChangedListener(watcher);

        // Save button
        btnSave.setOnClickListener(v -> {
            String college = etCollege.getText().toString().trim();
            String year = etYearLevel.getText().toString().trim();
            String program = etProgram.getText().toString().trim();
            String section = etSection.getText().toString().trim();

            if (college.isEmpty() || year.isEmpty() || program.isEmpty() || section.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (generatedCode[0].isEmpty()) {
                Toast.makeText(this, "Code not yet generated.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveSectionToFirebase(college, year, program, section, generatedCode[0], dialog);
        });

        dialog.show();
    }

    // ✅ Generate unique random code by checking Firebase
    private void generateUniqueCode(TextView tvGeneratedCode, String[] generatedCode) {
        String newCode = generateRandomCode();
        Log.d("SectionCodeGen", "Attempting to generate code: " + newCode);

        sectionsRef.child(newCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("SectionCodeGen", "Code already exists in Firebase: " + newCode + " — generating another...");
                    // If code already exists, generate another one
                    generateUniqueCode(tvGeneratedCode, generatedCode);
                } else {
                    generatedCode[0] = newCode;
                    tvGeneratedCode.setText("Generated Code: " + newCode);
                    Log.d("SectionCodeGen", "✅ Unique code generated: " + newCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SectionCodeGen", "Error checking code: " + error.getMessage());
                Toast.makeText(AdminSectionActivity.this, "Error checking code: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSectionToFirebase(String college, String year, String program, String section, String code, Dialog dialog) {
        Map<String, Object> sectionData = new HashMap<>();
        sectionData.put("college", college);
        sectionData.put("yearLevel", year);
        sectionData.put("program", program);
        sectionData.put("section", section);

        sectionsRef.child(code).setValue(sectionData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Section saved under code: " + code, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save section: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String generateRandomCode() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        for (int i = 0; i < 3; i++) {
            sb.append(random.nextInt(10));
        }

        String code = sb.toString();
        Log.d("SectionCodeGen", "Random code generated locally: " + code);
        return code;
    }

    private void loadSectionsFromFirebase() {
        sectionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sectionList.clear();
                for (DataSnapshot sectionSnap : snapshot.getChildren()) {
                    String code = sectionSnap.getKey();
                    Section section = sectionSnap.getValue(Section.class);
                    if (section != null) {
                        // Include the code since Firebase doesn’t include it in the object
                        sectionList.add(new Section(
                                section.getCollege(),
                                section.getYearLevel(),
                                section.getProgram(),
                                section.getSection(),
                                code
                        ));
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminSectionActivity.this, "Failed to load sections: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
