package com.k.rlite.server;

import com.k.rlite.cmd.CommandRouter;
import com.k.rlite.store.InMemoryStore;
import com.k.rlite.store.Store;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RedisServer {
    private RedisServer() {}

    public static void start(int port) throws IOException {
        Store store = new InMemoryStore();
        CommandRouter router = CommandRouter.defaultRouter(store);

        ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Redis Lite Server running on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                exec.submit(() -> new ClientHandler(client, router).run());
            }
        }
    }
}
