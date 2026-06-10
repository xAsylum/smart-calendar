package com.example.smartcalendar.ui.calendar;

import android.graphics.Color;
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
import com.example.smartcalendar.data.local.AppDatabase;
import com.example.smartcalendar.data.local.TokenManager;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.databinding.FragmentCalendarBinding;
import com.example.smartcalendar.ui.meeting.MeetingViewModel;
import com.example.smartcalendar.ui.calendar.decorators.MeetingDecorator;
import com.example.smartcalendar.ui.meeting.MeetingAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private MeetingAdapter adapter;
    private MeetingViewModel viewModel;
    private final List<Meeting> currentDisplayedMeetings = new ArrayList<>();
    private Map<CalendarDay, List<Meeting>> allMeetingsMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);

        setupRecyclerView();

        viewModel.getAllMeetingsLive(requireContext()).observe(getViewLifecycleOwner(), meetings -> {
            if (meetings != null) {
                allMeetingsMap = groupMeetingsByDate(meetings);
                updateCalendarDecorators(allMeetingsMap.keySet());
                updateMeetingList(binding.customCalendarView.getSelectedDate());
            }
        });

        viewModel.refreshMeetings(requireContext());

        binding.customCalendarView.setOnDateChangedListener((widget, date, selected) -> updateMeetingList(date));

        binding.buttonLogout.setOnClickListener(v -> {
            new Thread(() -> {
                AppDatabase.getInstance(requireContext()).clearAllTables();
                TokenManager.getInstance().deleteToken(requireContext());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Navigation.findNavController(view).navigate(R.id.action_SecondFragment_to_FirstFragment);
                    });
                }
            }).start();
        });

        binding.buttonFriends.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_CalendarFragment_to_FriendsFragment);
        });

        binding.buttonIncoming.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_CalendarFragment_to_IncomingMeetingsFragment);
        });

        binding.fabAddMeeting.setOnClickListener(v -> {
            CalendarDay date = binding.customCalendarView.getSelectedDate();
            if (date == null) date = CalendarDay.today();

            viewModel.createNewMeeting(requireContext(), date, serverId -> {
                Bundle bundle = new Bundle();
                bundle.putInt("meeting_id", serverId);
                Navigation.findNavController(view).navigate(R.id.action_CalendarFragment_to_MeetingManagementFragment, bundle);
            });
        });

        binding.customCalendarView.setSelectedDate(CalendarDay.today());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshMeetings(requireContext());
        }
    }

    private void setupRecyclerView() {
        int currentUserId = TokenManager.getInstance().getUserId(requireContext());
        adapter = new MeetingAdapter(
                currentDisplayedMeetings, 
                currentUserId, 
                meeting -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("meeting_id", meeting.getId());
                    Navigation.findNavController(requireView()).navigate(R.id.action_CalendarFragment_to_MeetingDetailFragment, bundle);
                }, 
                meeting -> {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete Meeting")
                            .setMessage("Are you sure you want to delete this meeting?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                viewModel.deleteMeeting(requireContext(), meeting.getId());
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );
        binding.rvMeetings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMeetings.setAdapter(adapter);
    }

    private void updateMeetingList(CalendarDay date) {
        if (date == null) return;
        currentDisplayedMeetings.clear();
        List<Meeting> meetingsForDate = allMeetingsMap.get(date);
        if (meetingsForDate != null) {
            currentDisplayedMeetings.addAll(meetingsForDate);
        }
        adapter.notifyDataSetChanged();
    }

    private void updateCalendarDecorators(Set<CalendarDay> days) {
        binding.customCalendarView.removeDecorators();
        binding.customCalendarView.addDecorator(new MeetingDecorator(days, Color.RED));
    }

    private Map<CalendarDay, List<Meeting>> groupMeetingsByDate(List<Meeting> meetings) {
        Map<CalendarDay, List<Meeting>> map = new HashMap<>();
        for (Meeting m : meetings) {
            try {
                if (m.getStartTime() == null || !m.getStartTime().contains("T")) continue;
                String[] dateParts = m.getStartTime().split("T")[0].split("-");
                CalendarDay day = CalendarDay.from(
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2])
                );
                if (!map.containsKey(day)) map.put(day, new ArrayList<>());
                map.get(day).add(m);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return map;
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
