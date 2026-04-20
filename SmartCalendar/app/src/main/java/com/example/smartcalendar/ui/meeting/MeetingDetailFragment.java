package com.example.smartcalendar.ui.meeting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.Meeting;
import com.example.smartcalendar.databinding.FragmentMeetingDetailBinding;
import com.example.smartcalendar.ui.meeting.MeetingViewModel;

public class MeetingDetailFragment extends Fragment {

    private FragmentMeetingDetailBinding binding;
    private MeetingViewModel viewModel;
    private ChatAdapter chatAdapter;
    private int meetingId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMeetingDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);

        if (getArguments() != null) {
            meetingId = getArguments().getInt("meeting_id");
        }

        Meeting meeting = viewModel.getMeeting(meetingId);

        if (meeting != null) {
            binding.tvDetailTitle.setText(meeting.getName());

            String dateTimeInfo = meeting.getStartTime().replace("T", " ");
            String duration = meeting.getDuration();
            binding.tvDetailDateTime.setText(dateTimeInfo);
            binding.tvDetailDuration.setText(duration);
            if (meeting.getLocation() != null && meeting.getLocation().getAddress() != null) {
                binding.tvDetailLocation.setText(meeting.getLocation().getAddress());
            } else {
                binding.tvDetailLocation.setText("Brak lokalizacji");
            }

            setupChat(meeting);
        }

        binding.btnSend.setOnClickListener(v -> {
            String msgText = binding.etChatMessage.getText().toString().trim();
            if (!msgText.isEmpty()) {
                //viewModel.addChatMessage(meetingId, "Me: " + msgText);

                chatAdapter.notifyItemInserted(meeting.getChatMessages().size() - 1);
                binding.rvChat.scrollToPosition(meeting.getChatMessages().size() - 1);

                binding.etChatMessage.setText("");
            }
        });

        binding.btnManage.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("meeting_id", meetingId);
            Navigation.findNavController(view).navigate(R.id.action_MeetingDetailFragment_to_MeetingManagementFragment, bundle);
        });
    }

    private void setupChat(Meeting meeting) {
        chatAdapter = new ChatAdapter(meeting.getChatMessages());
        binding.rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChat.setAdapter(chatAdapter);

        if (!meeting.getChatMessages().isEmpty()) {
            binding.rvChat.scrollToPosition(meeting.getChatMessages().size() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}