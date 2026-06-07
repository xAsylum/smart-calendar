package com.example.smartcalendar.ui.meeting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.smartcalendar.data.models.photon.PhotonResponse;
import com.example.smartcalendar.data.network.NetworkClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class PhotonAdapter extends ArrayAdapter<PhotonResponse.Feature> {
    private List<PhotonResponse.Feature> suggestions = new ArrayList<>();

    public PhotonAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Nullable
    @Override
    public PhotonResponse.Feature getItem(int position) {
        return suggestions.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        PhotonResponse.Feature feature = getItem(position);
        if (feature != null) {
            textView.setText(feature.getProperties().getDisplayName());
        }
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    try {
                        Response<PhotonResponse> response = NetworkClient.getPhotonApiService()
                                .getSuggestions(constraint.toString(), 10)
                                .execute();
                        if (response.isSuccessful() && response.body() != null) {
                            suggestions = response.body().getFeatures();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof PhotonResponse.Feature) {
                    return ((PhotonResponse.Feature) resultValue).getProperties().getDisplayName();
                }
                return super.convertResultToString(resultValue);
            }
        };
    }
}
