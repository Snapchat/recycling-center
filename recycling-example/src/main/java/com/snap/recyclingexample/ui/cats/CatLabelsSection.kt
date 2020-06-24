package com.snap.recyclingexample.ui.cats

import com.snap.recyclingexample.ui.cats.data.CatPageState
import com.snap.recyclingexample.ui.cats.view.LabelScroller
import com.snap.recyclingexample.ui.cats.view.LabelViewModel
import com.snap.ui.recycling.ObservableSectionController
import com.snap.ui.recycling.factory.ViewFactory
import com.snap.ui.recycling.viewmodel.AdapterViewModel
import com.snap.ui.seeking.Seekable
import com.snap.ui.seeking.Seekables
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicBoolean

class CatLabelsSection(
        private val viewFactory: ViewFactory,
        private val eventDispatcher: CatPageEventDispatcher,
        private val pageStateObservable: Observable<CatPageState>
) : ObservableSectionController {

    private val disposed = AtomicBoolean(false)

    override fun getViewModels(): Observable<out Seekable<AdapterViewModel>> {
        return pageStateObservable.map { pageState ->
            val labels = listOf(
                    LabelViewModel("Show Big Cats", pageState.showBigCats),
                    LabelViewModel("Show Medium Cats", pageState.showMediumCats),
                    LabelViewModel("Show Small Cats", pageState.showSmallCats)
            )
            Seekables.of(LabelScroller(viewFactory, eventDispatcher, labels))
        }
    }

    override fun dispose() {
        disposed.set(true)
    }

    override fun isDisposed(): Boolean {
        return disposed.get()
    }
}