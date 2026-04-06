package com.campasian.service;

import com.campasian.config.AgoraConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Hosts a tiny local page that runs Agora Web SDK inside the user's browser.
 */
public final class BrowserCallBridgeService {

    private static final BrowserCallBridgeService INSTANCE = new BrowserCallBridgeService();

    private final Gson gson = new Gson();
    private final Map<String, BrowserCallSession> sessions = new ConcurrentHashMap<>();

    private HttpServer server;
    private int port;

    public static BrowserCallBridgeService getInstance() {
        return INSTANCE;
    }

    private BrowserCallBridgeService() {
    }

    public synchronized void ensureStarted() throws IOException {
        if (server != null) return;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/call/audio", new CallPageHandler());
        server.createContext("/call/session", new SessionStateHandler());
        server.createContext("/call/event", new SessionEventHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        port = server.getAddress().getPort();
    }

    public void registerSession(String callId, Consumer<String> eventListener) {
        sessions.compute(callId, (id, existing) -> {
            BrowserCallSession session = existing != null ? existing : new BrowserCallSession();
            session.eventListener = eventListener;
            return session;
        });
    }

    public void updateMute(String callId, boolean muted) {
        BrowserCallSession session = sessions.computeIfAbsent(callId, key -> new BrowserCallSession());
        session.muted = muted;
    }

    public void requestEnd(String callId) {
        BrowserCallSession session = sessions.computeIfAbsent(callId, key -> new BrowserCallSession());
        session.endRequested = true;
    }

    public void clearSession(String callId) {
        sessions.remove(callId);
    }

    public URI buildLaunchUri(String callId, String channelName, String userId, String friendName) throws IOException {
        ensureStarted();
        String query = "callId=" + encode(callId)
            + "&channel=" + encode(channelName)
            + "&userId=" + encode(userId)
            + "&friendName=" + encode(friendName != null ? friendName : "Friend")
            + "&appId=" + encode(AgoraConfig.getAppId())
            + "&uid=" + Math.abs(userId.hashCode());
        return URI.create("http://127.0.0.1:" + port + "/call/audio?" + query);
    }

    public void launchBrowser(URI uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri);
        } else {
            throw new IOException("Desktop browser launch is not supported on this machine.");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    private final class CallPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] body;
            try (InputStream inputStream = getClass().getResourceAsStream("/web/call-audio.html")) {
                if (inputStream == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                body = inputStream.readAllBytes();
            }
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }

    private final class SessionStateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String callId = queryParam(exchange.getRequestURI(), "callId");
            BrowserCallSession session = callId != null ? sessions.get(callId) : null;
            JsonObject json = new JsonObject();
            json.addProperty("muted", session != null && session.muted);
            json.addProperty("endRequested", session != null && session.endRequested);
            byte[] body = gson.toJson(json).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        }
    }

    private final class SessionEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String callId = queryParam(exchange.getRequestURI(), "callId");
            if (callId == null || callId.isBlank()) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String type = null;
            try {
                JsonObject json = gson.fromJson(body, JsonObject.class);
                if (json != null && json.has("type")) {
                    type = json.get("type").getAsString();
                }
            } catch (Exception ignored) {
            }

            BrowserCallSession session = sessions.get(callId);
            if (session != null && session.eventListener != null && type != null && !type.isBlank()) {
                session.eventListener.accept(type);
            }
            exchange.sendResponseHeaders(204, -1);
        }
    }

    private static String queryParam(URI uri, String name) {
        if (uri == null || uri.getQuery() == null) return null;
        for (String pair : uri.getQuery().split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && name.equals(parts[0])) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static final class BrowserCallSession {
        private volatile boolean muted;
        private volatile boolean endRequested;
        private volatile Consumer<String> eventListener;
    }
}
