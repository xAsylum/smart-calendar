package com.example.smartcalendar.ui.friends;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.friendrequest.FriendRequestListResponse;
import com.example.smartcalendar.data.network.NetworkClient;
import com.example.smartcalendar.data.repository.FriendRepository;
import com.example.smartcalendar.databinding.FragmentFriendsBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsFragment extends Fragment {

    private FriendRepository friendRepo;
    private FriendRequestAdapter receivedAdapter;
    private FriendRequestAdapter sentAdapter;
    private FragmentFriendsBinding binding; // Zakładam użycie ViewBinding

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        friendRepo = FriendRepository.getInstance();
        setupRecyclerViews();

        binding.btnSendInvite.setOnClickListener(v -> {
            String username = binding.etSearchUsername.getText().toString();
            if (!username.isEmpty()) {
                friendRepo.sendRequest(getContext(), username, this::refreshData);
                binding.etSearchUsername.setText("");
            }
        });

        refreshData();
    }

    private void setupRecyclerViews() {
        receivedAdapter = new FriendRequestAdapter(true, new FriendRequestAdapter.OnRequestActionListener() {
            @Override public void onAccept(int id) { friendRepo.acceptRequest(getContext(), id, () -> refreshData()); }
            @Override public void onReject(int id) { friendRepo.rejectRequest(getContext(), id, () -> refreshData()); }
            @Override public void onCancel(int id) {}
        });

        sentAdapter = new FriendRequestAdapter(false, new FriendRequestAdapter.OnRequestActionListener() {
            @Override public void onAccept(int id) {}
            @Override public void onReject(int id) {}
            @Override public void onCancel(int id) { friendRepo.cancelRequest(getContext(), id, () -> refreshData()); }
        });

        binding.rvReceivedRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReceivedRequests.setAdapter(receivedAdapter);

        binding.rvSentRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSentRequests.setAdapter(sentAdapter);
    }

    private void refreshData() {
        String token = "Bearer " + TokenManager.getInstance().getToken(getContext());

        NetworkClient.getApiService().getReceivedRequests(token).enqueue(new Callback<FriendRequestListResponse>() {
            @Override
            public void onResponse(Call<FriendRequestListResponse> call, Response<FriendRequestListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    receivedAdapter.setItems(response.body().getRequests());
                }
            }
            @Override public void onFailure(Call<FriendRequestListResponse> call, Throwable t) {}
        });

        NetworkClient.getApiService().getSentRequests(token).enqueue(new Callback<FriendRequestListResponse>() {
            @Override
            public void onResponse(Call<FriendRequestListResponse> call, Response<FriendRequestListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sentAdapter.setItems(response.body().getRequests());
                }
            }
            @Override public void onFailure(Call<FriendRequestListResponse> call, Throwable t) {}
        });
    }
}