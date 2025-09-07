package com.k.rlite.cmd;

import com.k.rlite.resp.RespWriter;
import java.io.IOException;

public interface Command {
    void handle(byte[][] args, RespWriter out) throws IOException;
}
