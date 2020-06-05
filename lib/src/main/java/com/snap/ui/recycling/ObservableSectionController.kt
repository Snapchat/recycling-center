package com.snap.ui.recycling

import com.snap.ui.recycling.viewmodel.AdapterViewModel
import com.snap.ui.seeking.Seekable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * A Section Controller to be use with {@link ObservableViewModelSectionAdapter}.
 * This controller is disposed along with the Adapter.
 */
interface ObservableSectionController : SectionController, Disposable {
    /**
     * Returns an Observable to get the list of view models for this section.
     */
    fun getViewModels(): Observable<out Seekable<AdapterViewModel>>

    fun getSectionName(): String = this.javaClass.name
}
