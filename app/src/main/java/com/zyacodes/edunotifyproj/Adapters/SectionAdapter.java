package com.zyacodes.edunotifyproj.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zyacodes.edunotifyproj.Models.Section;
import com.zyacodes.edunotifyproj.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private List<Section> sectionList;
    private DatabaseReference sectionsRef;

    public SectionAdapter(List<Section> sectionList) {
        this.sectionList = sectionList;
        this.sectionsRef = FirebaseDatabase.getInstance("https://edunotifyproj-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("sections");
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        Section section = sectionList.get(position);

        holder.tvCollege.setText(section.getCollege());
        holder.tvSectionName.setText(section.getProgram() + " | " + section.getYearLevel()+section.getSection());
        holder.tvCode.setText("Code: " + section.getCode());

        holder.btnRegenerateCode.setOnClickListener(v -> {
            String oldCode = section.getCode();
            String newCode = generateRandomCode();

            // Copy old data under new code
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("college", section.getCollege());
            updatedData.put("yearLevel", section.getYearLevel());
            updatedData.put("program", section.getProgram());
            updatedData.put("section", section.getSection());
            updatedData.put("code", newCode);

            // Write to Firebase under new code
            sectionsRef.child(newCode).setValue(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        // Remove old node
                        sectionsRef.child(oldCode).removeValue();
                        section.setCode(newCode);
                        notifyItemChanged(position);

                        Toast.makeText(v.getContext(), "New Code: " + newCode, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollege, tvSectionName, tvCode;
        Button btnRegenerateCode;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollege = itemView.findViewById(R.id.tvCollege);
            tvSectionName = itemView.findViewById(R.id.tvSectionName);
            tvCode = itemView.findViewById(R.id.tvCode);
            btnRegenerateCode = itemView.findViewById(R.id.btnRegenerateCode);
        }
    }

    // Simple random code generator
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}
