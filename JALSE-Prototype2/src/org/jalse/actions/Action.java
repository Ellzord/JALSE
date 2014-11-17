package org.jalse.actions;

import org.jalse.TickInfo;

@FunctionalInterface
public interface Action<T> {

    void perform(T actor, TickInfo tick);
}
