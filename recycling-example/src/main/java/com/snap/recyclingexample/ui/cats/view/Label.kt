package com.snap.recyclingexample.ui.cats.view

import android.view.View
import android.widget.TextView
import com.snap.recyclingexample.R
import com.snap.recyclingexample.ui.cats.CatPageViewTypes
import com.snap.recyclingexample.ui.cats.TappedLabelEvent
import com.snap.ui.recycling.ViewBinding
import com.snap.ui.recycling.viewmodel.AdapterViewModel

data class LabelViewModel(
        val title: String,
        val selected: Boolean
) : AdapterViewModel(CatPageViewTypes.LABEL, title.hashCode().toLong() /* sorry */) {

    override fun areContentsTheSame(model: AdapterViewModel): Boolean {
        return this == model
    }
}

class LabelViewBinding : ViewBinding<LabelViewModel>() {

    lateinit var titleView: TextView

    override fun onCreate(itemView: View) {
        titleView = itemView.findViewById(R.id.title)
        itemView.setOnClickListener { eventDispatcher.dispatch(TappedLabelEvent(this.model!!)) }
    }

    override fun onBind(model: LabelViewModel, previousModel: LabelViewModel?) {
        titleView.text = model.title
        itemView.isSelected = model.selected
    }
}