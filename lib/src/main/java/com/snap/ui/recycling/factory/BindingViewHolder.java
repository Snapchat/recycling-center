package com.snap.ui.recycling.factory;

import androidx.annotation.Nullable;
import android.view.View;

import java.util.Locale;

import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.SectionController;
import com.snap.ui.recycling.ViewBinding;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;

public class BindingViewHolder<T extends AdapterViewModel, D extends SectionController> extends ViewModelViewHolder<T> {
    private final ViewBinding<T> mItemBinding;

    BindingViewHolder(View itemView, ViewBinding<T> binding) {
        super(itemView);
        mItemBinding = binding;
    }

    @Override
    public void bind(T viewModel, EventDispatcher eventDispatcher, @Nullable SectionController sectionController) {
        try {
            mItemBinding.bind(viewModel, mBoundViewModel, eventDispatcher);
        } catch (RuntimeException e) {
            String error = String.format(Locale.US,
                    "Failed to onBind view of type %s",
                    mItemBinding.getClass().getSimpleName());
            throw new RecyclingCenterBindingException(error, e, viewModel.getDebugInfo());
        }

        super.bind(viewModel, eventDispatcher, sectionController);
    }

    @Override
    public void recycle() {
        mItemBinding.onRecycle();
        super.recycle();
    }

    @Override
    public boolean onFailedToRecycleView() {
        return mItemBinding.onFailedToRecycleView();
    }

    @Override
    public String toString() {
        return String.format("BindingViewHolder{%s %s %s}", itemView,
                getBoundViewModel(), super.toString());
    }

    public static class RecyclingCenterBindingException extends RuntimeException {
        public final @Nullable String debugInfo;

        public RecyclingCenterBindingException(String errorMsg, Throwable throwable, String debugInfo) {
            super(errorMsg, throwable);

            this.debugInfo = debugInfo;
        }
    }
}
