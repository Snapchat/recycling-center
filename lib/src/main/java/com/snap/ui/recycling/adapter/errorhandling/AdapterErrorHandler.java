package com.snap.ui.recycling.adapter.errorhandling;

import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.factory.ViewModelViewHolder;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;

import android.content.Context;
import androidx.annotation.Nullable;

public interface AdapterErrorHandler {
    void onError(Exception e, @Nullable AdapterErrorInfo adapterErrorInfo);
    int getErrorViewType();
    AdapterViewType getErrorAdapterViewType();
    AdapterViewModel getErrorAdapterViewModel();
    ViewModelViewHolder getViewModelViewHolder(Context context);
    long getItemId();
}
