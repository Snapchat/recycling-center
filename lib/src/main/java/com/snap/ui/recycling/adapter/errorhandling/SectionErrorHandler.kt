package com.snap.ui.recycling.adapter.errorhandling

/**
 * Handler errors for loading sections in recylcerView
 */
interface SectionErrorHandler {
    fun <T> onError(sectionClass: Class<T>, e: Throwable)
}
