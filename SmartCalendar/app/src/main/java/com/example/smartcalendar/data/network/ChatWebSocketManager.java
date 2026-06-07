package com.example.smartcalendar.data.network;

import android.util.Log;

import com.example.smartcalendar.data.models.chat.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketManager {
    private static final String TAG = "ChatWebSocket";
    private final OkHttpClient client;
    private WebSocket webSocket;
    private final ChatCallback callback;
    private final Gson gson = new Gson();

    public interface ChatCallback {
        void onHistoryReceived(List<ChatMessage> messages);
        void onNewMessageReceived(ChatMessage message);
        void onError(String error);
    }

    public ChatWebSocketManager(ChatCallback callback) {
        this.client = new OkHttpClient.Builder().build();
        this.callback = callback;
    }

    public void connect(String url) {
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket Connected");
                fetchHistory(0);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JsonObject json = gson.fromJson(text, JsonObject.class);
                    String type = json.get("type").getAsString();

                    if ("history".equals(type)) {
                        List<ChatMessage> messages = gson.fromJson(json.get("messages"), 
                            new TypeToken<List<ChatMessage>>(){}.getType());
                        callback.onHistoryReceived(messages);
                    } else if ("new_message".equals(type)) {
                        ChatMessage message = gson.fromJson(json.get("message"), ChatMessage.class);
                        callback.onNewMessageReceived(message);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing message", e);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket Failure", t);
                callback.onError(t.getMessage());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                Log.d(TAG, "WebSocket Closing: " + reason);
            }
        });
    }

    public void fetchHistory(int lastId) {
        JsonObject json = new JsonObject();
        json.addProperty("action", "fetch_history");
        json.addProperty("last_id", lastId);
        if (webSocket != null) {
            webSocket.send(json.toString());
        }
    }

    public void sendMessage(String text) {
        JsonObject json = new JsonObject();
        json.addProperty("action", "send_message");
        json.addProperty("message", text);
        if (webSocket != null) {
            webSocket.send(json.toString());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "User disconnected");
        }
    }
}
