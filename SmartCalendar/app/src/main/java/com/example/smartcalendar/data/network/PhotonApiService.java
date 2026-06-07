package com.example.smartcalendar.data.network;

import com.example.smartcalendar.data.models.photon.PhotonResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotonApiService {
    @GET("api/")
    Call<PhotonResponse> getSuggestions(@Query("q") String query, @Query("limit") int limit);
}
