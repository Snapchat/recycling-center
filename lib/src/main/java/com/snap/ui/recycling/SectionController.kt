package com.snap.ui.recycling

import android.view.View
import com.snap.ui.recycling.viewmodel.AdapterViewModel

interface SectionController {
    fun onViewBound(itemView: View, model: AdapterViewModel)
    fun onViewRecycled(itemView: View, viewModel: AdapterViewModel)
}
