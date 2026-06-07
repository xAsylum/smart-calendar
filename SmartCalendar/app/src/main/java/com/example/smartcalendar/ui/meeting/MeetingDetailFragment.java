package com.example.smartcalendar.ui.meeting;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.chat.ChatMessage;
import com.example.smartcalendar.data.network.ChatWebSocketManager;
import com.example.smartcalendar.data.network.NetworkClient;
import com.example.smartcalendar.databinding.FragmentMeetingDetailBinding;

import java.util.ArrayList;
import java.util.List;

public class MeetingDetailFragment extends Fragment implements ChatWebSocketManager.ChatCallback {

    private FragmentMeetingDetailBinding binding;
    private MeetingViewModel viewModel;
    private ChatAdapter chatAdapter;
    private int meetingId;
    private ChatWebSocketManager chatWebSocketManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeetingDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);

        if (getArguments() != null) {
            meetingId = getArguments().getInt("meeting_id");
        }

        chatAdapter = new ChatAdapter(new ArrayList<>());
        binding.rvChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChat.setAdapter(chatAdapter);

        viewModel.getMeetingLive(requireContext(), meetingId).observe(getViewLifecycleOwner(), meeting -> {
            if (meeting != null) {
                binding.tvDetailTitle.setText(meeting.getName());

                if (meeting.getStartTime() != null) {
                    String dateTimeInfo = meeting.getStartTime().replace("T", " ");
                    binding.tvDetailDateTime.setText(dateTimeInfo);
                }

                binding.tvDetailDuration.setText(meeting.getDuration());

                if (meeting.getLocation() != null && meeting.getLocation().getAddress() != null) {
                    binding.tvDetailLocation.setText(meeting.getLocation().getAddress());
                } else {
                    binding.tvDetailLocation.setText("Brak lokalizacji");
                }
            }
        });

        setupWebSocket();

        binding.btnSend.setOnClickListener(v -> {
            String msgText = binding.etChatMessage.getText().toString().trim();
            if (!msgText.isEmpty()) {
                if (chatWebSocketManager != null) {
                    chatWebSocketManager.sendMessage(msgText);
                }
                binding.etChatMessage.setText("");
            }
        });

        binding.btnManage.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("meeting_id", meetingId);
            Navigation.findNavController(view).navigate(
                    R.id.action_MeetingDetailFragment_to_MeetingManagementFragment, bundle);
        });
    }

    private void setupWebSocket() {
        String token = TokenManager.getInstance().getToken(requireContext());
        if (token == null) return;

        String wsUrl = NetworkClient.getWsUrl(meetingId, token);

        chatWebSocketManager = new ChatWebSocketManager(this);
        chatWebSocketManager.connect(wsUrl);
    }

    @Override
    public void onHistoryReceived(List<ChatMessage> messages) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    binding.rvChat.scrollToPosition(messages.size() - 1);
                }
            });
        }
    }

    @Override
    public void onNewMessageReceived(ChatMessage message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                chatAdapter.addMessage(message);
                binding.rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
            });
        }
    }

    @Override
    public void onError(String error) {
        Log.e("MeetingDetailFragment", "WebSocket Error: " + error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatWebSocketManager != null) {
            chatWebSocketManager.disconnect();
        }
        binding = null;
    }
}
