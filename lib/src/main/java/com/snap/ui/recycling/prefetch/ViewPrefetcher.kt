package com.snap.ui.recycling.prefetch

import android.view.View

/**
 * Interface to get a [View] that may have been prefetched
 */
interface ViewPrefetcher {
    /**
     * Returns a [View] for the given layout id, if such a view has been prefetched.
     */
    fun getPrefetchedView(layoutId: Int): View?
}
