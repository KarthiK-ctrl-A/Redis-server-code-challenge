package com.k.rlite.resp;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class RespCodecTest {

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void parseSimpleString() throws Exception {
        var in = new ByteArrayInputStream(bytes("+OK\r\n"));
        var reader = new RespReader(in);
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.SIMPLE_STRING);
        assertThat(msg.simple).isEqualTo("OK");
    }

    @Test
    void parseError() throws Exception {
        var in = new ByteArrayInputStream(bytes("-ERR something\r\n"));
        var reader = new RespReader(in);
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.ERROR);
        assertThat(msg.simple).isEqualTo("ERR something".substring(4));
    }

    @Test
    void parseInteger() throws Exception {
        var in = new ByteArrayInputStream(bytes(":123\r\n"));
        var reader = new RespReader(in);
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.INTEGER);
        assertThat(msg.integer).isEqualTo(123L);
    }

    @Test
    void parseNullBulk() throws Exception {
        var in = new ByteArrayInputStream(bytes("$-1\r\n"));
        var reader = new RespReader(in);
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.NULL_BULK_STRING);
        assertThat(msg.bulk).isNull();
    }

    @Test
    void parseEmptyBulk() throws Exception {
        var in = new ByteArrayInputStream(bytes("$0\r\n\r\n"));
        var reader = new RespReader(in);
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.BULK_STRING);
        assertThat(msg.bulk).isNotNull();
        assertThat(msg.bulk.length).isEqualTo(0);
    }

    @Test
    void parseBulkHello() throws Exception {
        var in = new ByteArrayInputStream(bytes("$5\r\nhello\r\n"));
        var reader = new RespReader(in);
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.BULK_STRING);
        assertThat(new String(msg.bulk, StandardCharsets.UTF_8)).isEqualTo("hello");
    }

    @Test
    void parseArrayEcho() throws Exception {
        String frame = "*2\r\n$4\r\nECHO\r\n$5\r\nhello\r\n";
        var reader = new RespReader(new ByteArrayInputStream(bytes(frame)));
        var msg = reader.read();
        assertThat(msg.type).isEqualTo(RespMessage.Type.ARRAY);
        assertThat(msg.array).hasSize(2);
        assertThat(msg.array.get(0).type).isEqualTo(RespMessage.Type.BULK_STRING);
        assertThat(new String(msg.array.get(0).bulk, StandardCharsets.UTF_8)).isEqualTo("ECHO");
        assertThat(new String(msg.array.get(1).bulk, StandardCharsets.UTF_8)).isEqualTo("hello");
    }

    @Test
    void writeThenReadRoundTrip() throws Exception {
        var bout = new ByteArrayOutputStream();
        var writer = new RespWriter(bout);
        writer.write(RespMessage.simple("OK"));
        writer.write(RespMessage.integer(42));
        writer.write(RespMessage.bulk("ping".getBytes(StandardCharsets.UTF_8)));

        var reader = new RespReader(new ByteArrayInputStream(bout.toByteArray()));
        assertThat(reader.read().simple).isEqualTo("OK");
        assertThat(reader.read().integer).isEqualTo(42);
        assertThat(new String(reader.read().bulk, StandardCharsets.UTF_8)).isEqualTo("ping");
    }

    @Test
    void invalidMissingCrlf() {
        var in = new ByteArrayInputStream(bytes("+OK\n"));
        var reader = new RespReader(in);
        assertThatThrownBy(reader::read).isInstanceOf(Exception.class);
    }

    @Test
    void invalidTruncatedBulk() {
        var in = new ByteArrayInputStream(bytes("$5\r\nhel"));
        var reader = new RespReader(in);
        assertThatThrownBy(reader::read).isInstanceOf(Exception.class);
    }
}
