package com.snap.ui.recycling.viewmodel;

import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.HorizontalRecyclerViewBinding;
import com.snap.ui.recycling.factory.ViewFactory;

import java.util.List;

/**
 * A ViewModel for a {@link HorizontalRecyclerViewBinding}. Since this View itself presents a
 * list of Views, this ViewModel holds a List of {@link AdapterViewModel}s.
 */

public class HorizontalScrollerModel<D extends AdapterViewModel> extends ListViewModel<D> {

    private final ViewFactory mViewFactory;
    private final EventDispatcher mEventDispatcher;
    private final int mScrollerHeight;
    private final int mPadding;
    private final int mOffset;

    public HorizontalScrollerModel(
            AdapterViewType scrollerViewType,
            ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            List<D> viewModels,
            long id,
            int scrollerHeight,
            int padding,
            int offset) {
        super(viewModels, scrollerViewType, id);
        mViewFactory = viewFactory;
        mScrollerHeight = scrollerHeight;
        mEventDispatcher = eventDispatcher;
        mPadding = padding;
        mOffset = offset;
    }

    public ViewFactory getViewFactory() {
        return mViewFactory;
    }

    public EventDispatcher getEventDispatcher() {
        return mEventDispatcher;
    }

    public int getScrollerHeight() {
        return mScrollerHeight;
    }

    public int getPadding() {
        return mPadding;
    }

    public int getOffset() {
        return mOffset;
    }
}
