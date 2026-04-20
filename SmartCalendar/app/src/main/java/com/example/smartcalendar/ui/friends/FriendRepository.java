package com.example.smartcalendar.ui.friends;

import com.example.smartcalendar.data.models.User;

import java.util.ArrayList;
import java.util.List;

public class FriendRepository {
    private static FriendRepository instance;
    private List<User> myFriends = new ArrayList<>();

    private FriendRepository() {
        generateMockFriends();
    }

    public static synchronized FriendRepository getInstance() {
        if (instance == null) instance = new FriendRepository();
        return instance;
    }

    private void generateMockFriends() {
        myFriends.add(new User(101, "alicja_kowalska"));
        myFriends.add(new User(102, "bartosz_nowak"));
        myFriends.add(new User(103, "czarek_it"));
        myFriends.add(new User(104, "daria_programistka"));
    }

    public List<User> getFriends() {
        return new ArrayList<>(myFriends);
    }
}