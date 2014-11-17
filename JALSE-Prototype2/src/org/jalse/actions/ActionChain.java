package org.jalse.actions;

import java.util.List;

import org.jalse.TickInfo;

public class ActionChain<T> implements Action<T> {

    private final List<Action<T>> chain;

    public ActionChain(final List<Action<T>> chain) {

	this.chain = chain;
    }

    @Override
    public void perform(final T actor, final TickInfo tick) {

	for (final Action<T> action : chain) {

	    action.perform(actor, tick);
	}
    }

    public int size() {

	return chain.size();
    }
}
