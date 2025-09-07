package com.k.rlite.cmd.commands;

import com.k.rlite.cmd.Command;
import com.k.rlite.resp.RespMessage;
import com.k.rlite.resp.RespWriter;

import java.io.IOException;

public final class PingCommand implements Command {
    @Override
    public void handle(byte[][] args, RespWriter out) throws IOException {
        if (args.length == 0) {
            out.write(RespMessage.simple("PONG"));
        } else if (args.length == 1) {
            out.write(RespMessage.bulk(args[0]));
        } else {
            out.write(RespMessage.error("ERR wrong number of arguments for 'PING' command"));
        }
    }
}
