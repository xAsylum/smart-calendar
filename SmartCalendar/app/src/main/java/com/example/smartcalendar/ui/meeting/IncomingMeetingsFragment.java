package com.example.smartcalendar.ui.meeting;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smartcalendar.R;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.databinding.FragmentIncomingMeetingsBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomingMeetingsFragment extends Fragment {

    private FragmentIncomingMeetingsBinding binding;
    private MeetingViewModel viewModel;
    private IncomingMeetingsAdapter adapter;
    private final List<Meeting> todayMeetings = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    requestLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    requestLocation();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentIncomingMeetingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);

        adapter = new IncomingMeetingsAdapter(todayMeetings);
        binding.rvIncomingMeetings.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvIncomingMeetings.setAdapter(adapter);

        binding.toggleTransportMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String mode = (checkedId == R.id.btn_mode_driving) ? "driving" : "walking";
                adapter.setTransportMode(mode);
            }
        });

        viewModel.getAllMeetingsLive(requireContext()).observe(getViewLifecycleOwner(), meetings -> {
            if (meetings != null) {
                todayMeetings.clear();
                Date now = new Date();
                String todayStr = dateFormat.format(now);

                for (Meeting meeting : meetings) {
                    if (meeting.getStartTime() != null && meeting.getStartTime().startsWith(todayStr)) {
                        try {
                            Date startTime = apiFormat.parse(meeting.getStartTime());
                            if (startTime != null && startTime.after(now)) {
                                todayMeetings.add(meeting);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
                Collections.sort(todayMeetings, (m1, m2) -> {
                    try {
                        Date d1 = apiFormat.parse(m1.getStartTime());
                        Date d2 = apiFormat.parse(m2.getStartTime());
                        if (d1 != null && d2 != null) {
                            return d1.compareTo(d2);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                });

                adapter.notifyDataSetChanged();
            }
        });
        
        checkLocationPermission();
        viewModel.refreshMeetings(requireContext());
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        } else {
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void requestLocation() {
        LocationHelper.getLastLocation(requireContext(), (lat, lng) -> {
            if (adapter != null) {
                adapter.setUserLocation(lat, lng);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
