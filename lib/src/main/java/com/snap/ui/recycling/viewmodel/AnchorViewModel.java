package com.snap.ui.recycling.viewmodel;

import com.snap.ui.recycling.AdapterViewType;

/**
 * ViewModel for an empty view that can be used to anchor the scrolling
 * of a RecyclerView.
 */
public class AnchorViewModel extends AdapterViewModel {

    public AnchorViewModel(AdapterViewType viewType) {
        super(viewType, 0);
    }
}
