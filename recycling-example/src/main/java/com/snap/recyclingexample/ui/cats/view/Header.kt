package com.snap.recyclingexample.ui.cats.view

import android.view.View
import android.widget.TextView
import com.snap.recyclingexample.R
import com.snap.recyclingexample.ui.cats.CatPageViewTypes
import com.snap.ui.recycling.ViewBinding
import com.snap.ui.recycling.viewmodel.AdapterViewModel

data class HeaderViewModel(
        val title: String
) : AdapterViewModel(CatPageViewTypes.TITLE, title.hashCode().toLong() /* sorry */) {

    override fun areContentsTheSame(model: AdapterViewModel): Boolean {
        return this == model
    }
}

class HeaderViewBinding : ViewBinding<HeaderViewModel>() {

    lateinit var titleView: TextView

    override fun onCreate(itemView: View) {
        titleView = itemView.findViewById(R.id.title)
    }

    override fun onBind(model: HeaderViewModel, previousModel: HeaderViewModel?) {
        titleView.text = model.title
    }
}