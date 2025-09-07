package com.k.rlite.store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryStore implements Store {
    private final ConcurrentMap<String, byte[]> map = new ConcurrentHashMap<>();

    @Override public byte[] get(String key) { return map.get(key); }
    @Override public byte[] put(String key, byte[] value) { return map.put(key, value); }
}
