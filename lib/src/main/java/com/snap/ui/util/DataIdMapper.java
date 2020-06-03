package com.snap.ui.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RecyclerViews need a numeric stable id. Most of our
 * data sources only provide unique String identifier. Since Hashcode
 * is not guaranteed unique, track a mapping from the <code>String</code> identifier
 * to a unique <code>long</code>.
 */

public class DataIdMapper {

    public DataIdMapper() {
        
    }

    public DataIdMapper(Long seedValue) {
        mIdGenerator.set(seedValue);
    }

    private final ConcurrentHashMap<String, Long> mStableIds = new ConcurrentHashMap<>();
    private final AtomicLong mIdGenerator = new AtomicLong();

    public long getStableId(String key) {
        Long id = mStableIds.get(key);
        if (id == null) {
            long generatedId = mIdGenerator.incrementAndGet();
            Long existingId = mStableIds.putIfAbsent(key, generatedId);
            id = (existingId == null) ? generatedId : existingId;
        }
        return id;
    }
}