package com.k.rlite.resp;

import java.util.List;

public final class RespMessage {
    public enum Type {
        SIMPLE_STRING, ERROR, INTEGER, BULK_STRING, ARRAY, NULL_BULK_STRING, NULL_ARRAY
    }

    public final Type type;
    public final String simple;        // SIMPLE_STRING or ERROR
    public final long integer;         // INTEGER
    public final byte[] bulk;          // BULK_STRING (null indicates NULL_BULK_STRING)
    public final List<RespMessage> array; // ARRAY (null indicates NULL_ARRAY)

    private RespMessage(Type type, String simple, long integer, byte[] bulk, List<RespMessage> array) {
        this.type = type;
        this.simple = simple;
        this.integer = integer;
        this.bulk = bulk;
        this.array = array;
    }

    public static RespMessage simple(String s) { return new RespMessage(Type.SIMPLE_STRING, s, 0, null, null); }
    public static RespMessage error(String s) { return new RespMessage(Type.ERROR, s, 0, null, null); }
    public static RespMessage integer(long v) { return new RespMessage(Type.INTEGER, null, v, null, null); }
    public static RespMessage bulk(byte[] b) {
        return (b == null) ? new RespMessage(Type.NULL_BULK_STRING, null, 0, null, null)
                : new RespMessage(Type.BULK_STRING, null, 0, b, null);
    }
    public static RespMessage array(List<RespMessage> a) {
        return (a == null) ? new RespMessage(Type.NULL_ARRAY, null, 0, null, null)
                : new RespMessage(Type.ARRAY, null, 0, null, List.copyOf(a));
    }
}
