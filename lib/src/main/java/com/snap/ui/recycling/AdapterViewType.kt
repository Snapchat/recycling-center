package com.snap.ui.recycling

import androidx.annotation.LayoutRes

import com.snap.ui.recycling.factory.ViewFactory

/**
 * Interface for descriptors of views that can be inflated and used by [ViewFactory].
 */
interface AdapterViewType {
    /**
     * A layout resource id to inflate for this ViewType. Pass [ViewFactory.DEFAULT_CONTAINER] for a basic
     * FrameLayout in place of an inflated view (with unspecified LayoutParameters)
     */
    @get:LayoutRes
    val layoutId: Int
}
