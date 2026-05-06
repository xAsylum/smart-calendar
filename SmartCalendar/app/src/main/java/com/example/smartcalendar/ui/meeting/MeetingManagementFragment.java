package com.example.smartcalendar.ui.meeting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.smartcalendar.data.models.friend.Friend;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingMember;
import com.example.smartcalendar.data.repository.FriendRepository;
import com.example.smartcalendar.databinding.FragmentMeetingManagementBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;

public class MeetingManagementFragment extends Fragment {

    private FragmentMeetingManagementBinding binding;
    private MeetingViewModel viewModel;
    private int meetingId;

    private Meeting currentMeeting;
    private List<MeetingMember> currentMembers = new ArrayList<>();
    private boolean isInitialLoad = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMeetingManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);
        meetingId = getArguments().getInt("meeting_id");

        // Obserwowanie podstawowych danych spotkania
        viewModel.getMeetingLive(requireContext(), meetingId).observe(getViewLifecycleOwner(), meeting -> {
            if (meeting != null) {
                currentMeeting = meeting;
                updateBasicUI(meeting);
            }
        });

        // Obserwowanie członków ładowanych oddzielnie z API
        viewModel.getMeetingMembersLive().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                currentMembers = members;
                updateMembersUI(members);
            }
        });

        // Inicjalne ładowanie członków
        viewModel.loadMeetingMembers(requireContext(), meetingId);

        binding.btnSaveChanges.setOnClickListener(v -> {
            viewModel.updateMeeting(
                    requireContext(),
                    meetingId,
                    binding.etEditName.getText().toString(),
                    binding.etEditAddress.getText().toString()
            );
            Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        });

        binding.btnChangeTime.setOnClickListener(v -> {
            if (currentMeeting != null) showTimePicker();
        });

        binding.btnChangeDuration.setOnClickListener(v -> {
            if (currentMeeting != null) showDurationPicker();
        });

        binding.btnAddMember.setOnClickListener(v -> {
            if (currentMeeting != null) showAddMemberDialog();
        });
    }

    private void updateBasicUI(Meeting meeting) {
        if (isInitialLoad) {
            binding.etEditName.setText(meeting.getName());
            if (meeting.getLocation() != null) {
                binding.etEditAddress.setText(meeting.getLocation().getAddress());
            }
            isInitialLoad = false;
        }

        if (meeting.getStartTime() != null) {
            String timePart = meeting.getStartTime().contains("T")
                    ? meeting.getStartTime().split("T")[1].substring(0, 5)
                    : meeting.getStartTime();
            binding.btnChangeTime.setText("Time: " + timePart);
        }

        if (meeting.getDuration() != null) {
            binding.btnChangeDuration.setText("Duration: " + meeting.getDuration());
        }
    }

    private void updateMembersUI(List<MeetingMember> members) {
        binding.cgMembers.removeAllViews();
        for (MeetingMember member : members) {
            addMemberChip(member);
        }
    }

    private void showAddMemberDialog() {
        viewModel.getAvailableFriends(requireContext(), new FriendRepository.FriendsLoadListener() {
            @Override
            public void onSuccess(List<Friend> allFriends) {
                if (!isAdded()) return;

                String[] friendNames = new String[allFriends.size()];
                boolean[] checkedItems = new boolean[allFriends.size()];

                for (int i = 0; i < allFriends.size(); i++) {
                    Friend friend = allFriends.get(i);
                    friendNames[i] = friend.getUsername();

                    for (MeetingMember m : currentMembers) {
                        if (m.getUserId() == friend.getId()) {
                            checkedItems[i] = true;
                            break;
                        }
                    }
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Zarządzaj uczestnikami")
                        .setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> {
                            checkedItems[which] = isChecked;
                        })
                        .setPositiveButton("Aktualizuj", (dialog, which) -> {
                            List<Friend> selectedUsers = new ArrayList<>();
                            for (int i = 0; i < allFriends.size(); i++) {
                                if (checkedItems[i]) {
                                    selectedUsers.add(allFriends.get(i));
                                }
                            }
                            viewModel.setMeetingMembers(requireContext(), meetingId, selectedUsers);
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Nie udało się pobrać znajomych: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addMemberChip(MeetingMember member) {
        Chip chip = new Chip(requireContext());
        chip.setText(member.getUsername());
        chip.setCloseIconVisible(false);
        binding.cgMembers.addView(chip);
    }

    private void showTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select starting time.")
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            viewModel.updateStartTime(requireContext(), meetingId, picker.getHour(), picker.getMinute());
        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void showDurationPicker() {
        String[] durations = {"00:15:00", "00:30:00", "01:00:00", "02:00:00", "05:00:00"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select meeting duration")
                .setItems(durations, (dialog, which) -> {
                    viewModel.updateDuration(requireContext(), meetingId, durations[which]);
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
