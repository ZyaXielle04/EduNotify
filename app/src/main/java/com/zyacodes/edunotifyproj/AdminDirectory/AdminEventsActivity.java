package com.zyacodes.edunotifyproj.AdminDirectory;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CalendarView;

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
import com.zyacodes.edunotifyproj.Interfaces.EventActionListener;
import com.zyacodes.edunotifyproj.Models.Event;
import com.zyacodes.edunotifyproj.R;
<<<<<<< HEAD
import com.zyacodes.edunotifyproj.utils.EmailSender; // <-- Added import
=======
>>>>>>> 2c8201d26e015646d160557817c446570b615c65

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEventsActivity extends AppCompatActivity implements EventActionListener {

    private CalendarView calendarView;
    private RecyclerView recyclerCalendar;
    private ImageButton fabAddEvent;

    private List<Event> eventList = new ArrayList<>();
    private EventsAdapter eventsAdapter;

    private LinearLayout navDashboard, navHome, navUsers, navEvents, navSettings;
    private ScrollView mainScrollView;
    private FirebaseAuth mAuth;

    private long selectedDate;
    private DatabaseReference eventsRef;
    private DatabaseReference usersRef;

<<<<<<< HEAD
=======

>>>>>>> 2c8201d26e015646d160557817c446570b615c65
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_events);

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
    }

    private void setupRecyclerView() {
        recyclerCalendar.setLayoutManager(new LinearLayoutManager(this));
        eventsAdapter = new EventsAdapter(eventList, this, true);
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
        navDashboard.setOnClickListener(v -> openActivity(AdminDashboardActivity.class, "Opening Dashboard..."));
        navHome.setOnClickListener(v -> openActivity(AdminHomeActivity.class, "Opening Home..."));
        navUsers.setOnClickListener(v -> openActivity(AdminUsersActivity.class, "Opening User Manager..."));
        navEvents.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of your Event Manager", Toast.LENGTH_SHORT).show();
            }
        });
        navSettings.setOnClickListener(v -> openActivity(AdminSettingsActivity.class, "Opening Settings..."));
    }

    private void openActivity(Class<?> cls, String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, cls));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
<<<<<<< HEAD
        
=======
>>>>>>> 2c8201d26e015646d160557817c446570b615c65
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
<<<<<<< HEAD

=======
>>>>>>> 2c8201d26e015646d160557817c446570b615c65
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
<<<<<<< HEAD
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Boolean> attendanceMap = new HashMap<>();

                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String role = userSnap.child("role").getValue(String.class);
                            String email = userSnap.child("email").getValue(String.class);
                            String studentNumber = userSnap.child("studentNumber").getValue(String.class);

                            if (role == null || email == null) continue;
                            if ("Student".equals(role)) {
                                if (studentNumber != null) attendanceMap.put(studentNumber, false);
                            }

                            // send notification email
                            String subject = "New Event: " + title;
                            String body = "Hello " + role + ",\n\nA new event has been scheduled:\n\n" +
                                    "📅 Date: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dateInMillis) +
                                    "\n⏰ Time: " + time +
                                    "\n📍 Venue: " + venue +
                                    "\n\n" + description +
                                    "\n\n- EduNotify Admin";
                            EmailSender.sendEmail(email, subject, body);
                        }

                        Event newEvent = new Event(key, dateInMillis, title, description, time, venue, false, attendanceMap);
                        eventsRef.child(key).setValue(newEvent).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(AdminEventsActivity.this, "Event added & notifications sent!", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                loadEventsForDate(dateInMillis);
                            } else Toast.makeText(AdminEventsActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminEventsActivity.this, "Failed to fetch users", Toast.LENGTH_SHORT).show();
                    }
                });
=======
                usersRef.orderByChild("role").equalTo("Student")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Map<String, Boolean> attendanceMap = new HashMap<>();
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    String studentNumber = userSnap.child("studentNumber").getValue(String.class);
                                    if (studentNumber != null) attendanceMap.put(studentNumber, false);
                                }
                                Event newEvent = new Event(key, dateInMillis, title, description, time, venue, false, attendanceMap);
                                eventsRef.child(key).setValue(newEvent).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AdminEventsActivity.this, "Event added!", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadEventsForDate(dateInMillis);
                                    } else Toast.makeText(AdminEventsActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(AdminEventsActivity.this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                            }
                        });
>>>>>>> 2c8201d26e015646d160557817c446570b615c65
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
                eventsAdapter.updateList(filtered);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
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

    // ---------------------- Attendance Dialog ----------------------
    public void openAttendanceDialog(Event event) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_attendance);
        dialog.setCancelable(true);

        LinearLayout container = dialog.findViewById(R.id.attendanceContainer);
        Button btnSave = dialog.findViewById(R.id.btnSaveAttendance);

        if (container != null) container.removeAllViews();

        // Set dialog width to 60% of screen and height to wrap content but max 80% of screen
        Window window = dialog.getWindow();
        if (window != null) {
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = (int) (metrics.widthPixels * 0.6); // 60% width
            int maxHeight = (int) (metrics.heightPixels * 0.8); // 80% max height
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Limit ScrollView height dynamically
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

        // --- Existing attendance loading logic ---
        eventsRef.child(event.getId()).child("attendance")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Boolean> attendanceMap = new HashMap<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                attendanceMap.put(child.getKey(), child.getValue(Boolean.class));
                            }
                        }

                        if (attendanceMap.isEmpty()) {
                            TextView emptyText = new TextView(AdminEventsActivity.this);
                            emptyText.setText("There are no attendees in this event");
                            emptyText.setTextSize(16f);
                            emptyText.setPadding(8, 8, 8, 8);
                            container.addView(emptyText);
                            btnSave.setVisibility(View.GONE);
                        } else if (event.isClosed()) {
                            for (Map.Entry<String, Boolean> entry : attendanceMap.entrySet()) {
                                String studentNumber = entry.getKey();
                                Boolean present = entry.getValue();
                                TextView txtStatus = new TextView(AdminEventsActivity.this);
                                txtStatus.setText(studentNumber + ": " + (present != null && present ? "Present" : "Absent"));
                                txtStatus.setTextSize(16f);
                                txtStatus.setPadding(8, 8, 8, 8);
                                container.addView(txtStatus);
                            }
                            btnSave.setVisibility(View.GONE);
                        } else {
                            usersRef.orderByChild("role").equalTo("Student")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot usersSnap) {
                                            for (DataSnapshot userSnap : usersSnap.getChildren()) {
                                                String studentNumber = userSnap.child("studentNumber").getValue(String.class);
                                                String username = userSnap.child("username").getValue(String.class);
                                                if (studentNumber == null || username == null) continue;

                                                CheckBox checkBox = new CheckBox(AdminEventsActivity.this);
                                                checkBox.setText(username + " (" + studentNumber + ")");
                                                checkBox.setTag(studentNumber);
                                                checkBox.setChecked(attendanceMap.getOrDefault(studentNumber, false));
                                                container.addView(checkBox);
                                            }

                                            btnSave.setOnClickListener(v -> {
                                                Map<String, Boolean> updatedAttendance = new HashMap<>();
                                                for (int i = 0; i < container.getChildCount(); i++) {
                                                    View view = container.getChildAt(i);
                                                    if (view instanceof CheckBox) {
                                                        CheckBox cb = (CheckBox) view;
                                                        String studentNumber = (String) cb.getTag();
                                                        updatedAttendance.put(studentNumber, cb.isChecked());
                                                    }
                                                }
                                                eventsRef.child(event.getId()).child("attendance").setValue(updatedAttendance)
                                                        .addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(AdminEventsActivity.this, "Attendance updated!", Toast.LENGTH_SHORT).show();
                                                                dialog.dismiss();
                                                            } else {
                                                                Toast.makeText(AdminEventsActivity.this, "Failed to update attendance", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(AdminEventsActivity.this, "Failed to load students", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminEventsActivity.this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }


    // ---------------------- Interface Methods ----------------------
    @Override
    public void onEditEvent(Event event) {
        openEventDialog(event);
    }

    @Override
    public void onDeleteEvent(Event event) {
        eventsRef.child(event.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Event deleted!", Toast.LENGTH_SHORT).show();
                loadEventsForDate(selectedDate);
            } else {
                Toast.makeText(this, "Failed to delete event", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onViewAttendance(Event event) {
        openAttendanceDialog(event);
    }

    @Override
    public void onCloseEvent(Event event) {
        eventsRef.child(event.getId()).child("isClosed").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Event closed!", Toast.LENGTH_SHORT).show();
                loadEventsForDate(selectedDate);
            } else {
                Toast.makeText(this, "Failed to close event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
