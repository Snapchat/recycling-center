package com.snap.ui.recycling.adapter;

import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;

/**
 * Interface for {@link androidx.recyclerview.widget.RecyclerView.Adapter}
 * that supports {@link AdapterViewType} views.
 */
public interface ViewModelAdapter {
    AdapterViewType getItemAdapterViewType(int position);
    AdapterViewModel getItemViewModel(int position);
    int getItemCount();
}
