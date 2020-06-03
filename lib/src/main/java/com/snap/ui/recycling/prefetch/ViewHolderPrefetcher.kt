package com.snap.ui.recycling.prefetch

import com.snap.ui.recycling.AdapterViewType
import com.snap.ui.recycling.factory.ViewModelViewHolder

/**
 * Client for retrieving [ViewModelViewHolder<*>] that may have been prefetched.
 */
interface ViewHolderPrefetcher {
    /**
     * Returns a [ViewModelViewHolder] instance for a given [AdapterViewType], if it has been prefetched.
     */
    fun getPrefetchedViewHolder(viewType: AdapterViewType): ViewModelViewHolder<*>?
}
