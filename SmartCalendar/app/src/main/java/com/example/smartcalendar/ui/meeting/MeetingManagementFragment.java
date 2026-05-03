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

import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingMember;
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

    // Trzymamy aktualne spotkanie w pamięci, aby przekazywać je do dialogów
    private Meeting currentMeeting;
    // Zabezpieczenie przed nadpisywaniem EditTextów podczas pisania
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

        // OBSERWATOR - Ładuje dane w tle, eliminuje IllegalStateException
        viewModel.getMeetingLive(requireContext(), meetingId).observe(getViewLifecycleOwner(), meeting -> {
            if (meeting != null) {
                currentMeeting = meeting;
                updateUI(meeting);
            }
        });

        // Zapisywanie zmian (tylko nazwa i adres wpisywane z palca)
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

    private void updateUI(Meeting meeting) {
        // Wypełniamy EditTexty tylko przy pierwszym załadowaniu
        if (isInitialLoad) {
            binding.etEditName.setText(meeting.getName());
            if (meeting.getLocation() != null) {
                binding.etEditAddress.setText(meeting.getLocation().getAddress());
            }
            isInitialLoad = false;
        }

        // Zawsze aktualizujemy przyciski (reagują na akcje z okien dialogowych)
        if (meeting.getStartTime() != null) {
            String timePart = meeting.getStartTime().contains("T")
                    ? meeting.getStartTime().split("T")[1].substring(0, 5)
                    : meeting.getStartTime();
            binding.btnChangeTime.setText("Time: " + timePart);
        }

        if (meeting.getDuration() != null) {
            binding.btnChangeDuration.setText("Duration: " + meeting.getDuration());
        }

        // Automatyczne odświeżanie Chipy z uczestnikami
        binding.cgMembers.removeAllViews();
        for (MeetingMember member : meeting.getMembers()) {
            addMemberChip(member);
        }
    }

    private void showAddMemberDialog() {
        // Uruchamiamy pobieranie znajomych w osobnym wątku, aby nie blokować UI
        // (na wypadek, gdyby getAvailableFriends() korzystało z bazy lokalnej)
        new Thread(() -> {
            List<User> allFriends = viewModel.getAvailableFriends();

            if (!isAdded()) return; // Zabezpieczenie przed crashem, gdy fragment zostanie zamknięty

            requireActivity().runOnUiThread(() -> {
                List<MeetingMember> currentMembers = currentMeeting.getMembers();
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
                            // Aktualizujemy bazę.
                            // UWAGA: Nie musimy już ręcznie manipulować Chipami, bo LiveData odświeży je w updateUI!
                            viewModel.setMeetingMembers(requireContext(), meetingId, selectedUsers);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }).start();
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
            // Tylko aktualizujemy bazę. LiveData samo wywoła updateUI i zmieni tekst na przycisku!
            viewModel.updateStartTime(requireContext(), meetingId, picker.getHour(), picker.getMinute());
        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");
    }

    private void showDurationPicker() {
        String[] durations = {"00:15:00", "00:30:00", "01:00:00", "02:00:00", "05:00:00"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select meeting duration")
                .setItems(durations, (dialog, which) -> {
                    // Tylko aktualizujemy bazę. LiveData odświeży przycisk!
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