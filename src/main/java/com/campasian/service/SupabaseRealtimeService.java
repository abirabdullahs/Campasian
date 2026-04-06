package com.campasian.service;

import com.campasian.config.SupabaseConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Minimal Supabase Realtime websocket subscriber for Postgres changes.
 */
public final class SupabaseRealtimeService implements WebSocket.Listener {

    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final AtomicInteger refCounter = new AtomicInteger(1);
    private ScheduledExecutorService heartbeatExecutor;

    private volatile WebSocket socket;
    private volatile String topic;
    private volatile Consumer<JsonObject> changeListener;

    public void subscribeToCalls(String userId, String bearerToken, Consumer<JsonObject> onChange) {
        unsubscribe();
        if (userId == null || userId.isBlank()) return;

        this.changeListener = onChange;
        this.topic = "realtime:calls-" + Math.abs(userId.hashCode());
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "supabase-realtime-heartbeat");
            thread.setDaemon(true);
            return thread;
        });

        String base = SupabaseConfig.getSupabaseUrl().replace("https://", "wss://").replace("http://", "ws://");
        String websocketUrl = base + "/realtime/v1/websocket?apikey=" + SupabaseConfig.getAnonKey() + "&vsn=1.0.0";
        client.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .buildAsync(URI.create(websocketUrl), this)
            .thenAccept(ws -> {
                socket = ws;
                joinCallsChannel(userId, bearerToken);
                heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 20, 20, TimeUnit.SECONDS);
            });
    }

    public void unsubscribe() {
        WebSocket existing = socket;
        socket = null;
        if (existing != null) {
            try {
                existing.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
            } catch (Exception ignored) {
            }
        }
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
            heartbeatExecutor = null;
        }
    }

    private void joinCallsChannel(String userId, String bearerToken) {
        JsonObject payload = new JsonObject();
        JsonObject config = new JsonObject();
        JsonObject broadcast = new JsonObject();
        broadcast.addProperty("ack", false);
        broadcast.addProperty("self", false);
        config.add("broadcast", broadcast);

        JsonObject presence = new JsonObject();
        presence.addProperty("enabled", false);
        config.add("presence", presence);

        JsonArray postgresChanges = new JsonArray();
        JsonObject receiverFilter = new JsonObject();
        receiverFilter.addProperty("event", "*");
        receiverFilter.addProperty("schema", "public");
        receiverFilter.addProperty("table", "calls");
        receiverFilter.addProperty("filter", "receiver_id=eq." + userId);
        postgresChanges.add(receiverFilter);

        JsonObject callerFilter = new JsonObject();
        callerFilter.addProperty("event", "*");
        callerFilter.addProperty("schema", "public");
        callerFilter.addProperty("table", "calls");
        callerFilter.addProperty("filter", "caller_id=eq." + userId);
        postgresChanges.add(callerFilter);

        config.add("postgres_changes", postgresChanges);
        payload.add("config", config);
        if (bearerToken != null && !bearerToken.isBlank()) {
            payload.addProperty("access_token", bearerToken);
        }

        send(topic, "phx_join", payload);
    }

    private void sendHeartbeat() {
        if (socket == null) return;
        send("phoenix", "heartbeat", new JsonObject());
    }

    private void send(String sendTopic, String event, JsonObject payload) {
        WebSocket current = socket;
        if (current == null) return;
        JsonObject envelope = new JsonObject();
        envelope.addProperty("topic", sendTopic);
        envelope.addProperty("event", event);
        envelope.add("payload", payload);
        envelope.addProperty("ref", String.valueOf(refCounter.getAndIncrement()));
        envelope.addProperty("join_ref", "1");
        current.sendText(gson.toJson(envelope), true);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            JsonObject message = JsonParser.parseString(data.toString()).getAsJsonObject();
            String event = message.has("event") ? message.get("event").getAsString() : "";
            if ("postgres_changes".equals(event) && changeListener != null) {
                JsonObject payload = message.getAsJsonObject("payload");
                if (payload != null) changeListener.accept(payload);
            }
        } catch (Exception ignored) {
        }
        webSocket.request(1);
        return null;
    }
}
