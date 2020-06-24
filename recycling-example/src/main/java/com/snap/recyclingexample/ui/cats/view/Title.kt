package com.snap.recyclingexample.ui.cats.view

import android.view.View
import android.widget.TextView
import com.snap.recyclingexample.R
import com.snap.recyclingexample.ui.cats.CatPageViewTypes
import com.snap.ui.recycling.ViewBinding
import com.snap.ui.recycling.viewmodel.AdapterViewModel

data class TitleViewModel(
        val title: String
) : AdapterViewModel(CatPageViewTypes.TITLE, title.hashCode().toLong() /* sorry */) {

    override fun areContentsTheSame(model: AdapterViewModel): Boolean {
        return this == model
    }
}

class TitleViewBinding : ViewBinding<TitleViewModel>() {

    lateinit var titleView: TextView

    override fun onCreate(itemView: View) {
        titleView = itemView.findViewById(R.id.title)
    }

    override fun onBind(model: TitleViewModel, previousModel: TitleViewModel?) {
        titleView.text = model.title
    }
}