package com.snap.ui.recycling.adapter;

import android.content.Context;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.atomic.AtomicBoolean;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import io.reactivex.disposables.Disposable;

/**
 * Special adapter that allows you to loop the provided adapter indefinitely
 */
public class LoopingAdapter<T extends ViewHolder> extends RecyclerView.Adapter<T> implements Disposable {

    static public class LoopingLayoutManager extends LinearLayoutManager {

        private int mPendingScrollPosition;
        private boolean mScrollDisabled;

        public LoopingLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
            mPendingScrollPosition = NO_POSITION;
            mScrollDisabled = false;
        }

        @Override
        public void onLayoutCompleted(RecyclerView.State state) {
            super.onLayoutCompleted(state);
            mPendingScrollPosition = NO_POSITION;
            mScrollDisabled = false;
            setItemPrefetchEnabled(true);
        }

        @Override
        public void scrollToPosition(int position) {
            super.scrollToPosition(position);
            mPendingScrollPosition = position;
        }

        @Override
        public void scrollToPositionWithOffset(int position, int offset) {
            super.scrollToPositionWithOffset(position, offset);
            mPendingScrollPosition = position;
        }

        @Override
        public boolean canScrollHorizontally() {
            return super.canScrollHorizontally() && !mScrollDisabled;
        }

        public void disableScrollingUntilLayout() {
            mScrollDisabled = true;
            setItemPrefetchEnabled(false);
        }

        public int getCurrentScrollPosition() {
            return mPendingScrollPosition == NO_POSITION ? findFirstVisibleItemPosition() : mPendingScrollPosition;
        }
    }

    @NonNull
    private final RecyclerView.Adapter adapter;
    @NonNull
    private final LoopingLayoutManager layoutManager;

    private int scrollOffsetX;
    private int scrollOffsetY;

    private final RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        private final Object mScrollLock = new Object();

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            synchronized (mScrollLock) {
                int scrollPosition = computePreviousScrollPosition();
                if (positionStart <= scrollPosition) {
                    scrollPosition += itemCount;
                }
                ensureLooping(scrollPosition);
                notifyItemRangeInserted(getLoopingPosition(adapter.getItemCount(), positionStart), itemCount);
            }
        }


        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            synchronized (mScrollLock) {
                int scrollPosition = computePreviousScrollPosition();
                if (positionStart + itemCount <= scrollPosition) {
                    scrollPosition -= itemCount;
                } else if (positionStart <= scrollPosition) {
                    shouldResetPositionOnChange = true;
                }
                ensureLooping(scrollPosition);
                notifyItemRangeRemoved(getLoopingPosition(adapter.getItemCount(), positionStart), itemCount);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            synchronized (mScrollLock) {
                int scrollPosition = computePreviousScrollPosition();
                ensureLooping(scrollPosition);
                notifyItemRangeChanged(getLoopingPosition(adapter.getItemCount(), positionStart), itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            synchronized (mScrollLock) {
                int scrollPosition = computePreviousScrollPosition();
                if (fromPosition < scrollPosition && toPosition > scrollPosition) {
                    scrollPosition -= itemCount;
                } else if (fromPosition > scrollPosition && toPosition < scrollPosition) {
                    scrollPosition += itemCount;
                }
                ensureLooping(scrollPosition);

                if (itemCount == 1) {
                    notifyItemMoved(getLoopingPosition(adapter.getItemCount(), fromPosition),
                            getLoopingPosition(adapter.getItemCount(), toPosition));
                } else {
                    notifyDataSetChanged();
                }
            }
        }
    };

    @GuardedBy("this")
    private boolean subscribed;

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    private int previousCount;

    /**
     * If set to true, the position will reset to 0 when the adapter contents change
     */
    private boolean shouldResetPositionOnChange = false;

    private LoopingAdapter(
            @NonNull final RecyclerView.Adapter adapter,
            @NonNull final LoopingLayoutManager layoutManager) {
        this.adapter = adapter;
        this.layoutManager = layoutManager;
    }

    /**
     * Computes current scroll position based on the previous count of items
     * @return
     */
    private int computePreviousScrollPosition() {
        int countBefore = previousCount != 0 ? previousCount : adapter.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (firstVisibleItemPosition != NO_POSITION) {
            final View firstView = layoutManager.findViewByPosition(firstVisibleItemPosition);

            if (firstView != null) {
                // Record the previous scroll offsets for later scroll use
                boolean isLayoutReversed = layoutManager.getReverseLayout();
                scrollOffsetX = isLayoutReversed ? firstView.getRight() : firstView.getLeft();
                scrollOffsetY = isLayoutReversed ? firstView.getBottom() : firstView.getTop();
            }
        }
        return Math.max(0, layoutManager.getCurrentScrollPosition()) % countBefore;
    }

    /**
     * Ensure that the recycler view is looping via scrolling to the middle of the really large list
     */
    private void ensureLooping(int currentPosition) {
        if (shouldResetPositionOnChange) {
            currentPosition = 0;
        }
        // scroll to the middle of the adapter to fake looping
        if (adapter.getItemCount() == 0) {
            layoutManager.requestLayout();
            layoutManager.disableScrollingUntilLayout();
        } else {
            // Scroll to the position with previous scroll offset so user don't see flickering when updating data
            final int scrollOffset =
                    layoutManager.getOrientation() == LinearLayoutManager.VERTICAL ? scrollOffsetY : scrollOffsetX;
            layoutManager.scrollToPositionWithOffset(
                    getLoopingPosition(adapter.getItemCount(), currentPosition),
                    scrollOffset);
        }
        previousCount = adapter.getItemCount();
    }

    /**
     * Compute position in infinitely looping carousel using raw position and adapter item count
     */
    public static int getLoopingPosition(int itemCount, int currentPosition) {
        return itemCount == 0 ? 0 : (Integer.MAX_VALUE / itemCount / 2) * itemCount + currentPosition;
    }

    /**
     * If set to true, the position will be reset to 0 when the adapter contents change. Off by default.
     */
    public void setShouldResetPositionOnChange(boolean shouldResetPositionOnChange) {
        this.shouldResetPositionOnChange = shouldResetPositionOnChange;
    }

    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return (T) adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(T holder, int position) {
        position = position % adapter.getItemCount();
        adapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount() > 0 ? Integer.MAX_VALUE : 0;
    }

    @Override
    public int getItemViewType(int position) {
        position = position % adapter.getItemCount();
        return adapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        position = position % adapter.getItemCount();
        return adapter.getItemId(position);
    }

    @Override
    public void onViewRecycled(T holder) {
        adapter.onViewRecycled(holder);
    }

    public synchronized Disposable subscribe() {
        subscribed = true;
        adapter.registerAdapterDataObserver(dataObserver);
        return this;
    }

    @Override
    public synchronized void dispose() {
        if (disposed.compareAndSet(false, true)) {
            adapter.unregisterAdapterDataObserver(dataObserver);
            subscribed = false;
        }
    }

    @Override
    public synchronized boolean isDisposed() {
        return disposed.get();
    }

    /**
     * Intall the looping adapter on an existing recycler view
     * @param recyclerView
     * @return
     */
    @NonNull
    public static Disposable install(
            @NonNull final RecyclerView recyclerView,
            @NonNull final RecyclerView.Adapter adapter) {
        LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null || !(layoutManager instanceof LoopingLayoutManager)) {
            throw new IllegalArgumentException("Only LinearLayoutManager is supported for a LoopingAdapter.");
        }
        LoopingAdapter loopingAdapter = new LoopingAdapter(adapter, (LoopingLayoutManager) layoutManager);
        recyclerView.setAdapter(loopingAdapter);
        return loopingAdapter.subscribe();
    }
}
