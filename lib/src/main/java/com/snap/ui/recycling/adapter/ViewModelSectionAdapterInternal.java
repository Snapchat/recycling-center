package com.snap.ui.recycling.adapter;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.core.os.TraceCompat;
import com.snap.ui.event.AttachToRecyclerViewEvent;
import com.snap.ui.event.AttachToRecyclerViewEvent.EventType;
import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.SectionController;
import com.snap.ui.recycling.adapter.errorhandling.AdapterErrorHandler;
import com.snap.ui.recycling.adapter.errorhandling.AdapterErrorInfo;
import com.snap.ui.recycling.factory.ViewFactory;
import com.snap.ui.recycling.factory.ViewModelViewHolder;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;
import com.snap.ui.seeking.Seekable;
import com.snap.ui.seeking.Seekables;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * <p>A {@link RecyclerView.Adapter} that uses {@link ViewFactory} as
 * its source for views, binding view models from {@link com.snap.ui.recycling.SectionController}s.
 */
class ViewModelSectionAdapterInternal extends RecyclerView.Adapter<ViewModelViewHolder>
        implements ViewModelAdapter {

    private static final int CACHE_SIZE = 50;
    private final ViewFactory viewFactory;
    private final EventDispatcher eventDispatcher;
    private @Nullable final AdapterErrorHandler errorHandler;
    private final LinkedHashMap<SectionController, Seekable<AdapterViewModel>> sections = new LinkedHashMap<>();

    private int itemCount;
    private final SectionItemInfoCache itemInfoCache;

    private boolean asyncBinding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final LinkedList<BindingRequest> bindingRequests = new LinkedList<>();
    private final PublishSubject<AttachToRecyclerViewEvent> mAttachPublisher = PublishSubject.create();
    private final AtomicBoolean asyncBindingScheduled = new AtomicBoolean(false);

    // How long an async binding may run before yielding the thread:
    private static final long ASYNC_BINDING_POLITENESS_TIMEOUT = 12;
    // If a bind has been stalled by this amount of time, fade the cell in:
    private static final long ASYNC_BINDING_ANIMATION_DELAY = 150;
    private static final long ASYNC_BINDING_ANIMATION_DURATION_MS = 100;

    public ViewModelSectionAdapterInternal(
            ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            @Nullable AdapterErrorHandler adapterErrorHandler) {
        this.viewFactory = viewFactory;
        this.eventDispatcher = eventDispatcher;
        this.errorHandler = adapterErrorHandler;

        this.itemInfoCache = new SectionItemInfoCache(viewFactory, CACHE_SIZE);
        setHasStableIds(true);
    }

    public ViewModelSectionAdapterInternal(
            ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            AdapterErrorHandler errorHandler,
            List<? extends SectionController> initialControllers) {
        this(viewFactory, eventDispatcher, errorHandler);
        for (SectionController sectionController : initialControllers) {
            sections.put(sectionController, Seekables.empty());
        }
        itemCount = computeSize();
        notifyChangedViews(0, Seekables.empty(), Seekables.empty(), 0, itemCount);
    }

    public void setAsyncBinding(boolean asyncBinding) {
        this.asyncBinding = asyncBinding;
    }

    /**
     * Replaces a single section's views.  Attempts to do an in-place update of the section if existing.
     *
     * Error handled by our adapter already
     */
    @MainThread
    void updateSection(SectionController section, Seekable<AdapterViewModel> views) {
        throwIfNotOnMainThread();
        int seen = 0;
        boolean existing = false;
        for (Map.Entry<SectionController, Seekable<AdapterViewModel>> entry : sections.entrySet()) {
            if (section == entry.getKey()) {
                existing = true;
                break;
            }
            seen += entry.getValue().size();
        }

        int oldAdapterSize = itemCount;
        Seekable<AdapterViewModel> oldViews = sections.put(section, views);
        itemCount = computeSize();
        if (existing) {
            itemInfoCache.evictAll(); // be conservative and evict the entire cache.
        }

        if (oldViews == null) {
            oldViews = Seekables.empty();
        }

        notifyChangedViews(seen, oldViews, views, oldAdapterSize, itemCount);
    }

    /**
     * Removes a single section and its views.
     */
    @MainThread
    void removeSection(SectionController section) {
        throwIfNotOnMainThread();
        int seen = 0;
        boolean existing = false;
        for (Map.Entry<SectionController, Seekable<AdapterViewModel>> entry : sections.entrySet()) {
            if (section == entry.getKey()) {
                existing = true;
                break;
            }
            seen += entry.getValue().size();
        }

        int oldAdapterSize = itemCount;
        Seekable<AdapterViewModel> oldViews = sections.remove(section);
        itemCount = computeSize();
        if (existing) {
            itemInfoCache.evictAll(); // be conservative and evict the entire cache.
        }

        if (oldViews == null) {
            oldViews = Seekables.empty();
        }

        Seekable<AdapterViewModel> views = Seekables.empty();
        notifyChangedViews(seen, oldViews, views, oldAdapterSize, itemCount);
    }

    @Override
    public @NonNull ViewModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            return viewFactory.createViewHolder(parent.getContext(), viewType, parent);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, null);
                return errorHandler.getViewModelViewHolder(parent.getContext());
            } else {
                throw e;
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewModelViewHolder holder, int position) {
        if (asyncBinding && !holder.hasBeenBound()) {
            bindAsync(holder, position);
        } else {
            bind(holder, position);
        }
    }

    private void bind(ViewModelViewHolder holder, int position) {
        try {
            TraceCompat.beginSection("RC:bind");
            SectionItemInfo info = getSectionInfoForPosition(position);
            holder.bind(info.viewModel, eventDispatcher, info.sectionController);
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, new AdapterErrorInfo(position));
            } else {
                throw e;
            }
        } finally {
            TraceCompat.endSection();
        }
    }

    @MainThread
    private void bindAsync(ViewModelViewHolder holder, int position) {
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        final boolean needsSizing = lp.width == LayoutParams.WRAP_CONTENT || lp.height == LayoutParams.WRAP_CONTENT;
        if (needsSizing) {
            holder.itemView.setVisibility(View.INVISIBLE);
        }

        bindingRequests.add(new BindingRequest(holder, position, needsSizing));
        if (asyncBindingScheduled.compareAndSet(false, true)) {
            handler.post(asyncBindingRunnable);
        }
    }

    @Override
    public void onViewRecycled(ViewModelViewHolder holder) {
        try {
            for (BindingRequest request : bindingRequests) {
                if (request.holder == holder) {
                    request.recycled = true;
                    return;
                }
            }
            holder.recycle();
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, null);
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean onFailedToRecycleView(ViewModelViewHolder holder) {
        try {
            return holder.onFailedToRecycleView();
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, null);
                return true;
            } else {
                throw e;
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        try {
            return getSectionInfoForPosition(position).itemViewTypeId;
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, new AdapterErrorInfo(position));
                return errorHandler.getErrorViewType();
            } else {
                throw e;
            }
        }

    }

    @Override
    public AdapterViewType getItemAdapterViewType(int position) {
        try {
            return getSectionInfoForPosition(position).viewModel.getType();
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, new AdapterErrorInfo(position));
                return errorHandler.getErrorAdapterViewType();
            } else {
                throw e;
            }
        }
    }

    @Override
    public AdapterViewModel getItemViewModel(int position) {
        try {
            return getSectionInfoForPosition(position).viewModel;
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, new AdapterErrorInfo(position));
                return errorHandler.getErrorAdapterViewModel();
            } else {
                throw e;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        try {
            return getSectionInfoForPosition(position).uniqueId;
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.onError(e, new AdapterErrorInfo(position));
                return errorHandler.getItemId();
            } else {
                throw e;
            }
        }
    }

    int computeSize() {
        int count = 0;
        for (Seekable seekable : sections.values()) {
            count += seekable.size();
        }
        return count;
    }

    @MainThread
    private SectionItemInfo getSectionInfoForPosition(int position) {
        return itemInfoCache.get(position);
    }

    /**
     * Generates a unique id for a model, for use by RecyclerView.Adapter's stableIds.
     * The data model id must be unique for a given viewType and section. This means
     * a data model can have the same id as long as the viewTypes are different-- useful
     * when using a database rowId to represent different types of data in the same RecyclerView,
     * also allowing a model to be used across sections, even with matching viewType.
     */
    private static long createUniqueId(AdapterViewModel model, int viewTypeId, int sectionId) {
        return (model.getId()) ^ ((long)sectionId << 40) ^ (((long)viewTypeId) << 52);
    }

    static class SectionItemInfo {
        int itemViewTypeId;
        AdapterViewModel viewModel;
        SectionController sectionController;
        long uniqueId;
    }

    private class SectionItemInfoCache extends LruCache<Integer, SectionItemInfo> {

        final ViewFactory viewFactory;

        // Recycle the evicted SectionItemInfo
        final AtomicReference<SectionItemInfo> evictedItemInfo = new AtomicReference<>();

        SectionItemInfoCache(ViewFactory viewFactory, int capacity) {
            super(capacity);
            this.viewFactory = viewFactory;
        }

        @Override
        protected SectionItemInfo create(Integer key) {
            throwIfNotOnMainThread();
            SectionItemInfo info = evictedItemInfo.getAndSet(null);
            if (info == null) {
                info = new SectionItemInfo();
            }

            int position = key;
            int seen = 0;
            SectionController foundAtSection = null;
            int foundAt;
            AdapterViewModel foundModel = null;
            int sectionId = 0;
            for (Map.Entry<SectionController, Seekable<AdapterViewModel>> seekable : sections.entrySet()) {
                int relativePos = position - seen;
                Seekable<AdapterViewModel> views = seekable.getValue();
                if (relativePos < views.size()) {
                    foundAt = relativePos;
                    foundAtSection = seekable.getKey();
                    foundModel = views.get(foundAt);
                    break;
                }
                seen += views.size();
                sectionId++;
            }

            if (foundModel == null) {
                throw new IllegalStateException();
            }

            info.viewModel = foundModel;
            info.itemViewTypeId = viewFactory.getViewTypeId(foundModel.getType());
            info.sectionController = foundAtSection;
            info.uniqueId = createUniqueId(info.viewModel, info.itemViewTypeId, sectionId);
            return info;
        }

        @Override
        protected void entryRemoved(boolean evicted, Integer key, SectionItemInfo oldValue,
                                    SectionItemInfo newValue) {
            evictedItemInfo.set(oldValue);
        }
    }

    /**
     * Notifies the given {@code adapter} to update only the views that have changed.
     * This results in less flickering when an update happens.
     * @param sectionOffset   Position of the first view of the section
     * @param previousViews   Views being replaced
     * @param newViews        Views being added
     */
    private void notifyChangedViews(int sectionOffset,
                                    Seekable<AdapterViewModel> previousViews,
                                    Seekable<AdapterViewModel> newViews,
                                    int oldAdapterSize,
                                    int newAdapterSize) {
        TraceCompat.beginSection("RV:diff");
        SingleSectionDiffUtilCallback callback =
                new SingleSectionDiffUtilCallback(sectionOffset, previousViews, newViews,
                        oldAdapterSize, newAdapterSize);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(callback, false);
        TraceCompat.endSection();
        diff.dispatchUpdatesTo(this);
    }

    /**
     * A DiffUtils callback that accepts old and new views from a single section
     * and computes the difference.
     */
    private static class SingleSectionDiffUtilCallback extends DiffUtil.Callback {

        private final Seekable<AdapterViewModel> oldViews;
        private final Seekable<AdapterViewModel> newViews;
        private final int sectionPositionOffset;

        private final int oldAdapterSize;
        private final int newAdapterSize;
        private final int oldSectionSize;
        private final int newSectionSize;

        SingleSectionDiffUtilCallback(int sectionOffset,
                                      Seekable<AdapterViewModel> originalList,
                                      Seekable<AdapterViewModel> newList,
                                      int oldAdapterSize,
                                      int newAdapterSize) {
            this.sectionPositionOffset = sectionOffset;
            this.oldViews = originalList;
            this.newViews = newList;
            this.oldAdapterSize = oldAdapterSize;
            this.newAdapterSize = newAdapterSize;

            this.oldSectionSize = oldViews.size();
            this.newSectionSize = newViews.size();
        }

        @Override
        public int getOldListSize() {
            return oldAdapterSize;
        }

        @Override
        public int getNewListSize() {
            return newAdapterSize;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // This callback only handles single-section updates.
            if (!viewsFromThisSection(oldItemPosition, newItemPosition)) {
                return viewsOutsideThisSectionAreSame(oldItemPosition, newItemPosition);
            }

            AdapterViewModel old = oldViews.get(reposition(oldItemPosition));
            AdapterViewModel gnu = newViews.get(reposition(newItemPosition));

            return old.areItemsTheSame(gnu);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            return oldViews.get(reposition(oldItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if (!viewsFromThisSection(oldItemPosition, newItemPosition)) {
                return true;
            }

            AdapterViewModel old = oldViews.get(reposition(oldItemPosition));
            AdapterViewModel gnu = newViews.get(reposition(newItemPosition));
            return old.areContentsTheSame(gnu);
        }

        int reposition(int position) {
            return position - sectionPositionOffset;
        }

        /**
         * Returns true if the views at both oldItemPosition and newItemPosition are from this section.
         */
        private boolean viewsFromThisSection(int oldItemPosition, int newItemPosition) {
            if (oldItemPosition < sectionPositionOffset ||
                    oldItemPosition >= sectionPositionOffset + oldSectionSize) {
                return false;
            }
            if (newItemPosition < sectionPositionOffset ||
                    newItemPosition >= sectionPositionOffset + newSectionSize) {
                return false;
            }

            return true;
        }

        private boolean viewsOutsideThisSectionAreSame(int oldItemPosition, int newItemPosition) {
            if (oldItemPosition < sectionPositionOffset) {
                // Item at oldItemPosition did not move.
                return oldItemPosition == newItemPosition;
            }
            if (oldItemPosition >= sectionPositionOffset + oldSectionSize) {
                // [0-9](10-19) -> [](0-9)
                // 11 -> 1
                return newItemPosition == oldItemPosition - oldSectionSize + newSectionSize;
            }
            return false;
        }
    }

    private static class BindingRequest {
        final ViewModelViewHolder<?> holder;
        final int position;
        final boolean makeVisible;
        final long requestTime;

        boolean recycled;

        BindingRequest(ViewModelViewHolder<?> holder, int position, boolean makeVisible) {
            this.holder = holder;
            this.position = position;
            this.makeVisible = makeVisible;
            this.requestTime = System.nanoTime();
        }
    }

    private final Runnable asyncBindingRunnable = new Runnable() {
        @Override
        public void run() {
            BindingRequest request;
            long startTime = System.nanoTime();
            while ((request = bindingRequests.poll()) != null) {
                ViewModelViewHolder<?> holder = request.holder;
                if (request.recycled) {
                    continue;
                }
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    bind(holder, pos);
                    long now = System.nanoTime();
                    if (request.makeVisible) {
                        holder.itemView.setVisibility(View.VISIBLE);
                        long latencyMs = TimeUnit.MILLISECONDS.convert(now - request.requestTime, TimeUnit.NANOSECONDS);
                        if (latencyMs > ASYNC_BINDING_ANIMATION_DELAY) {
                            holder.itemView.setAlpha(0f);
                            holder.itemView.animate()
                                    .alpha(1)
                                    .withLayer()
                                    .setDuration(ASYNC_BINDING_ANIMATION_DURATION_MS);
                        }
                    }

                    long runtime = TimeUnit.MILLISECONDS.convert(now - startTime, TimeUnit.NANOSECONDS);
                    if (runtime > ASYNC_BINDING_POLITENESS_TIMEOUT) {
                        break;
                    }
                }

            }

            if (bindingRequests.isEmpty()) {
                asyncBindingScheduled.set(false);
            } else {
                handler.post(asyncBindingRunnable);
            }
        }
    };

    public Observable<AttachToRecyclerViewEvent> observeAttachToRecyclerViewEvents() {
        return mAttachPublisher;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAttachPublisher.onNext(new AttachToRecyclerViewEvent(EventType.ATTACH, recyclerView));
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAttachPublisher.onNext(new AttachToRecyclerViewEvent(EventType.DETACH, recyclerView));
    }

    private static void throwIfNotOnMainThread() {
        throw new IllegalStateException("You can only modify sections on main thread.");
    }
}
