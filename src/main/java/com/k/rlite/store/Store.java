package com.k.rlite.store;

public interface Store {
    byte[] get(String key);
    byte[] put(String key, byte[] value);
}
