package com.k.rlite;
import com.k.rlite.server.RedisServer;

public class Main {
    public static void main(String[] args) throws Exception {
        RedisServer.start(6379);
    }
}
