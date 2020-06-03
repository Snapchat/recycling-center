package com.snap.ui.event;

/**
 * Interface for dispatching a generic event to interested listeners.
 */
public interface EventDispatcher {
    void dispatch(Object event);
}

