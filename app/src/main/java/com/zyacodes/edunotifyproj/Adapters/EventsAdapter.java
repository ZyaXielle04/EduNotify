package com.zyacodes.edunotifyproj.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zyacodes.edunotifyproj.AdminDirectory.AdminEventsActivity;
import com.zyacodes.edunotifyproj.Models.Event;
import com.zyacodes.edunotifyproj.R;
import com.zyacodes.edunotifyproj.TeacherDirectory.TeacherEventsActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<Event> eventList;
    private final Context context;
    private final boolean isAdmin;
    private final DatabaseReference eventsRef;
    private final DatabaseReference usersRef;

    private String currentUserRole = "";
    private String currentStudentNumber = "";

    public EventsAdapter(List<Event> eventList, Context context, boolean isAdmin) {
        this.eventList = eventList;
        this.context = context;
        this.isAdmin = isAdmin;

        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/");

        eventsRef = db.getReference("events");
        usersRef = db.getReference("users");

        fetchCurrentUserInfo();
    }

    private void fetchCurrentUserInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null) {
            usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUserRole = snapshot.child("role").getValue(String.class);
                    currentStudentNumber = snapshot.child("studentNumber").getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        holder.txtTitle.setText(event.getTitle());
        holder.txtDescription.setText(event.getDescription());
        holder.txtVenue.setText("Venue: " + event.getVenue());
        holder.txtDate.setText("Date: " + sdf.format(event.getDateInMillis()));
        holder.txtTime.setText("Time: " + event.getTime());

        // Hide all buttons by default
        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);
        holder.btnAttendance.setVisibility(View.GONE);
        holder.btnCloseEvent.setVisibility(View.GONE);

        // ----- ADMIN / TEACHER -----
        if (currentUserRole.equalsIgnoreCase("Admin") || currentUserRole.equalsIgnoreCase("Teacher")) {
            // Set status
            holder.txtStatus.setText(event.isClosed() ? "Status: Closed" : "Status: Open");
            holder.txtStatus.setTextColor(0xFF757575); // gray

            // Buttons logic
            if (event.isClosed()) {
                holder.btnAttendance.setVisibility(View.VISIBLE);
            } else {
                if (isAdmin) {
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.btnDelete.setVisibility(View.VISIBLE);
                    holder.btnAttendance.setVisibility(View.VISIBLE);
                    holder.btnCloseEvent.setVisibility(View.VISIBLE);
                } else { // Teacher
                    holder.btnEdit.setVisibility(View.VISIBLE);
                    holder.btnAttendance.setVisibility(View.VISIBLE);
                }
            }

            // Button listeners
            holder.btnEdit.setOnClickListener(v -> {
                if (context instanceof AdminEventsActivity) {
                    ((AdminEventsActivity) context).openEventDialog(event);
                } else if (context instanceof TeacherEventsActivity) {
                    ((TeacherEventsActivity) context).openEventDialog(event);
                }
            });

            holder.btnAttendance.setOnClickListener(v -> {
                if (context instanceof AdminEventsActivity) {
                    ((AdminEventsActivity) context).openAttendanceDialog(event);
                } else if (context instanceof TeacherEventsActivity) {
                    ((TeacherEventsActivity) context).openAttendanceDialog(event);
                }
            });

            holder.btnDelete.setOnClickListener(v -> confirmDelete(event));
            holder.btnCloseEvent.setOnClickListener(v -> confirmClose(event, holder));

        }
        // ----- STUDENT / PARENT -----
        else if (currentUserRole.equalsIgnoreCase("Student") || currentUserRole.equalsIgnoreCase("Parent")) {
            if (currentUserRole.equalsIgnoreCase("Parent")) {
                // Fetch child's studentNumber
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                usersRef.child(uid).child("student").get().addOnSuccessListener(snapshot -> {
                    currentStudentNumber = snapshot.getValue(String.class);
                    fetchAttendance(holder, event);
                }).addOnFailureListener(e -> {
                    holder.txtStatus.setText("Attendance: Error");
                    holder.txtStatus.setTextColor(0xFFD32F2F);
                });
            } else {
                // Student directly uses their studentNumber
                fetchAttendance(holder, event);
            }
        }
    }

    private void fetchAttendance(EventViewHolder holder, Event event) {
        if (currentStudentNumber == null || currentStudentNumber.isEmpty()) {
            holder.txtStatus.setText("Attendance: Not recorded");
            holder.txtStatus.setTextColor(0xFF757575);
            return;
        }

        if (!event.isClosed()) {
            holder.txtStatus.setText("Status: Open");
            holder.txtStatus.setTextColor(0xFF757575);
            return;
        }

        holder.txtStatus.setText("Attendance: Loading...");
        eventsRef.child(event.getId())
                .child("attendance")
                .child(currentStudentNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Boolean isPresent = snapshot.getValue(Boolean.class);
                            if (Boolean.TRUE.equals(isPresent)) {
                                holder.txtStatus.setText("Attendance: Present");
                                holder.txtStatus.setTextColor(0xFF2E7D32); // green
                            } else {
                                holder.txtStatus.setText("Attendance: Absent");
                                holder.txtStatus.setTextColor(0xFFD32F2F); // red
                            }
                        } else {
                            holder.txtStatus.setText("Attendance: Not recorded");
                            holder.txtStatus.setTextColor(0xFF757575);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        holder.txtStatus.setText("Attendance: Error");
                        holder.txtStatus.setTextColor(0xFFD32F2F);
                    }
                });
    }

    private void confirmDelete(Event event) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    eventsRef.child(event.getId()).removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                                    eventList.remove(event);
                                    notifyDataSetChanged();
                                } else {
                                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmClose(Event event, EventViewHolder holder) {
        new AlertDialog.Builder(context)
                .setTitle("Close Event Attendance")
                .setMessage("Are you sure you want to close attendance for this event?")
                .setPositiveButton("Close", (dialog, which) -> {
                    eventsRef.child(event.getId()).child("isClosed").setValue(true)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Event attendance closed", Toast.LENGTH_SHORT).show();
                                    event.setIsClosed(true);
                                    holder.txtStatus.setText("Status: Closed");
                                    holder.btnCloseEvent.setVisibility(View.GONE);
                                } else {
                                    Toast.makeText(context, "Failed to close event", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtVenue, txtDate, txtTime, txtStatus;
        Button btnEdit, btnDelete, btnAttendance, btnCloseEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtVenue = itemView.findViewById(R.id.txtVenue);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAttendance = itemView.findViewById(R.id.btnAttendance);
            btnCloseEvent = itemView.findViewById(R.id.btnCloseEvent);
        }
    }
}
