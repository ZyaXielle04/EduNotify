package com.zyacodes.edunotifyproj.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zyacodes.edunotifyproj.Models.Event;
import com.zyacodes.edunotifyproj.R;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private final List<Event> eventList;
    private final OnGeneratePdfClickListener listener;

    public interface OnGeneratePdfClickListener {
        void onGeneratePdfClick(Event event);
    }

    public ReportsAdapter(List<Event> eventList, OnGeneratePdfClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_report_card, parent, false);
        return new ReportsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportsAdapter.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText("Date: " + event.getFormattedDate());
        holder.time.setText("Time: " + event.getTime());
        holder.venue.setText("Venue: " + event.getVenue());
        holder.status.setText("Status: " + (event.isClosed() ? "Closed" : "Open"));

        holder.btnGenerate.setOnClickListener(v -> listener.onGeneratePdfClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, time, venue, status;
        Button btnGenerate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.eventDate);
            time = itemView.findViewById(R.id.eventTime);
            venue = itemView.findViewById(R.id.eventVenue);
            status = itemView.findViewById(R.id.eventStatus);
            btnGenerate = itemView.findViewById(R.id.btnGeneratePdf);
        }
    }
}
