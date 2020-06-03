package com.snap.ui.recycling.factory;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class DelegatedConcurrentHashMap<K, V> extends HashMap<K, V> {

    private final ConcurrentHashMap<K, V> mDelegate;

    public DelegatedConcurrentHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        mDelegate = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    public DelegatedConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
        mDelegate = new ConcurrentHashMap<>(initialCapacity);
    }

    public DelegatedConcurrentHashMap() {
        super();
        mDelegate = new ConcurrentHashMap<>();
    }

    public DelegatedConcurrentHashMap(Map<? extends K, ? extends V> m) {
        super(m);
        mDelegate = new ConcurrentHashMap<>(m);
    }

    @Override
    public int size() {
        return mDelegate.size();
    }

    @Override
    public boolean isEmpty() {
        return mDelegate.isEmpty();
    }

    @Nullable
    @Override
    public V get(@Nullable Object key) {
        return mDelegate.get(key);
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return mDelegate.containsKey(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return mDelegate.put(key, value);
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        mDelegate.putAll(m);
    }

    @Nullable
    @Override
    public V remove(@Nullable Object key) {
        if (key != null) {
            return mDelegate.remove(key);
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        mDelegate.clear();
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return mDelegate.containsValue(value);
    }

    @NonNull
    @Override
    public synchronized Set<K> keySet() {
        Enumeration<K> keys = mDelegate.keys();
        HashSet<K> set = new HashSet<>();
        while (keys.hasMoreElements()) {
            set.add(keys.nextElement());
        }
        return set;
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return mDelegate.values();
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return mDelegate.entrySet();
    }

    @TargetApi(VERSION_CODES.N)
    @Nullable
    @Override
    public V getOrDefault(@Nullable Object key, @Nullable V defaultValue) {
        return mDelegate.getOrDefault(key, defaultValue);
    }

    @TargetApi(VERSION_CODES.N)
    @Nullable
    @Override
    public V putIfAbsent(K key, V value) {
        return mDelegate.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(@Nullable Object key, @Nullable Object value) {
        return mDelegate.remove(key, value);
    }

    @Override
    public boolean replace(K key, @Nullable V oldValue, V newValue) {
        return mDelegate.replace(key, oldValue, newValue);
    }

    @Nullable
    @Override
    public V replace(K key, V value) {
        return mDelegate.replace(key, value);
    }

    @TargetApi(VERSION_CODES.N)
    @Nullable
    @Override
    public V computeIfAbsent(
            K key, @NonNull Function<? super K, ? extends V> mappingFunction) {
        return mDelegate.computeIfAbsent(key, mappingFunction);
    }

    @TargetApi(VERSION_CODES.N)
    @Nullable
    @Override
    public V computeIfPresent(
            K key, @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mDelegate.computeIfPresent(key, remappingFunction);
    }

    @TargetApi(VERSION_CODES.N)
    @Nullable
    @Override
    public V compute(
            K key, @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return mDelegate.compute(key, remappingFunction);
    }

    @TargetApi(VERSION_CODES.N)
    @Nullable
    @Override
    public V merge(
            K key,
            @NonNull V value,
            @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return mDelegate.merge(key, value, remappingFunction);
    }

    @TargetApi(VERSION_CODES.N)
    @Override
    public void forEach(@NonNull BiConsumer<? super K, ? super V> action) {
        mDelegate.forEach(action);
    }

    @TargetApi(VERSION_CODES.N)
    @Override
    public void replaceAll(@NonNull BiFunction<? super K, ? super V, ? extends V> function) {
        mDelegate.replaceAll(function);
    }

    @TargetApi(VERSION_CODES.N)
    @NonNull
    @Override
    public Object clone() {
        return new DelegatedConcurrentHashMap<>(mDelegate);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DelegatedConcurrentHashMap)) {
            return false;
        }
        return mDelegate.equals(((DelegatedConcurrentHashMap<?, ?>) o).mDelegate);
    }

    @Override
    public int hashCode() {
        return mDelegate.hashCode();
    }

    @Override
    public String toString() {
        return mDelegate.toString();
    }
}
