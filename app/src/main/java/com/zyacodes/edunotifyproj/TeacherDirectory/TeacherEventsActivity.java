package com.zyacodes.edunotifyproj.TeacherDirectory;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.AdapterView;

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
import com.zyacodes.edunotifyproj.Adapters.EventsAdapter;
import com.zyacodes.edunotifyproj.Models.Event;
import com.zyacodes.edunotifyproj.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TeacherEventsActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerCalendar;
    private ImageButton fabAddEvent;

    private List<Event> eventList = new ArrayList<>();
    private EventsAdapter eventsAdapter;

    private LinearLayout navDashboard, navHome, navUsers, navEvents, navSettings;
    private ScrollView mainScrollView;
    private FirebaseAuth mAuth;

    private long selectedDate; // selected date in millis
    private DatabaseReference eventsRef;
    private DatabaseReference usersRef;
    private DatabaseReference sectionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_events);

        initViews();
        setupFirebase();
        setupRecyclerView();
        setupCalendar();
        setupNavigation();
        setupFab();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerCalendar = findViewById(R.id.recyclerCalendar);
        fabAddEvent = findViewById(R.id.fabAddEvent);

        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navUsers = findViewById(R.id.navUsers);
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings);
        mainScrollView = findViewById(R.id.mainScrollView);

        mAuth = FirebaseAuth.getInstance();
    }

    private void setupFirebase() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");
        eventsRef = db.getReference("events");
        usersRef = db.getReference("users");
        sectionsRef = db.getReference("sections");
    }

    private void setupRecyclerView() {
        eventsAdapter = new EventsAdapter(eventList, this, false);
        recyclerCalendar.setLayoutManager(new LinearLayoutManager(this));
        recyclerCalendar.setAdapter(eventsAdapter);
    }

    private void setupCalendar() {
        selectedDate = System.currentTimeMillis();
        loadEventsForDate(selectedDate);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = getDateInMillis(year, month, dayOfMonth);
            loadEventsForDate(selectedDate);
        });
    }

    private void setupFab() {
        fabAddEvent.setOnClickListener(v -> openEventDialog(null));
    }

    private void setupNavigation() {
        navDashboard.setOnClickListener(v -> openActivity(TeacherDashboardActivity.class, "Opening Dashboard..."));
        navHome.setOnClickListener(v -> openActivity(TeacherHomeActivity.class, "Opening Home..."));
        navEvents.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your Event Manager", Toast.LENGTH_SHORT).show();
            }
        });
        navSettings.setOnClickListener(v -> openActivity(TeacherSettingsActivity.class, "Opening Settings..."));
    }

    private void openActivity(Class<?> cls, String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, cls));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ---------------------- Event Dialog ----------------------
    public void openEventDialog(Event eventToEdit) {
        boolean isEdit = eventToEdit != null;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_event);
        dialog.setCancelable(true);

        EditText edtTitle = dialog.findViewById(R.id.edtTitle);
        EditText edtDescription = dialog.findViewById(R.id.edtDescription);
        EditText edtVenue = dialog.findViewById(R.id.edtVenue);
        EditText edtDate = dialog.findViewById(R.id.edtDate);
        EditText edtTime = dialog.findViewById(R.id.edtTime);
        Button btnSave = dialog.findViewById(R.id.btnSaveEvent);
        Button btnCancel = dialog.findViewById(R.id.btnCancelEvent);

        final long[] selectedDateInMillis = new long[1];
        final String[] selectedTime = new String[1];

        if (isEdit) {
            edtTitle.setText(eventToEdit.getTitle());
            edtDescription.setText(eventToEdit.getDescription());
            edtVenue.setText(eventToEdit.getVenue());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            edtDate.setText(sdf.format(eventToEdit.getDateInMillis()));
            selectedDateInMillis[0] = eventToEdit.getDateInMillis();
            edtTime.setText(eventToEdit.getTime());
            selectedTime[0] = eventToEdit.getTime();
        }

        edtDate.setOnClickListener(v -> showDatePicker(edtDate, selectedDateInMillis));
        edtTime.setOnClickListener(v -> showTimePicker(edtTime, selectedTime));

        btnSave.setOnClickListener(v -> saveEvent(isEdit, eventToEdit, selectedDateInMillis[0], selectedTime[0], edtTitle, edtDescription, edtVenue, dialog));
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDatePicker(EditText edtDate, long[] selectedDateInMillis) {
        Calendar calendar = Calendar.getInstance();
        if (selectedDateInMillis[0] != 0) calendar.setTimeInMillis(selectedDateInMillis[0]);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedDateInMillis[0] = calendar.getTimeInMillis();
                    edtDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker(EditText edtTime, String[] selectedTime) {
        Calendar calendar = Calendar.getInstance();
        if (selectedTime[0] != null) {
            String[] parts = selectedTime[0].split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        }

        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    edtTime.setText(selectedTime[0]);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePicker.show();
    }

    private void saveEvent(boolean isEdit, Event eventToEdit, long dateInMillis, String time,
                           EditText edtTitle, EditText edtDescription, EditText edtVenue, Dialog dialog) {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String venue = edtVenue.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || dateInMillis == 0 || time == null) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEdit) {
            Event updatedEvent = new Event(eventToEdit.getId(), dateInMillis, title, description, time, venue,
                    eventToEdit.isClosed(), eventToEdit.getAttendance());
            eventsRef.child(eventToEdit.getId()).setValue(updatedEvent).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Event updated!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadEventsForDate(dateInMillis);
                } else Toast.makeText(this, "Failed to update event", Toast.LENGTH_SHORT).show();
            });
        } else {
            String key = eventsRef.push().getKey();
            if (key != null) {
                usersRef.orderByChild("role").equalTo("Student")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Boolean> attendanceMap = new HashMap<>();
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String studentNumber = userSnap.child("studentNumber").getValue(String.class);
                                    if (studentNumber != null) attendanceMap.put(studentNumber, false);
                                }
                                Map<String, Map<String, Boolean>> nestedAttendance = new HashMap<>();
                                nestedAttendance.put("allStudents", attendanceMap);

                                Event newEvent = new Event(key, dateInMillis, title, description, time, venue, false, nestedAttendance);
                                eventsRef.child(key).setValue(newEvent).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(TeacherEventsActivity.this, "Event added!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadEventsForDate(dateInMillis);
                                    } else Toast.makeText(TeacherEventsActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(TeacherEventsActivity.this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private long getDateInMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void loadEventsForDate(long dateInMillis) {
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> filtered = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Event event = child.getValue(Event.class);
                    if (event != null && isSameDay(event.getDateInMillis(), dateInMillis)) {
                        filtered.add(event);
                    }
                }
                if (eventsAdapter != null) {
                    eventsAdapter.updateList(filtered);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isSameDay(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        cal2.setTimeInMillis(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    // ---------------------- Updated Attendance Dialog ----------------------
    public void openAttendanceDialog(Event event) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_attendance);
        dialog.setCancelable(true);

        LinearLayout container = dialog.findViewById(R.id.attendanceContainer);
        Button btnSave = dialog.findViewById(R.id.btnSaveAttendance);
        Spinner spinnerSections = dialog.findViewById(R.id.spinnerSections);

        if (container != null) container.removeAllViews();

        // ------------------ Set dialog width to 90% ------------------
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.9); // 90% of screen width
            int maxHeight = (int) (metrics.heightPixels * 0.8); // 80% max height
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            ScrollView scrollView = dialog.findViewById(R.id.scrollAttendance);
            if (scrollView != null) {
                scrollView.post(() -> {
                    if (scrollView.getHeight() > maxHeight) {
                        scrollView.getLayoutParams().height = maxHeight;
                        scrollView.requestLayout();
                    }
                });
            }
        }

        // Load sections
        List<String> sectionCodes = new ArrayList<>();
        List<String> sectionLabels = new ArrayList<>();
        sectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot secSnap : snapshot.getChildren()) {
                    String code = secSnap.getKey();
                    String college = secSnap.child("college").getValue(String.class);
                    String program = secSnap.child("program").getValue(String.class);
                    String yearLevel = secSnap.child("yearLevel").getValue(String.class);
                    String section = secSnap.child("section").getValue(String.class);

                    sectionCodes.add(code);
                    sectionLabels.add(college + " | " + program + " - " + yearLevel + section);
                }
                spinnerSections.setAdapter(new ArrayAdapter<>(TeacherEventsActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, sectionLabels));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        spinnerSections.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSection = sectionCodes.get(position);
                container.removeAllViews();

                usersRef.orderByChild("role").equalTo("Student")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String role = userSnap.child("role").getValue(String.class);
                                    String studentSection = userSnap.child("sectionCode").getValue(String.class);
                                    String studentNumber = userSnap.child("studentNumber").getValue(String.class);
                                    String username = userSnap.child("username").getValue(String.class);

                                    if ("Student".equalsIgnoreCase(role) && selectedSection.equals(studentSection) &&
                                            studentNumber != null && username != null) {
                                        CheckBox checkBox = new CheckBox(TeacherEventsActivity.this);
                                        checkBox.setText(username + " (" + studentNumber + ")");
                                        checkBox.setTag(studentNumber);

                                        if (event.getAttendance() != null) {
                                            Object sectionObj = event.getAttendance().get(selectedSection);
                                            if (sectionObj instanceof Map) {
                                                Map<String, Object> sectionMap = (Map<String, Object>) sectionObj;
                                                Object val = sectionMap.get(studentNumber);
                                                if (val instanceof Boolean && (Boolean) val) checkBox.setChecked(true);
                                            }
                                        }

                                        checkBox.setEnabled(!event.isClosed());
                                        container.addView(checkBox);
                                    }
                                }

                                btnSave.setVisibility(event.isClosed() ? View.GONE : View.VISIBLE);
                                btnSave.setOnClickListener(v -> {
                                    Map<String, Object> attendanceData = new HashMap<>();
                                    for (int i = 0; i < container.getChildCount(); i++) {
                                        View view = container.getChildAt(i);
                                        if (view instanceof CheckBox) {
                                            CheckBox cb = (CheckBox) view;
                                            attendanceData.put((String) cb.getTag(), cb.isChecked());
                                        }
                                    }

                                    eventsRef.child(event.getId())
                                            .child("attendance")
                                            .child(selectedSection)
                                            .updateChildren(attendanceData)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(TeacherEventsActivity.this, "Attendance saved!", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                } else {
                                                    Toast.makeText(TeacherEventsActivity.this, "Failed to save attendance", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(TeacherEventsActivity.this, "Failed to load students", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        dialog.show();
    }
}
