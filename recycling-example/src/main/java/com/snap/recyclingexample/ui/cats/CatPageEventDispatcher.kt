package com.snap.recyclingexample.ui.cats

import com.snap.recyclingexample.ui.cats.data.CatPageStateProvider
import com.snap.ui.event.EventDispatcher

/**
 * There are a few different styles of [EventDispatcher] available in Recycling Center.
 * [SimpleBus] provides an implementation backed by EventBus for simple, reflection-based dispatching.
 * [RxEventDispatcher] binds a mapping from event to a dedicated handler, powered by RxJava.
 */
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
                if (event.model.title == "Show Striped Cats") {
                    pageStateProvider.update(pageState.copy(showStripedCats = !pageState.showStripedCats))
                }
                if (event.model.title == "Show Fluffy Cats") {
                    pageStateProvider.update(pageState.copy(showFluffyCats = !pageState.showFluffyCats))
                }
            }
        }
    }
}