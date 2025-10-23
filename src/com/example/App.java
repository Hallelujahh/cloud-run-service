package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws Exception {
        // 默认端口 8080（Cloud Run 会通过 PORT 环境变量传入）
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException ignored) {}
        }

        // 监听所有地址（Cloud Run 需要 0.0.0.0）
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        // 根路径：返回 Hello 文本
        server.createContext("/", (HttpExchange exchange) -> {
            String response = "Hello from Cloud Run (no Maven)!";
            sendResponse(exchange, 200, response);
        });

        // 健康检查路径
        server.createContext("/healthz", (HttpExchange exchange) -> {
            sendResponse(exchange, 200, "ok");
        });

        System.out.println("✅ Server started on port " + port);
        server.start();
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] resp = body.getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }
}