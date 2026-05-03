package com.example.smartcalendar.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.friendrequest.FriendRequest;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<FriendRequest> items = new ArrayList<>();
    private final boolean isReceived;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAccept(int userId);
        void onReject(int userId);
        void onCancel(int userId);
    }

    public FriendRequestAdapter(boolean isReceived, OnRequestActionListener listener) {
        this.isReceived = isReceived;
        this.listener = listener;
    }

    public void setItems(List<FriendRequest> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest item = items.get(position);
        holder.username.setText(item.getUsername());

        if (isReceived) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);

            holder.btnAccept.setOnClickListener(v -> listener.onAccept(item.getId()));
            holder.btnReject.setOnClickListener(v -> listener.onReject(item.getId()));
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);

            holder.btnCancel.setOnClickListener(v -> listener.onCancel(item.getId()));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        Button btnAccept, btnReject, btnCancel;

        ViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvRequestUsername);
            btnAccept = itemView.findViewById(R.id.btnAcceptRequest);
            btnReject = itemView.findViewById(R.id.btnRejectRequest);
            btnCancel = itemView.findViewById(R.id.btnCancelRequest);
        }
    }
}