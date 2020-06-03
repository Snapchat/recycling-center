package com.snap.ui.recycling.viewmodel;

import java.util.List;

import com.google.common.base.Objects;
import com.snap.ui.recycling.AdapterViewType;

/**
 * A ViewModel that is a list of other view models. The primary difference between a
 * <code>ListViewModel</code> and a <code>List&lt;ViewModel&gt;</code> is that a
 * <code>ListViewModel</code> has its own view type, such as a horizontal scroller.
 *
 * See {@link com.snapchat.android.app.feature.search.ui.view.common.HorizontalRecyclerView}
 */
public class ListViewModel <D extends AdapterViewModel> extends AdapterViewModel {

    private final List<D> mViewModels;

    public ListViewModel(List<D> viewModels, AdapterViewType viewType, long id) {
        super(viewType, id);
        mViewModels = viewModels;
    }

    public ListViewModel(List<D> viewModels, AdapterViewType viewType) {
        super(viewType);
        mViewModels = viewModels;
    }

    public List<D> getModels() {
        return mViewModels;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ListViewModel)) {
            return false;
        }

        ListViewModel<?> other = (ListViewModel<?>)obj;
        return mAdapterViewType == other.mAdapterViewType && mViewModels.equals(other.mViewModels);
    }


    @Override
    public boolean areContentsTheSame(AdapterViewModel model) {
        if (!super.areContentsTheSame(model)) {
            return false;
        }

        if (!(model instanceof ListViewModel)) {
            return false;
        }

        ListViewModel<?> other = (ListViewModel<?>)model;
        return mViewModels.equals(other.mViewModels);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mViewModels, mAdapterViewType);
    }

    @Override
    public String toString() {
        return String.format("ListViewModel{id=%s viewType=%s models=%s}", mId, mAdapterViewType, mViewModels);
    }
}