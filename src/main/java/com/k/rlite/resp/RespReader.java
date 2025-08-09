package com.k.rlite.resp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespReader {

    private final BufferedInputStream in;

    public RespReader(InputStream in) {
        this.in = new BufferedInputStream(in);
    }

    public RespMessage read() throws IOException {
        int b = in.read();
        if (b == -1) throw new EOFException("Stream ended before type byte");
        switch (b) {
            case '+': return readSimpleString();
            case '-': return readError();
            case ':': return readInteger();
            case '$': return readBulkString();
            case '*': return readArray();
            default: throw new RespException("Unknown RESP type byte: " + (char) b);
        }
    }

    private RespMessage readSimpleString() throws IOException {
        String s = readLine();
        return RespMessage.simple(s);
    }

    private RespMessage readError() throws IOException {
        String s = readLine(); // e.g. "ERR something" or "WRONGTYPE Operation..."
        String msg = s;
        int idx = s.indexOf(' ');
        // If there's a space and the token before it is ALL CAPS, treat it as an error code
        if (idx > 0) {
            String maybeCode = s.substring(0, idx);
            if (!maybeCode.isEmpty() && maybeCode.chars().allMatch(Character::isUpperCase)) {
                msg = s.substring(idx + 1);
            }
        }
        return RespMessage.error(msg);
    }

    private RespMessage readInteger() throws IOException {
        String s = readLine();
        try {
            long v = Long.parseLong(s.trim());
            return RespMessage.integer(v);
        } catch (NumberFormatException e) {
            throw new RespException("Invalid integer: " + s, e);
        }
    }

    private RespMessage readBulkString() throws IOException {
        String lenLine = readLine();
        int len;
        try {
            len = Integer.parseInt(lenLine.trim());
        } catch (NumberFormatException e) {
            throw new RespException("Invalid bulk length: " + lenLine, e);
        }
        if (len == -1) return RespMessage.bulk(null); // Null bulk string

        if (len < 0) throw new RespException("Negative bulk length: " + len);
        byte[] data = readExactly(len);
        // Expect CRLF after the bulk bytes
        int cr = in.read();
        int lf = in.read();
        if (cr != '\r' || lf != '\n') throw new RespException("Bulk string missing terminating CRLF");
        return RespMessage.bulk(data);
    }

    private RespMessage readArray() throws IOException {
        String countLine = readLine();
        int count;
        try {
            count = Integer.parseInt(countLine.trim());
        } catch (NumberFormatException e) {
            throw new RespException("Invalid array length: " + countLine, e);
        }
        if (count == -1) return RespMessage.array(null); // Null array
        if (count < 0) throw new RespException("Negative array length: " + count);

        List<RespMessage> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            items.add(read());
        }
        return RespMessage.array(items);
    }

    // ---------- helpers ----------

    private String readLine() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(64);
        int prev = -1;
        int cur;
        while ((cur = in.read()) != -1) {
            if (prev == '\r' && cur == '\n') {
                // remove the '\r' we already wrote
                byte[] bytes = buf.toByteArray();
                int len = bytes.length - 1; // drop last '\r'
                if (len < 0) len = 0;
                return new String(bytes, 0, len, StandardCharsets.UTF_8);
            }
            buf.write(cur);
            prev = cur;
        }
        throw new EOFException("Stream ended before CRLF");
    }

    private byte[] readExactly(int len) throws IOException {
        byte[] out = new byte[len];
        int off = 0;
        while (off < len) {
            int r = in.read(out, off, len - off);
            if (r == -1) throw new EOFException("Stream ended mid-bulk (" + off + "/" + len + ")");
            off += r;
        }
        return out;
    }
}
