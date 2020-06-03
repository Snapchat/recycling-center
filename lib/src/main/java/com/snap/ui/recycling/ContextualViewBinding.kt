package com.snap.ui.recycling

import android.view.View
import com.snap.ui.recycling.adapter.ViewModelAdapter
import com.snap.ui.recycling.factory.ViewFactory
import com.snap.ui.recycling.viewmodel.AdapterViewModel

/**
 * Binds a View and a ViewModel in a [ViewModelAdapter] RecyclerView adapter.
 * Receives its [BindingContext] from the [com.snap.ui.recycling.factory.ViewFactory] that creates it.
 */
abstract class ContextualViewBinding<TBindingContext :
    BindingContext, TData : AdapterViewModel> : ViewBinding<TData>() {

    private lateinit var _bindingContext: TBindingContext
    private lateinit var _viewFactory: ViewFactory

    val bindingContext: TBindingContext
    get() = _bindingContext

    val viewFactory: ViewFactory
    get() = _viewFactory

    protected abstract fun onCreate(bindingContext: TBindingContext, itemView: View)

    fun create(bindingContext: TBindingContext, viewFactory: ViewFactory, itemView: View) {
        super.create(itemView)
        this._bindingContext = bindingContext
        this._viewFactory = viewFactory
        onCreate(bindingContext, itemView)
    }

    override fun create(itemView: View) {
        throw UnsupportedOperationException("must call create(bindingContext, itemView)")
    }

    override fun onCreate(itemView: View) {}
}
