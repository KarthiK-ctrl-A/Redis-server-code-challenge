package com.k.rlite.cmd.commands;

import com.k.rlite.cmd.Command;
import com.k.rlite.resp.RespMessage;
import com.k.rlite.resp.RespWriter;
import com.k.rlite.store.Store;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class SetCommand implements Command {
    private final Store store;

    public SetCommand(Store store) {
        this.store = store;
    }

    @Override
    public void handle(byte[][] args, RespWriter out) throws IOException {
        if (args.length != 2) {
            out.write(RespMessage.error("ERR wrong number of arguments for 'SET' command"));
            return;
        }
        if (args[0] == null) {
            out.write(RespMessage.error("ERR key is null"));
            return;
        }
        String key = new String(args[0], StandardCharsets.UTF_8);
        byte[] value = (args[1] == null) ? new byte[0] : args[1];
        store.put(key, value);
        out.write(RespMessage.simple("OK"));
    }
}
