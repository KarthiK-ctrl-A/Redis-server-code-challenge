package com.k.rlite.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer {
    public static void start(int port) throws IOException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Requires JDK 21

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Redis Lite Server running on port " + port);

            while (true) {
                Socket client = serverSocket.accept();
                executor.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket socket) {
        try (socket) {
            System.out.println("Client connected: " + socket.getRemoteSocketAddress());
            // TODO: Implement RESP parsing and command execution here
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
