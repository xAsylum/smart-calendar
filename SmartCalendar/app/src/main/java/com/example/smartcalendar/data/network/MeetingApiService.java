package com.example.smartcalendar.data.network;

import com.example.smartcalendar.data.models.friend.FriendListResponse;
import com.example.smartcalendar.data.models.friendrequest.FriendRequestSchema;
import com.example.smartcalendar.data.models.friendrequest.FriendRequestListResponse;
import com.example.smartcalendar.data.models.auth.LoginRequest;
import com.example.smartcalendar.data.models.auth.LoginResponse;
import com.example.smartcalendar.data.models.meeting.Meeting;
import com.example.smartcalendar.data.models.meeting.MeetingListResponse;
import com.example.smartcalendar.data.models.meeting.MeetingMember;
import com.example.smartcalendar.data.models.meeting.MeetingMembersResponse;
import com.example.smartcalendar.data.models.meeting.TravelTimeResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MeetingApiService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("auth/register")
    Call<LoginResponse> register(@Body LoginRequest request);

    @GET("meetings")
    Call<MeetingListResponse> getMeetings(@Header("Authorization") String token);

    @POST("meetings")
    Call<Meeting> createMeeting(@Header("Authorization") String token, @Body Meeting meeting);

    @PUT("meetings/{meeting_id}")
    Call<Meeting> updateMeeting(
            @Header("Authorization") String token,
            @Path("meeting_id") int id,
            @Body Meeting meeting
    );

    @DELETE("meetings/{meeting_id}")
    Call<Void> deleteMeeting(
            @Header("Authorization") String token,
            @Path("meeting_id") int id
    );

    @GET("meetings/{meeting_id}/members")
    Call<MeetingMembersResponse> getMeetingMembers(
            @Header("Authorization") String token,
            @Path("meeting_id") int meetingId
    );

    @POST("meetings/{meeting_id}/members")
    Call<Void> updateMeetingMembers(
            @Header("Authorization") String token,
            @Path("meeting_id") int meetingId,
            @Body List<Integer> memberIds
    );

    @GET("friends")
    Call<FriendListResponse> getFriends(@Header("Authorization") String token);

    @GET("friends/requests")
    Call<FriendRequestListResponse> getReceivedRequests(@Header("Authorization") String token);

    @GET("friends/requests/sent")
    Call<FriendRequestListResponse> getSentRequests(@Header("Authorization") String token);

    @POST("friends/requests")
    Call<Void> sendFriendRequest(@Header("Authorization") String token, @Body FriendRequestSchema request);

    @POST("friends/requests/{sender_id}/accept")
    Call<Void> acceptFriendRequest(@Header("Authorization") String token, @Path("sender_id") int senderId);

    @POST("friends/requests/{sender_id}/reject")
    Call<Void> rejectFriendRequest(@Header("Authorization") String token, @Path("sender_id") int senderId);

    @DELETE("friends/requests/{friend_id}")
    Call<Void> cancelFriendRequest(@Header("Authorization") String token, @Path("friend_id") int friendId);

    @GET("distance/travel-time")
    Call<TravelTimeResponse> getTravelTime(
            @Query("origin_lat") double originLat,
            @Query("origin_lng") double originLng,
            @Query("dest_lat") double destLat,
            @Query("dest_lng") double destLng,
            @Query("mode") String mode
    );
}
