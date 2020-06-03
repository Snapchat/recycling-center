package com.snap.ui.recycling.factory;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.SectionController;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;

/**
 * A {@link RecyclerView.ViewHolder} that binds an {@link AdapterViewModel} view model
 * to a View.
 */
public class ViewModelViewHolder<TModel extends AdapterViewModel> extends RecyclerView.ViewHolder {

    protected TModel mBoundViewModel;
    @Nullable protected SectionController mSectionController;
    private boolean mHasBeenBound;

    public ViewModelViewHolder(View itemView) {
        super(itemView);
    }

    @CallSuper
    public void bind(TModel viewModel, EventDispatcher eventDispatcher, @Nullable SectionController sectionController) {
        itemView.setContentDescription(viewModel.getContentDescription());

        mBoundViewModel = viewModel;
        mSectionController = sectionController;
        if (mSectionController != null) {
            mSectionController.onViewBound(itemView, mBoundViewModel);
        }
        mHasBeenBound = true;
    }

    @CallSuper
    public void recycle() {
        if (mSectionController != null) {
            mSectionController.onViewRecycled(itemView, mBoundViewModel);
            mSectionController = null;
        }
        mBoundViewModel = null;
    }

    /**
     * Called when {@link #itemView} cannot be recycled:
     * https://developer.android.com/reference/androidx.recyclerview.widget.RecyclerView.Adapter.html#onFailedToRecycleView(VH)
     */
    public boolean onFailedToRecycleView() {
        return false;
    }

    public AdapterViewModel getBoundViewModel() {
        return mBoundViewModel;
    }

    public boolean hasBeenBound() {
        return mHasBeenBound;
    }

    @Override
    public String toString() {
        return String.format("ViewModelViewHolder{%s %s %s}", itemView, mBoundViewModel, super.toString());
    }
}
