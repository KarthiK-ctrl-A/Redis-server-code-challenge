package com.k.rlite.cmd;

import com.k.rlite.cmd.commands.EchoCommand;
import com.k.rlite.cmd.commands.GetCommand;
import com.k.rlite.cmd.commands.PingCommand;
import com.k.rlite.cmd.commands.SetCommand;
import com.k.rlite.resp.RespMessage;
import com.k.rlite.resp.RespWriter;
import com.k.rlite.store.Store;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CommandRouter {
    private final Map<String, Command> commands = new HashMap<>();
    private final Store store;

    public CommandRouter(Store store) {
        this.store = store;
    }

    public static CommandRouter defaultRouter(Store store) {
        var r = new CommandRouter(store);
        r.register("PING", new PingCommand());
        r.register("ECHO", new EchoCommand());
        r.register("SET",  new SetCommand(store));
        r.register("GET",  new GetCommand(store));
        return r;
    }

    public void register(String name, Command impl) {
        commands.put(name.toUpperCase(Locale.ROOT), impl);
    }

    public void handle(RespMessage msg, RespWriter out) throws IOException {
        if (msg.type != RespMessage.Type.ARRAY || msg.array == null || msg.array.isEmpty()) {
            out.write(RespMessage.error("ERR Protocol error: expected array of bulk strings"));
            return;
        }

        var head = msg.array.get(0);
        if (head.type != RespMessage.Type.BULK_STRING || head.bulk == null) {
            out.write(RespMessage.error("ERR Protocol error: command name must be bulk string"));
            return;
        }

        var cmdName = new String(head.bulk, StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT);
        var impl = commands.get(cmdName);
        if (impl == null) {
            out.write(RespMessage.error("ERR unknown command '" + cmdName + "'"));
            return;
        }

        int argc = msg.array.size() - 1;
        byte[][] args = new byte[argc][];
        for (int i = 0; i < argc; i++) {
            var it = msg.array.get(i + 1);
            if (it.type == RespMessage.Type.BULK_STRING) {
                args[i] = it.bulk;
            } else if (it.type == RespMessage.Type.NULL_BULK_STRING) {
                args[i] = null;
            } else {
                out.write(RespMessage.error("ERR Protocol error: argument is not bulk string"));
                return;
            }
        }

        impl.handle(args, out);
    }
}
