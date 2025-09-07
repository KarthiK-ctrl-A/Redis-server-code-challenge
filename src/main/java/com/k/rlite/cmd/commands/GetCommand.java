package com.k.rlite.cmd.commands;

import com.k.rlite.cmd.Command;
import com.k.rlite.resp.RespMessage;
import com.k.rlite.resp.RespWriter;
import com.k.rlite.store.Store;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class GetCommand implements Command {
    private final Store store;

    public GetCommand(Store store) {
        this.store = store;
    }

    @Override
    public void handle(byte[][] args, RespWriter out) throws IOException {
        if (args.length != 1) {
            out.write(RespMessage.error("ERR wrong number of arguments for 'GET' command"));
            return;
        }
        if (args[0] == null) {
            out.write(RespMessage.bulk(null));
            return;
        }
        String key = new String(args[0], StandardCharsets.UTF_8);
        byte[] val = store.get(key);
        if (val == null) {
            out.write(RespMessage.bulk(null));
        } else {
            out.write(RespMessage.bulk(val));
        }
    }
}
