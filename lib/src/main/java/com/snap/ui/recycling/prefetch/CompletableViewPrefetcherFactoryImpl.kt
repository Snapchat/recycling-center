package com.snap.ui.recycling.prefetch

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import io.reactivex.Scheduler

/**
 * Implementation for [CompletableViewPrefetcherFactory].
 */
class CompletableViewPrefetcherFactoryImpl(
    private val activity: Activity
) : CompletableViewPrefetcherFactory {

    override fun createCompletableViewPrefetcher(
        parentView: ViewGroup,
        scheduler: Scheduler
    ): CompletableViewPrefetcher {
        val inflater = LayoutInflater.from(activity)
        return CompletableViewPrefetcher(scheduler, inflater, parentView)
    }
}
