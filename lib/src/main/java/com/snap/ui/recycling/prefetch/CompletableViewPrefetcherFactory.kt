package com.snap.ui.recycling.prefetch

import android.view.ViewGroup
import io.reactivex.Scheduler

/**
 * Factory that provides a CompletableViewPrefetcher.
 */
interface CompletableViewPrefetcherFactory {

    /**
     * Creates a CompletableViewPrefetcher with specified parent view and scheduler.
     */
    fun createCompletableViewPrefetcher(
        parentView: ViewGroup,
        scheduler: Scheduler
    ): CompletableViewPrefetcher
}
