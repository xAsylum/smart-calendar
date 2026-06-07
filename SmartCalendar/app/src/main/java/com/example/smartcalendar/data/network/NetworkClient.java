package com.example.smartcalendar.data.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkClient {
    private static MeetingApiService apiService;
    private static PhotonApiService photonApiService;
    public static final String BASE_URL = "https://poliomyelitic-paulina-indecipherably.ngrok-free.dev/";
    public static final String PHOTON_URL = "https://photon.komoot.io/";
    
    public static MeetingApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            apiService = retrofit.create(MeetingApiService.class);
        }
        return apiService;
    }

    public static PhotonApiService getPhotonApiService() {
        if (photonApiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(PHOTON_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            photonApiService = retrofit.create(PhotonApiService.class);
        }
        return photonApiService;
    }

    public static String getWsUrl(int meetingId, String token) {
        String wsBase = BASE_URL.replace("https://", "wss://").replace("http://", "ws://");
        return wsBase + "ws/chat/" + meetingId + "?token=" + token;
    }
}
