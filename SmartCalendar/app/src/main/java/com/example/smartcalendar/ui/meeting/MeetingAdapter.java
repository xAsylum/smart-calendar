package com.example.smartcalendar.ui.meeting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.meeting.Meeting;
import java.util.List;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    private List<Meeting> meetingList;
    private int currentUserId;

    public interface OnMeetingClickListener {
        void onMeetingClick(Meeting meeting);
    }

    public interface OnMeetingDeleteListener {
        void onMeetingDelete(Meeting meeting);
    }

    private OnMeetingClickListener listener;
    private OnMeetingDeleteListener deleteListener;

    public MeetingAdapter(List<Meeting> meetings, int currentUserId, OnMeetingClickListener listener, OnMeetingDeleteListener deleteListener) {
        this.meetingList = meetings;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.deleteListener = deleteListener;
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

        if (meeting.getOwner() == currentUserId) {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onMeetingDelete(meeting);
                }
            });
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return meetingList != null ? meetingList.size() : 0;
    }

    public static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvLocation;
        ImageButton btnDelete;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_meeting_title);
            tvTime = itemView.findViewById(R.id.tv_meeting_time);
            tvLocation = itemView.findViewById(R.id.tv_meeting_location);
            btnDelete = itemView.findViewById(R.id.btn_delete_meeting);
        }
    }
}