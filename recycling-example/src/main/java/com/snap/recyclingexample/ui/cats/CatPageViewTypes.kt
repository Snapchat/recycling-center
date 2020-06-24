package com.snap.recyclingexample.ui.cats

import androidx.annotation.LayoutRes
import com.snap.recyclingexample.R
import com.snap.recyclingexample.ui.cats.view.CatViewBinding
import com.snap.recyclingexample.ui.cats.view.LabelViewBinding
import com.snap.recyclingexample.ui.cats.view.HeaderViewBinding
import com.snap.ui.recycling.BindingAdapterViewType
import com.snap.ui.recycling.HorizontalRecyclerViewBinding
import com.snap.ui.recycling.ViewBinding

enum class CatPageViewTypes(
        @LayoutRes override val layoutId: Int,
        override val viewBindingClass: Class<out ViewBinding<*>>? = null
) : BindingAdapterViewType {

    CAT(R.layout.cat, CatViewBinding::class.java),
    TITLE(R.layout.section_header, HeaderViewBinding::class.java),
    LABEL(R.layout.label, LabelViewBinding::class.java),
    SCROLLER(HorizontalRecyclerViewBinding.LAYOUT, HorizontalRecyclerViewBinding::class.java)
}