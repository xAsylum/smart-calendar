package com.example.smartcalendar.ui.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.MeetingRepository;
import com.example.smartcalendar.data.models.Meeting;
import com.example.smartcalendar.databinding.FragmentCalendarBinding;
import com.example.smartcalendar.ui.meeting.MeetingViewModel;
import com.example.smartcalendar.ui.calendar.decorators.MeetingDecorator;
import com.example.smartcalendar.ui.meeting.MeetingAdapter;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private MeetingAdapter adapter;
    private MeetingViewModel viewModel;
    private final List<Meeting> currentDisplayedMeetings = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);

        MeetingAdapter.OnMeetingClickListener listener = meeting -> {
            Bundle bundle = new Bundle();
            bundle.putInt("meeting_id", meeting.getId());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_CalendarFragment_to_MeetingDetailFragment, bundle);
        };

        adapter = new MeetingAdapter(currentDisplayedMeetings, listener);
        binding.rvMeetings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMeetings.setAdapter(adapter);

        refreshCalendarDecorators();

        binding.customCalendarView.setOnDateChangedListener((widget, date, selected) -> {
            updateMeetingList(date);
        });

        binding.buttonLogout.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_SecondFragment_to_FirstFragment)
        );

        binding.customCalendarView.setSelectedDate(CalendarDay.today());
        updateMeetingList(CalendarDay.today());
    }

    private void refreshCalendarDecorators() {
        Map<CalendarDay, List<Meeting>> meetingsMap = MeetingRepository.getInstance().getMeetingsGroupedByDate();
        binding.customCalendarView.removeDecorators();
        binding.customCalendarView.addDecorator(
                new MeetingDecorator(meetingsMap.keySet(), Color.RED)
        );
    }

    private void updateMeetingList(CalendarDay date) {
        currentDisplayedMeetings.clear();

        Map<CalendarDay, List<Meeting>> meetingsMap = MeetingRepository.getInstance().getMeetingsGroupedByDate();
        List<Meeting> foundMeetings = meetingsMap.get(date);

        if (foundMeetings != null) {
            currentDisplayedMeetings.addAll(foundMeetings);
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCalendarDecorators();
        updateMeetingList(binding.customCalendarView.getSelectedDate());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}