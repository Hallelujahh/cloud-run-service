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
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try { port = Integer.parseInt(portEnv); } catch (NumberFormatException ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", (HttpExchange ex) -> {
            // 记录一下收到的请求，便于 Cloud Run 日志里排查
            String method = ex.getRequestMethod();
            String rawPath = ex.getRequestURI().getPath();

            // 规范化：去掉非根路径的尾斜杠，统一小写（/HealthZ 也能命中）
            String path = rawPath;
            if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
            path = path.toLowerCase();

            // HEAD 请求按 GET 处理但不返回 body
            boolean head = "HEAD".equalsIgnoreCase(method);

            switch (path) {
                case "/":
                    sendText(ex, 200, "Hello from Cloud Run (no Maven)!", head);
                    break;
                case "/healthz":
                    // 允许 /healthz 与 /healthz/ 都命中；HEAD/GET 都返回 200
                    sendText(ex, 200, "ok", head);
                    break;
                default:
                    sendText(ex, 404, "not found: " + rawPath, head);
            }
        });

        System.out.println("✅ Server started on port " + port);
        server.start();
    }

    private static void sendText(HttpExchange ex, int status, String body, boolean headOnly) throws IOException {
        byte[] resp = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        if (headOnly) {
            ex.sendResponseHeaders(status, -1); // HEAD：只发响应头
            ex.close();
            return;
        }
        ex.sendResponseHeaders(status, resp.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(resp); }
    }
}