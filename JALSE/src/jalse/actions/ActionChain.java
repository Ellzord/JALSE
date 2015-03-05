package jalse.actions;

import jalse.JALSE;
import jalse.entities.EntityContainer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A chain of {@link Action} that share the same actor and are performed one
 * after the other. An action chain should be used when it is required for
 * actions to only be run in a specific order (or in group). This is not
 * intended to replace first or last run actions but to provide an easy way to
 * group logically oriented actions.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type to perform on.
 *
 * @see JALSE#setFirstAction(Action)
 * @see JALSE#setLastAction(Action)
 */
public class ActionChain<T> implements Action<T> {

    /**
     * Convenience method for creating action chains with arrays or an
     * undetermined number of actions.
     *
     * @param actions
     *            Actions used to create the chain.
     * @return The newly created action chain.
     */
    @SafeVarargs
    public static <S extends EntityContainer> ActionChain<S> newChain(final Action<S>... actions) {

	return new ActionChain<>(Arrays.asList(actions));
    }

    private final List<Action<T>> chain;

    /**
     * Creates a new action chain with the specified actions.
     *
     * @param chain
     *            List of actions to use as the chain.
     */
    public ActionChain(final List<Action<T>> chain) {

	this.chain = Objects.requireNonNull(chain);
    }

    @Override
    public void perform(final T actor, final TickInfo tick) {

	for (final Action<T> action : chain) {

	    action.perform(actor, tick);
	}
    }

    /**
     * Gets the number of actions in the chain.
     *
     * @return Size of the chain.
     */
    public int size() {

	return chain.size();
    }
}
