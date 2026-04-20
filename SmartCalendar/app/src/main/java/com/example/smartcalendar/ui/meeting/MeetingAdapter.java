package com.example.smartcalendar.ui.meeting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.Meeting;
import java.util.List;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    private List<Meeting> meetingList;
    public interface OnMeetingClickListener {
        void onMeetingClick(Meeting meeting);
    }

    private OnMeetingClickListener listener;

    public MeetingAdapter(List<Meeting> meetings, OnMeetingClickListener listener) {
        this.meetingList = meetings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);

        holder.tvTitle.setText(meeting.getName());
        holder.tvLocation.setText(meeting.getLocation() != null ? meeting.getLocation().getAddress() : "Brak lokalizacji");

        holder.itemView.setOnClickListener(v -> listener.onMeetingClick(meeting));
        if (meeting.getStartTime() != null && meeting.getStartTime().length() > 16) {
            String time = meeting.getStartTime().substring(11, 16);
            holder.tvTime.setText(time);
        } else {
            holder.tvTime.setText("--:--");
        }
    }

    @Override
    public int getItemCount() {
        return meetingList != null ? meetingList.size() : 0;
    }

    public static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvLocation;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_meeting_title);
            tvTime = itemView.findViewById(R.id.tv_meeting_time);
            tvLocation = itemView.findViewById(R.id.tv_meeting_location);
        }
    }
}