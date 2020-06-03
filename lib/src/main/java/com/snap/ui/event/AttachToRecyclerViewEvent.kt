package com.snap.ui.event

import androidx.recyclerview.widget.RecyclerView

class AttachToRecyclerViewEvent(
    val eventType: EventType,
    val recyclerView: RecyclerView
) {
    enum class EventType {
        ATTACH, DETACH
    }
}
