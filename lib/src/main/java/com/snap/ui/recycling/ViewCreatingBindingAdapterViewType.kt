package com.snap.ui.recycling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes

/**
 * A [AdapterViewType] that, for each adapter view,
 * provides a binding class in addition to an inflatable view.
 */

interface ViewCreatingBindingAdapterViewType : BindingAdapterViewType {

    /**
     * Returns a View of type [ViewType]
     */
    fun createView(parent: ViewGroup, inflater: LayoutInflater): View
}

/**
 * Helper method for [ViewCreatingBindingAdapterViewType] implementations that support both
 * a layout creator and layout inflation.
 */
fun createOrInflate(
    creator: ViewCreator?,
    @LayoutRes layoutId: Int,
    parent: ViewGroup,
    inflater: LayoutInflater
): View {
    return when {
        creator != null -> creator.invoke(parent, inflater)
        layoutId == 0 -> return FrameLayout(parent.context)
        else -> inflater.inflate(layoutId, parent, false)
    }
}

typealias ViewCreator = ((parent: ViewGroup, inflater: LayoutInflater) -> View)
