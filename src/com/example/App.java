package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portEnv = System.getenv("PORT"); // Cloud Run 注入
        if (portEnv != null && !portEnv.isBlank()) {
            try { port = Integer.parseInt(portEnv); } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        // 只注册 "/"，在内部根据 path 分发
        server.createContext("/", (HttpExchange ex) -> {
            String path = ex.getRequestURI().getPath(); // 例如 "/", "/healthz"
            switch (path) {
                case "/":
                    sendText(ex, 200, "Hello from Cloud Run (no Maven)!");
                    break;
                case "/healthz":
                    // 可加一些快速自检逻辑
                    sendText(ex, 200, "ok");
                    break;
                default:
                    sendText(ex, 404, "not found: " + path);
            }
        });

        System.out.println("✅ Server started on port " + port);
        server.start();
    }

    private static void sendText(HttpExchange ex, int status, String body) throws IOException {
        byte[] resp = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store"); // 避免中间层缓存干扰
        ex.sendResponseHeaders(status, resp.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(resp); }
    }
}