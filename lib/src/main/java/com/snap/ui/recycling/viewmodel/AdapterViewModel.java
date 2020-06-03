package com.snap.ui.recycling.viewmodel;

import androidx.annotation.CallSuper;

import com.snap.ui.recycling.AdapterViewType;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

/**
 * Base class for our view models used in our RecyclerView Adapters.
 * <p>Two important properties of a view model help keep the UI fast and flicker-free: (1) a strong
 * notion of unique ID for the data, and (2) ability to track "sameness" of two models having the same id.
 * {@link #getId()} and {@link #areContentsTheSame(AdapterViewModel)} are the methods used to achieve these properties.
 * <p>
 * <p>In order to support stableIds to reduce latency and jank when refreshing a RecyclerView,
 * The ViewModelAdapter will generate a unique id for a ViewModel based on (1) its model id, (2) its viewType, and
 * (3) its section.
 **/
public class AdapterViewModel {

    private static final AtomicLong sIdGenerator = new AtomicLong();

    protected final long mId;

    final AdapterViewType mAdapterViewType;
    private @Nullable String mContentDescription;
    private @Nullable String mDebugInfo;

    /**
     * Generates a unique ID for an item in a content group.  Whenever possible, the ID should be assigned by using a
     * unique property of the item (story ID, user name, etc), to allow reuse.
     */
    public static long generateItemId() {
        return sIdGenerator.incrementAndGet();
    }

    public AdapterViewModel(AdapterViewType viewType) {
        this(viewType, generateItemId());
    }

    public AdapterViewModel(AdapterViewType viewType, long id) {
        mAdapterViewType = viewType;
        mId = id;
    }

    public AdapterViewType getType() {
        return mAdapterViewType;
    }

    public long getId() {
        return mId;
    }

    public String getContentDescription() {
        return mContentDescription;
    }

    public String getDebugInfo() {
        return mDebugInfo;
    }

    public void setDebugInfo(@Nullable String debugInfo) {
        mDebugInfo = debugInfo;
    }

    public void setContentDescription(@Nullable String contentDescription) {
        mContentDescription = contentDescription;
    }

    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     * For example, if your items have unique ids, this method should check their id equality.
     */
    public final boolean areItemsTheSame(AdapterViewModel model) {
        return getId() == model.getId() && getType().equals(model.getType());
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     *
     * This method is called only if areItemsTheSame(int, int) returns true for these items.
     */
    public boolean areContentsTheSame(AdapterViewModel model) {
        return true;
    }
}
