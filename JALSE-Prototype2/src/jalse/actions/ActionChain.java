package jalse.actions;

import jalse.TickInfo;

import java.util.Arrays;
import java.util.List;

public class ActionChain<T> implements Action<T> {

    @SafeVarargs
    public static <S> ActionChain<S> newChain(final Action<S>... actions) {

	return new ActionChain<>(Arrays.asList(actions));
    }

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
