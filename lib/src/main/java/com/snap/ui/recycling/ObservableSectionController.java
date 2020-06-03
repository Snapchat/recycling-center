package com.snap.ui.recycling;

import com.snap.ui.recycling.adapter.ObservableViewModelSectionAdapter;
import com.snap.ui.seeking.Seekable;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * A Section Controller to be use with {@link ObservableViewModelSectionAdapter}.
 * This controller is disposed along with the Adapter.
 */
public interface ObservableSectionController extends SectionController, Disposable {
    /**
     * Returns an Observable to get the list of view models for this section.
     */
    Observable<? extends Seekable<AdapterViewModel>> getViewModels();
}
