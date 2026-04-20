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

import com.example.smartcalendar.data.models.Meeting;
import com.example.smartcalendar.data.models.MeetingMember;
import com.example.smartcalendar.data.models.User;
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
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMeetingManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);

        meetingId = getArguments().getInt("meeting_id");

        Meeting meeting = viewModel.getMeeting(meetingId);

        loadMeetingData(meeting);


        binding.btnSaveChanges.setOnClickListener(v -> {
            viewModel.updateMeeting(
                    meetingId,
                    binding.etEditName.getText().toString(),
                    binding.etEditAddress.getText().toString()
            );
            Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        });

        binding.btnChangeTime.setOnClickListener(v -> showTimePicker());

        binding.btnChangeDuration.setOnClickListener(v -> showDurationPicker());

        binding.btnAddMember.setOnClickListener(v -> showAddMemberDialog());
    }

    private void showAddMemberDialog() {
        List<User> allFriends = viewModel.getAvailableFriends();
        Meeting meeting = viewModel.getMeeting(meetingId);
        List<MeetingMember> currentMembers = meeting.getMembers();

        String[] friendNames = new String[allFriends.size()];
        boolean[] checkedItems = new boolean[allFriends.size()];

        for (int i = 0; i < allFriends.size(); i++) {
            User friend = allFriends.get(i);
            friendNames[i] = friend.getUsername();

            for (MeetingMember m : currentMembers) {
                if (m.getUserId() == friend.getId()) {
                    checkedItems[i] = true;
                    break;
                }
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Meeting members")
                .setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("Update", (dialog, which) -> {
                    List<User> selectedUsers = new ArrayList<>();
                    for (int i = 0; i < allFriends.size(); i++) {
                        if (checkedItems[i]) {
                            selectedUsers.add(allFriends.get(i));
                        }
                    }

                    updateMeetingMembers(selectedUsers);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateMeetingMembers(List<User> newSelectedUsers) {
        viewModel.setMeetingMembers(meetingId, newSelectedUsers);

        binding.cgMembers.removeAllViews();
        for (User user : newSelectedUsers) {
            addMemberChip(new MeetingMember(user.getId(), user.getUsername()));
        }
    }

    private void loadMeetingData(Meeting meeting) {
        binding.etEditName.setText(meeting.getName());
        if (meeting.getLocation() != null) {
            binding.etEditAddress.setText(meeting.getLocation().getAddress());
        }

        for (MeetingMember member : meeting.getMembers()) {
            addMemberChip(member);
        }
    }

    private void addMemberChip(MeetingMember member) {
        Chip chip = new Chip(getContext());
        chip.setText(member.getUsername());
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            binding.cgMembers.removeView(chip);
        });
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
            String newTime = String.format("%02d:%02d", picker.getHour(), picker.getMinute());
            binding.btnChangeTime.setText("Time: " + newTime);
            // viewModel.updateStartTime(meetingId, ...)
        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void showDurationPicker() {
        String[] durations = {"15 min", "30 min", "1 h", "2 h", "5 h"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select meeting duration")
                .setItems(durations, (dialog, which) -> {
                    binding.btnChangeDuration.setText("Duration: " + durations[which]);
                    // viewModel.updateDuration(meetingId, durations[which]);
                })
                .show();
    }
}