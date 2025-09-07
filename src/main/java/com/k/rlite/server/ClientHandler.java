package com.k.rlite.server;

import com.k.rlite.cmd.CommandRouter;
import com.k.rlite.resp.RespMessage;
import com.k.rlite.resp.RespReader;
import com.k.rlite.resp.RespWriter;

import java.io.IOException;
import java.net.Socket;

public final class ClientHandler implements Runnable {
    private final Socket socket;
    private final CommandRouter router;

    public ClientHandler(Socket socket, CommandRouter router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try (socket;
             var in = socket.getInputStream();
             var out = socket.getOutputStream()) {

            var reader = new RespReader(in);
            var writer = new RespWriter(out);

            while (true) {
                RespMessage msg;
                try {
                    msg = reader.read();
                } catch (IOException e) {
                    break;
                }

                try {
                    router.handle(msg, writer);
                } catch (Exception e) {
                    writer.write(RespMessage.error("ERR " + e.getMessage()));
                }
            }
        } catch (IOException ignored) {}
    }
}
