package com.snap.ui.recycling;

import com.snap.ui.recycling.adapter.BindingViewModelAdapter;
import com.snap.ui.seeking.Seekables;
import com.snap.ui.recycling.viewmodel.HorizontalScrollerModel;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * Presents a horizontal scroll view ({@link RecyclerView}) of
 * {@link com.snap.ui.recycling.viewmodel.AdapterViewModel} items.
 */
public class HorizontalRecyclerViewBinding extends ViewBinding<HorizontalScrollerModel<?>> {

    public static final @LayoutRes int LAYOUT = R.layout.recycling_center_recycler_view;

    private BindingViewModelAdapter mAdapter;
    private /* lateinit */ RecyclerView mRecyclerView;

    protected BindingViewModelAdapter createAdapter(HorizontalScrollerModel<?> model) {
        return new BindingViewModelAdapter(model.getViewFactory(), model.getEventDispatcher());
    }

    protected RecyclerView getBackingRecyclerView() {
        return mRecyclerView;
    }

    @Override
    protected void onCreate(View itemView) {
        Context context = itemView.getContext();
        mRecyclerView = (RecyclerView)itemView;
        LayoutManager layoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL,
                false /*reverseLayout*/);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mRecyclerView.setItemAnimator(null);
    }

    @Override
    protected void onBind(HorizontalScrollerModel<?> model, HorizontalScrollerModel<?> previousModel) {

        if (mAdapter == null) {
            // First-bind initialization
            mAdapter = createAdapter(model);
            mRecyclerView.setAdapter(mAdapter);

            updateLayoutParams(model);
            ItemDecorator spacingDecorator = new ItemDecorator(model.getPadding(), model.getOffset());
            mRecyclerView.addItemDecoration(spacingDecorator);
        } else if (previousModel == null || model.getScrollerHeight() != previousModel.getScrollerHeight()) {
            updateLayoutParams(model);
        }

        // TODO base model should have a Seekable
        mAdapter.updateViewModels(Seekables.copyOf(model.getModels()));
    }

    private void updateLayoutParams(HorizontalScrollerModel<?> model) {
        LayoutParams lp = mRecyclerView.getLayoutParams();
        lp.height = model.getScrollerHeight();
        mRecyclerView.setLayoutParams(lp);
    }

    public static class ItemDecorator extends RecyclerView.ItemDecoration {

        /** Are we rendering in a locale whose script is left-to-right? */
        final int mOffsetPx;
        final int mRecyclerViewPadding;

        public ItemDecorator(int offsetPx, int paddingPx) {
            mOffsetPx = offsetPx;
            mRecyclerViewPadding = paddingPx;
        }

        @Override
        public void getItemOffsets(
                Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            boolean isLtr = parent.getLayoutDirection() == View.LAYOUT_DIRECTION_LTR;
            final int itemPosition = parent.getChildAdapterPosition(view);
            int startPos;
            int endPos;
            if (isLtr) {
                startPos = 0;
                endPos = state.getItemCount() - 1;
            } else {
                // We're in RTL land.
                startPos = state.getItemCount() - 1;
                endPos = 0;
            }
            if (itemPosition == startPos) {
                outRect.left = mRecyclerViewPadding;
                outRect.right = 0;
            } else if (itemPosition == endPos) {
                outRect.left = mOffsetPx;
                outRect.right = mRecyclerViewPadding;
            } else {
                outRect.left = mOffsetPx;
                outRect.right = 0;
            }
        }
    }
}
