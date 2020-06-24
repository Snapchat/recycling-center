package com.snap.recyclingexample.ui.cats

import com.snap.recyclingexample.ui.cats.data.CatPageStateProvider
import com.snap.ui.event.EventDispatcher

class CatPageEventDispatcher(
        private val pageStateProvider: CatPageStateProvider
) : EventDispatcher {

    override fun dispatch(event: Any) {
        val pageState = pageStateProvider.latestPageState
        when {
            event is TappedLabelEvent -> {
                if (event.model.title == "Show Big Cats") {
                    pageStateProvider.update(pageState.copy(showBigCats = !pageState.showBigCats))
                }
                if (event.model.title == "Show Medium Cats") {
                    pageStateProvider.update(pageState.copy(showMediumCats = !pageState.showMediumCats))
                }
                if (event.model.title == "Show Small Cats") {
                    pageStateProvider.update(pageState.copy(showSmallCats = !pageState.showSmallCats))
                }
            }
        }
    }
}