package com.zyacodes.edunotifyproj.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zyacodes.edunotifyproj.Models.UserModel;
import com.zyacodes.edunotifyproj.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<UserModel> userList;
    private final DatabaseReference usersRef = FirebaseDatabase.getInstance(
                    "https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users");

    public UserAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);

        holder.textName.setText(user.getUsername());
        holder.textEmail.setText(user.getEmail());
        holder.textRole.setText("Role: " + user.getRole());

        // Show student number only for Student/Parent roles
        if (user.getRole() != null && (user.getRole().equals("Student") || user.getRole().equals("Parent"))) {
            holder.textStudentNumber.setVisibility(View.VISIBLE);
            holder.textStudentNumber.setText("Student Number: " + user.getStudentNumber());
        } else {
            holder.textStudentNumber.setVisibility(View.GONE);
        }

        // Reset visibility and click listener (important for RecyclerView reuse)
        holder.buttonApprove.setVisibility(View.GONE);
        holder.textVerified.setVisibility(View.GONE);
        holder.buttonApprove.setOnClickListener(null);

        // Safe boolean check
        boolean approved = user.isApproved(); // force correct boolean

        if (approved) {
            // Already approved → show verified text
            holder.textVerified.setVisibility(View.VISIBLE);
            holder.buttonApprove.setVisibility(View.GONE);
        } else {
            // Not approved → show approve button
            holder.buttonApprove.setVisibility(View.VISIBLE);
            holder.textVerified.setVisibility(View.GONE);

            holder.buttonApprove.setOnClickListener(v -> {
                usersRef.child(user.getUid()).child("isApproved").setValue(true)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, user.getUsername() + " approved!", Toast.LENGTH_SHORT).show();
                            user.setApproved(true); // Update local object
                            holder.buttonApprove.setVisibility(View.GONE);
                            holder.textVerified.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Failed to approve: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textName, textEmail, textRole, textStudentNumber, textVerified;
        Button buttonApprove;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textRole = itemView.findViewById(R.id.textRole);
            textStudentNumber = itemView.findViewById(R.id.textStudentNumber);
            buttonApprove = itemView.findViewById(R.id.buttonApprove);
            textVerified = itemView.findViewById(R.id.textVerified);
        }
    }
}
