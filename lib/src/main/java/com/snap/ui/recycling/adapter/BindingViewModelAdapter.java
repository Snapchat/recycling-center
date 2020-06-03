package com.snap.ui.recycling.adapter;

import android.os.CancellationSignal;
import androidx.annotation.IntDef;
import androidx.annotation.MainThread;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.google.common.collect.ImmutableList;
import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.seeking.Seekable;
import com.snap.ui.recycling.factory.ViewFactory;
import com.snap.ui.recycling.factory.ViewModelViewHolder;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;
import com.snap.ui.seeking.Seekables;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link RecyclerView.Adapter} that uses {@link ViewFactory} as
 * its source for views, and binds a simple list of {@link AdapterViewModel}.
 */
public class BindingViewModelAdapter extends RecyclerView.Adapter<ViewModelViewHolder> implements ViewModelAdapter {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BIND_SYNC, BIND_ASYNC_SECTIONS, BIND_ASYNC_SECTIONS_ANIMATED, BIND_ASYNC_CELLS })
    public @interface BindMethod {}

    public static final int BIND_SYNC = 0; // Normal inline RecyclerView binding
    public static final int BIND_ASYNC_SECTIONS = 1; // Bind each section in a different render pass
    public static final int BIND_ASYNC_SECTIONS_ANIMATED = 2; // Bind each section async, and fade it in
    public static final int BIND_ASYNC_CELLS = 3; // Bind individual views async (with synchronous inflation)

    @BindMethod
    public static final int BIND_METHOD = BIND_SYNC;

    private static final int ANIMATION_DURATION_MS = 100;
    private final ViewFactory mSearchViewFactory;
    private final EventDispatcher mEventDispatcher;

    private Seekable<? extends AdapterViewModel> mViewModels = Seekables.empty();

    /**
     * A map of position -> async bindings we have in flight, used to cancel them if data changes prior to us finishing
     * the binding.
     */
    private final SparseArray<CancellationSignal> mPendingBinds = new SparseArray<>();

    private final boolean mAsyncBinding;

    public BindingViewModelAdapter(ViewFactory viewFactory, EventDispatcher eventDispatcher) {
        this(viewFactory, eventDispatcher, BIND_METHOD == BIND_ASYNC_CELLS);
    }

    public BindingViewModelAdapter(ViewFactory viewFactory, EventDispatcher eventDispatcher, boolean asyncBinding) {
        mSearchViewFactory = viewFactory;
        mEventDispatcher = eventDispatcher;
        mAsyncBinding = asyncBinding;
        setHasStableIds(true);
    }

    @Override
    public int getItemCount() {
        return mViewModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mSearchViewFactory.getViewTypeId(mViewModels.get(position).getType());
    }

    @Override
    public AdapterViewModel getItemViewModel(int position) {
        return mViewModels.get(position);
    }

    @Override
    public AdapterViewType getItemAdapterViewType(int position) {
        return mViewModels.get(position).getType();
    }

    @Override
    public long getItemId(int position) {
        return mViewModels.get(position).getId();
    }

    @Override
    public ViewModelViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mSearchViewFactory.createViewHolder(parent.getContext(), viewType, parent);
    }

    @Override
    public void onBindViewHolder(final ViewModelViewHolder holder, int position) {
        if (mAsyncBinding && !holder.hasBeenBound()) {
            LayoutParams lp = holder.itemView.getLayoutParams();
            final boolean needsSizing = lp.width == LayoutParams.WRAP_CONTENT || lp.height == LayoutParams.WRAP_CONTENT;
            if (needsSizing) {
                holder.itemView.setVisibility(View.INVISIBLE);
            }

            final CancellationSignal cancellationSignal = new CancellationSignal();
            holder.itemView.post(new Runnable() {
                @Override
                public void run() {
                    if (cancellationSignal.isCanceled()) {
                        return;
                    }

                    int pos = holder.getAdapterPosition();
                    mPendingBinds.remove(pos);
                    if (pos != RecyclerView.NO_POSITION) {
                        holder.bind(mViewModels.get(pos), mEventDispatcher, null);
                        if (needsSizing) {
                            holder.itemView.setVisibility(View.VISIBLE);
                            holder.itemView.setAlpha(0f);
                            holder.itemView.animate()
                                    .alpha(1)
                                    .withLayer()
                                    .setDuration(ANIMATION_DURATION_MS);
                        }
                    }
                }
            });

            mPendingBinds.put(holder.getAdapterPosition(), cancellationSignal);
        } else {
            holder.bind(mViewModels.get(position), mEventDispatcher, null);
        }
    }

    @Override
    public void onViewRecycled(ViewModelViewHolder holder) {
        cancelPendingBinding(holder.getAdapterPosition());
        if (holder.getBoundViewModel() != null) {
            holder.recycle();
        }
    }

    private void cancelPendingBinding(int adapterPosition) {
        CancellationSignal pendingBinding = mPendingBinds.get(adapterPosition);
        if (pendingBinding != null) {
            pendingBinding.cancel();
        }
        mPendingBinds.remove(adapterPosition);
    }

    @MainThread
    public void updateViewModels(Seekable<? extends AdapterViewModel> views) {
        if (views instanceof ImmutableList && views == mViewModels) {
            return;
        }

        Seekable<? extends AdapterViewModel> previousViews = mViewModels;
        mViewModels = views;
        notifyChangedViews(previousViews, views);
    }

    /**
     * Notifies the given {@code adapter} to update only the views that have changed.  This results in less flickering
     * when an update happens.
     */
    private void notifyChangedViews(
            Seekable<? extends AdapterViewModel> previousViews,
            Seekable<? extends AdapterViewModel> newViews) {
        int oldSize = previousViews.size();
        int newSize = newViews.size();
        int maxUpdateRange = Math.max(oldSize, newSize);

        for (int i = 0; i < maxUpdateRange; i++) {
            if (i >= newSize) {
                int itemCount = oldSize - newSize;
                // change the binding cancellation to use an AdapterDataOberver
                for (int pos = i; pos < i + itemCount; pos++) {
                    cancelPendingBinding(pos);
                }
                notifyItemRangeRemoved(i, itemCount);
                return;
            }
            if (i >= oldSize) {
                notifyItemRangeInserted(i, newSize - oldSize);
                return;
            }

            // TODO also update ranges
            AdapterViewModel newView = newViews.get(i);
            AdapterViewModel oldView = previousViews.get(i);
            if (!newView.areItemsTheSame(oldView) || !newView.areContentsTheSame(oldView)) {
                cancelPendingBinding(i);
                notifyItemChanged(i);
            }
        }
    }
}
