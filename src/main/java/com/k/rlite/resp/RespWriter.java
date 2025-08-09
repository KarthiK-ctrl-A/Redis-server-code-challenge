package com.k.rlite.resp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RespWriter {
    private static final byte[] CRLF = new byte[]{'\r', '\n'};

    private final OutputStream out;

    public RespWriter(OutputStream out) {
        this.out = out;
    }

    public void write(RespMessage m) throws IOException {
        switch (m.type) {
            case SIMPLE_STRING -> writeSimple(m.simple);
            case ERROR -> writeError(m.simple);
            case INTEGER -> writeInteger(m.integer);
            case BULK_STRING -> writeBulk(m.bulk);
            case NULL_BULK_STRING -> writeNullBulk();
            case ARRAY -> writeArray(m.array);
            case NULL_ARRAY -> writeNullArray();
        }
        out.flush();
    }

    public void writeSimple(String s) throws IOException {
        out.write('+');
        writeUtf8(s);
        out.write(CRLF);
    }

    public void writeError(String s) throws IOException {
        out.write('-');
        writeUtf8(s);
        out.write(CRLF);
    }

    public void writeInteger(long v) throws IOException {
        out.write(':');
        writeUtf8(Long.toString(v));
        out.write(CRLF);
    }

    public void writeBulk(byte[] data) throws IOException {
        out.write('$');
        writeUtf8(Integer.toString(data.length));
        out.write(CRLF);
        out.write(data);
        out.write(CRLF);
    }

    public void writeNullBulk() throws IOException {
        out.write('$');
        writeUtf8("-1");
        out.write(CRLF);
    }

    public void writeArray(List<RespMessage> items) throws IOException {
        out.write('*');
        writeUtf8(Integer.toString(items.size()));
        out.write(CRLF);
        for (RespMessage m : items) {
            write(m);
        }
    }

    public void writeNullArray() throws IOException {
        out.write('*');
        writeUtf8("-1");
        out.write(CRLF);
    }

    private void writeUtf8(String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
    }
}
