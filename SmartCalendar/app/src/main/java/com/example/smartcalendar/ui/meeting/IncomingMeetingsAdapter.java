package com.example.smartcalendar.ui.meeting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.TravelTimeResponse;
import com.example.smartcalendar.data.network.NetworkClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingMeetingsAdapter extends RecyclerView.Adapter<IncomingMeetingsAdapter.ViewHolder> {

    private final List<Meeting> meetings;
    private final Map<Integer, TravelTimeResponse> travelInfoMap = new HashMap<>();
    private final Set<Integer> pendingRequests = new HashSet<>();
    private double userLat = 0, userLng = 0;
    private boolean locationSet = false;
    private String transportMode = "driving";
    
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public IncomingMeetingsAdapter(List<Meeting> meetings) {
        this.meetings = meetings;
    }

    public void setUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLng = lng;
        this.locationSet = true;
        notifyDataSetChanged();
    }

    public void setTransportMode(String mode) {
        if (!this.transportMode.equals(mode)) {
            this.transportMode = mode;
            this.travelInfoMap.clear();
            this.pendingRequests.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_incoming_meeting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        Context context = holder.itemView.getContext();
        
        holder.tvName.setText(meeting.getName());
        
        String address = (meeting.getLocation() != null) ? meeting.getLocation().getAddress() : "Online";
        boolean isOnline = "Online".equalsIgnoreCase(address) || (meeting.getApiLatitude() == null && meeting.getApiLongitude() == null);
        
        holder.tvLocation.setText(context.getString(R.string.location_label, address));

        try {
            Date startDate = apiFormat.parse(meeting.getStartTime());
            if (startDate != null) {
                holder.tvTime.setText(displayFormat.format(startDate));

                if (isOnline) {
                    holder.tvDeparture.setText("Online meeting");
                    holder.tvDistance.setText("-");
                    holder.statusIndicator.setBackgroundColor(Color.BLUE);
                } else {
                    if (locationSet && !travelInfoMap.containsKey(meeting.getId()) && !pendingRequests.contains(meeting.getId())) {
                        fetchTravelTime(meeting, holder.getAdapterPosition());
                    }

                    TravelTimeResponse travelInfo = travelInfoMap.get(meeting.getId());
                    if (travelInfo != null) {
                        long durationSeconds = travelInfo.getDurationSeconds();
                        Date departureDate = new Date(startDate.getTime() - (durationSeconds * 1000));
                        holder.tvDeparture.setText(context.getString(R.string.departure_time, displayFormat.format(departureDate)));
                        holder.tvDistance.setText(context.getString(R.string.distance, travelInfo.getDistanceText()));

                        // Color logic
                        long now = System.currentTimeMillis();
                        long diffToDeparture = departureDate.getTime() - now;

                        if (diffToDeparture < 0) {
                            holder.statusIndicator.setBackgroundColor(Color.GRAY);
                        } else if (diffToDeparture < 15 * 60 * 1000) {
                            holder.statusIndicator.setBackgroundColor(Color.RED);
                        } else if (diffToDeparture < 60 * 60 * 1000) {
                            holder.statusIndicator.setBackgroundColor(Color.YELLOW);
                        } else {
                            holder.statusIndicator.setBackgroundColor(Color.GREEN);
                        }
                    } else {
                        holder.tvDeparture.setText("Calculating...");
                        holder.tvDistance.setText("-");
                        holder.statusIndicator.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.btnOpenMaps.setOnClickListener(v -> {
            if (isOnline) {
                Toast.makeText(context, "Meeting is online", Toast.LENGTH_SHORT).show();
                return;
            }
            String uri;
            if (meeting.getApiLatitude() != null && meeting.getApiLongitude() != null) {
                uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)", 
                        meeting.getApiLatitude(), meeting.getApiLongitude(), 
                        meeting.getApiLatitude(), meeting.getApiLongitude(), meeting.getName());
            } else {
                uri = "geo:0,0?q=" + Uri.encode(address);
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            v.getContext().startActivity(intent);
        });
    }

    private void fetchTravelTime(Meeting meeting, int position) {
        if (!locationSet || meeting.getApiLatitude() == null || meeting.getApiLongitude() == null) return;
        
        pendingRequests.add(meeting.getId());
        NetworkClient.getApiService().getTravelTime(
                userLat, userLng,
                meeting.getApiLatitude(), meeting.getApiLongitude(),
                transportMode
        ).enqueue(new Callback<TravelTimeResponse>() {
            @Override
            public void onResponse(Call<TravelTimeResponse> call, Response<TravelTimeResponse> response) {
                pendingRequests.remove(meeting.getId());
                if (response.isSuccessful() && response.body() != null) {
                    travelInfoMap.put(meeting.getId(), response.body());
                    notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(Call<TravelTimeResponse> call, Throwable t) {
                pendingRequests.remove(meeting.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvLocation, tvDeparture, tvDistance;
        View statusIndicator;
        Button btnOpenMaps;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_meeting_name);
            tvTime = itemView.findViewById(R.id.tv_meeting_time);
            tvLocation = itemView.findViewById(R.id.tv_location_name);
            tvDeparture = itemView.findViewById(R.id.tv_departure_time);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
            btnOpenMaps = itemView.findViewById(R.id.btn_open_maps);
        }
    }
}
