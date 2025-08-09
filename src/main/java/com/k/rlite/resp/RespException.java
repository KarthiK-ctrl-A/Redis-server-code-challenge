package com.k.rlite.resp;

public class RespException extends RuntimeException {
    public RespException(String message) { super(message); }
    public RespException(String message, Throwable cause) { super(message, cause); }
}
