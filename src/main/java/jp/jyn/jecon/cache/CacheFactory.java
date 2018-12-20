package jp.jyn.jecon.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheFactory {
    public final static int DISABLE = 0;
    public final static int INFINITY = -1;

    private CacheFactory() {

    }

    public static <K, V> Map<K, V> createCache(int size) {
        if (size == -1) {
            return new HashMap<>();
        } else if (size == 0) {
            return NoOpMap.getInstance();
        } else if (size > 0) {
            return new LRUMap<>(size);
        }
        throw new IllegalArgumentException("Invalid size");
    }
}
