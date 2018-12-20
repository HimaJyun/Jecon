package jp.jyn.jecon.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * "Do nothing" cache
 *
 * @param <K> Key
 * @param <V> Value
 */
public class NoOpMap<K, V> implements Map<K, V> {
    private final static NoOpMap instance = new NoOpMap();
    @SuppressWarnings("unchecked")
    public static <K, V> NoOpMap<K, V> getInstance() {
        return (NoOpMap<K, V>) instance;
    }
    private NoOpMap() {
    }

    @Override
    public int size() {
        return 0;
    }
    @Override
    public boolean isEmpty() {
        return true;
    }
    @Override
    public boolean containsKey(Object key) {
        return false;
    }
    @Override
    public boolean containsValue(Object value) {
        return false;
    }
    @Override
    public V get(Object key) {
        return null;
    }
    @Override
    public V put(K key, V value) {
        return null;
    }
    @Override
    public V remove(Object key) {
        return null;
    }
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }
    @Override
    public void clear() {

    }
    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }
    @Override
    public Collection<V> values() {
        return Collections.emptySet();
    }
    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return defaultValue;
    }
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }
    @Override
    public V replace(K key, V value) {
        return null;
    }
    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Objects.requireNonNull(mappingFunction).apply(key);
    }
    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        return null;
    }
    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Objects.requireNonNull(remappingFunction).apply(key, null);
    }
    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        return value;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
