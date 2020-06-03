package com.snap.ui.recycling.adapter;

import androidx.annotation.MainThread;
import androidx.recyclerview.widget.RecyclerView;

import com.snap.ui.event.AttachToRecyclerViewEvent;
import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.ObservableSectionController;
import com.snap.ui.recycling.SectionController;
import com.snap.ui.recycling.adapter.errorhandling.AdapterErrorHandler;
import com.snap.ui.recycling.factory.ViewFactory;
import com.snap.ui.recycling.factory.ViewModelViewHolder;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;
import com.snap.ui.seeking.Seekable;
import com.snap.ui.seeking.Seekables;
import io.reactivex.Observable;

import java.util.List;

/**
 * A data adapter for section based recycler view. Note this class is not a subclass of
 * {@link androidx.recyclerview.widget.RecyclerView.Adapter}, but a wrapper. Users should not use the
 * stock adapter directly.
 */
public class SectionedRecyclerViewAdapter implements ViewModelAdapter {

    private final ViewModelSectionAdapterInternal adapter;

    public SectionedRecyclerViewAdapter(ViewFactory viewFactory, EventDispatcher eventDispatcher) {
        adapter = new ViewModelSectionAdapterInternal(viewFactory, eventDispatcher, null);
    }

    public SectionedRecyclerViewAdapter(
            ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            AdapterErrorHandler errorHandler) {
        adapter = new ViewModelSectionAdapterInternal(viewFactory, eventDispatcher, errorHandler);
    }

    public SectionedRecyclerViewAdapter(
            ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            AdapterErrorHandler errorHandler,
            List<ObservableSectionController> initialControllers) {
        adapter = new ViewModelSectionAdapterInternal(viewFactory, eventDispatcher, errorHandler, initialControllers);
    }

    public RecyclerView.Adapter<ViewModelViewHolder> getRecyclerViewAdapter() {
        return adapter;
    }

    public Observable<AttachToRecyclerViewEvent> observeAttachToRecyclerViewEvents() {
        return adapter.observeAttachToRecyclerViewEvents();
    }

    /**
     * Ideally ViewBinding should be under 1ms per RecyclerView entry. In practice this
     * can be difficult to achieve. Asychronous binding attempts to schedule initial bindings
     * on different main thread cycles, to try and preserve user interactivity and animations.
     */
    public void setAsyncBinding(boolean asyncBinding) {
        adapter.setAsyncBinding(asyncBinding);
    }

    /**
     * Declares the addition of a section. This method is useful when we want to set the order of sections before we
     * have data in each sections.
     */
    @MainThread
    public void addSection(SectionController section) {
        adapter.updateSection(section, Seekables.<AdapterViewModel>empty());
    }

    /**
     * Replaces a single section's views. Attempts to do an in-place update of the section if existing.
     */
    @MainThread
    public void updateSection(SectionController section, Seekable<AdapterViewModel> views) {
        adapter.updateSection(section, views);
    }

    /**
     * Deletes a single section and its views.
     */
    @MainThread
    public void removeSection(SectionController section) {
        adapter.removeSection(section);
    }

    public AdapterViewModel getItem(int position) {
        return adapter.getItemViewModel(position);
    }

    @Override
    public AdapterViewType getItemAdapterViewType(int position) {
        return adapter.getItemAdapterViewType(position);
    }

    @Override
    public AdapterViewModel getItemViewModel(int position) {
        return adapter.getItemViewModel(position);
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount();
    }
}
