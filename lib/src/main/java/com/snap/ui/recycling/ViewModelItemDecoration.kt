package com.snap.ui.recycling

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.snap.ui.recycling.adapter.ViewModelAdapter
import com.snap.ui.recycling.viewmodel.AdapterViewModel

/**
 * A [RecyclerView.ItemDecoration] that uses a [AdapterViewModel] to provide item offsets.
 */
abstract class ViewModelItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as ViewModelAdapter
        val pos = parent.getChildAdapterPosition(view)
        getItemOffsets(outRect, view, adapter.getItemViewModel(pos), pos)
    }

    abstract fun getItemOffsets(outRect: Rect, view: View, viewModel: AdapterViewModel, position: Int)
}
