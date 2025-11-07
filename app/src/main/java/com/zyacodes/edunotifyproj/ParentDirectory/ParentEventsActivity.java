package com.zyacodes.edunotifyproj.ParentDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.zyacodes.edunotifyproj.Adapters.EventsAdapter;
import com.zyacodes.edunotifyproj.Interfaces.EventActionListener;
import com.zyacodes.edunotifyproj.Models.Event;
import com.zyacodes.edunotifyproj.R;
import com.zyacodes.edunotifyproj.ParentDirectory.ParentDashboardActivity;
import com.zyacodes.edunotifyproj.ParentDirectory.ParentHomeActivity;
import com.zyacodes.edunotifyproj.ParentDirectory.ParentSettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ParentEventsActivity extends AppCompatActivity implements EventActionListener {

    private CalendarView calendarView;
    private RecyclerView recyclerCalendar;
    private ScrollView mainScrollView;
    private LinearLayout navDashboard, navHome, navEvents, navSettings;

    private DatabaseReference eventsRef;
    private List<Event> eventList = new ArrayList<>();
    private EventsAdapter eventsAdapter;

    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_events);

        initViews();
        setupFirebase();
        setupRecyclerView();
        setupCalendar();
        setupNavigation();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        recyclerCalendar = findViewById(R.id.recyclerCalendar);
        mainScrollView = findViewById(R.id.mainScrollView);

        navDashboard = findViewById(R.id.navDashboard);
        navHome = findViewById(R.id.navHome);
        navEvents = findViewById(R.id.navEvents);
        navSettings = findViewById(R.id.navSettings);
    }

    private void setupFirebase() {
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");
        eventsRef = db.getReference("events");
    }

    private void setupRecyclerView() {
        recyclerCalendar.setLayoutManager(new LinearLayoutManager(this));
        // Set editable = false (so no admin actions show)
        eventsAdapter = new EventsAdapter(eventList, this, false);
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

    private void setupNavigation() {
        navDashboard.setOnClickListener(v -> openActivity(ParentDashboardActivity.class, "Opening Dashboard..."));
        navHome.setOnClickListener(v -> openActivity(ParentHomeActivity.class, "Opening Home..."));
        navEvents.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
                Toast.makeText(this, "Back to top of Events", Toast.LENGTH_SHORT).show();
            }
        });
        navSettings.setOnClickListener(v -> openActivity(ParentSettingsActivity.class, "Opening Settings..."));
    }

    private void openActivity(Class<?> cls, String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, cls));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void loadEventsForDate(long dateInMillis) {
        // Get current parent's student number
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        DatabaseReference usersRef = FirebaseDatabase.getInstance(
                        "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String studentNumber = snapshot.child("student").getValue(String.class);
                if (studentNumber == null) return;

                eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Event> filtered = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Event event = child.getValue(Event.class);
                            if (event != null && isSameDay(event.getDateInMillis(), dateInMillis)) {

                                // If event is closed, check attendance for the student's number
                                if (event.isClosed() && event.getAttendance() != null) {
                                    // Get the nested map
                                    Map<String, Boolean> attendanceMap = event.getAttendance().get("allStudents");

                                    if (attendanceMap != null) {
                                        Boolean isPresent = attendanceMap.get(studentNumber);
                                        if (isPresent == null) {
                                            event.setStatus("Not Recorded");
                                        } else if (isPresent) {
                                            event.setStatus("Present");
                                        } else {
                                            event.setStatus("Absent");
                                        }
                                    } else {
                                        event.setStatus("Not Recorded");
                                    }
                                } else if (event.isClosed()) {
                                    event.setStatus("Not Recorded");
                                } else {
                                    event.setStatus("Open");
                                }


                                filtered.add(event);
                            }
                        }
                        eventsAdapter.updateList(filtered);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ParentEventsActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private boolean isSameDay(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        cal2.setTimeInMillis(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private long getDateInMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    // ---------------------- Interface Methods ----------------------
    @Override
    public void onEditEvent(Event event) {
        // Disabled for parents
        Toast.makeText(this, "Only administrators can edit events", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteEvent(Event event) {
        // Disabled for parents
        Toast.makeText(this, "Only administrators can delete events", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewAttendance(Event event) {
        // Optional: Parents can only view their own attendance
        Toast.makeText(this, "Attendance can be viewed after event completion", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCloseEvent(Event event) {
        // Disabled for parents
        Toast.makeText(this, "Only administrators can close events", Toast.LENGTH_SHORT).show();
    }
}
