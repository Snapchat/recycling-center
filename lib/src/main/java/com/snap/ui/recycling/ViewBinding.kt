package com.snap.ui.recycling

import android.view.View
import androidx.annotation.CallSuper
import com.snap.ui.event.EventDispatcher
import com.snap.ui.recycling.adapter.ViewModelAdapter
import com.snap.ui.recycling.viewmodel.AdapterViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Binds a View and a ViewModel in a [ViewModelAdapter] RecyclerView adapter.
 */
abstract class ViewBinding<TData : AdapterViewModel> {

    private lateinit var _itemView: View

    protected lateinit var _eventDispatcher: EventDispatcher

    protected var _model: TData? = null

    private val disposables = CompositeDisposable()

    protected val model: TData?
        get() = _model

    protected val isBound
        get() = _model != null

    val eventDispatcher: EventDispatcher
        get() = _eventDispatcher

    val itemView: View
        get() = _itemView

    protected abstract fun onCreate(itemView: View)
    protected abstract fun onBind(model: TData, previousModel: TData?)

    open fun create(itemView: View) {
        this._itemView = itemView
        onCreate(itemView)
    }

    open fun bind(model: TData, previousModel: TData?, eventDispatcher: EventDispatcher) {
        this._eventDispatcher = eventDispatcher
        this._model = model
        onBind(model, previousModel)
    }

    @CallSuper
    open fun onRecycle() {
        disposables.clear()
    }

    /**
     * See [androidx.recyclerview.widget.RecyclerView.Adapter.onFailedToRecycleView]
     */
    open fun onFailedToRecycleView(): Boolean {
        return false
    }

    /**
     * Tie the disposable to [ViewBinding.onRecycle] event. When onRecycle occurs,
     * [Disposable.dispose] will be called to dispose the work.
     * Note: subscribing is actually kind of expensive and should be avoided in a bind() if possible
     * @param disposable the [Disposable] that bounds to onRecycle
     */
    fun bindUntilRecycle(disposable: Disposable) {
        disposables.add(disposable)
    }
}
