package com.snap.ui.recycling

/**
 * A [AdapterViewType] that, for each adapter view,
 * provides a binding class in addition to an inflatable view.
 */

interface BindingAdapterViewType : AdapterViewType {
    /**
     * Binding class that is created alongside the inflated view.
     * May be null, in which case no binding code will be run for the inflated
     * [AdapterViewType].
     */
    val viewBindingClass: Class<out ViewBinding<*>>?
}
