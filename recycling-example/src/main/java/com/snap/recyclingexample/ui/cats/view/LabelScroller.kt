package com.snap.recyclingexample.ui.cats.view

import com.snap.recyclingexample.ui.cats.CatPageEventDispatcher
import com.snap.recyclingexample.ui.cats.CatPageViewTypes
import com.snap.ui.recycling.factory.ViewFactory
import com.snap.ui.recycling.viewmodel.HorizontalScrollerModel

class LabelScroller(
        viewFactory: ViewFactory,
        eventDispatcher: CatPageEventDispatcher,
        models: List<LabelViewModel>
): HorizontalScrollerModel<LabelViewModel>(
        CatPageViewTypes.SCROLLER,
        viewFactory,
        eventDispatcher,
        models,
        0,
        120,
        0,
        0
)
