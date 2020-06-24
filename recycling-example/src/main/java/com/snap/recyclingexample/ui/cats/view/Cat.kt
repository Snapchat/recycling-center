package com.snap.recyclingexample.ui.cats.view

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import coil.api.load
import com.snap.recyclingexample.R
import com.snap.recyclingexample.ui.cats.CatPageViewTypes
import com.snap.recyclingexample.ui.cats.data.CatData
import com.snap.ui.recycling.ViewBinding
import com.snap.ui.recycling.viewmodel.AdapterViewModel


data class CatViewModel(
        private val data: CatData
) : AdapterViewModel(CatPageViewTypes.CAT, data.id) {
    override fun areContentsTheSame(model: AdapterViewModel): Boolean {
        return this == model
    }

    val imageUri: Uri get() = data.imageUri
    val caption: String get() = data.name
}

class CatViewBinding : ViewBinding<CatViewModel>() {

    lateinit var image: ImageView
    lateinit var caption: TextView

    override fun onCreate(itemView: View) {
        image = itemView.findViewById(R.id.image)
        caption = itemView.findViewById(R.id.caption)
    }

    override fun onBind(model: CatViewModel, previousModel: CatViewModel?) {
        image.load(model.imageUri)
        caption.text = model.caption
    }
}