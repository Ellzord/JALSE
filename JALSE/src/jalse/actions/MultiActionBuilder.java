package jalse.actions;

import jalse.actions.MultiAction.MultiActionOperation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An {@link Action} builder for creating Actions that chain, schedule or await other actions. This
 * is useful for when some operations cannot be done out of sequence but the {@link ActionEngine} is
 * a concurrent one.<br>
 * <br>
 * Actions can be easily chained with {@link #buildChain(Action...)}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type of actor to be supplied (can be {@code ?} for no actor).
 *
 * @see ForkJoinActionEngine
 * @see ThreadPoolActionEngine
 */
public final class MultiActionBuilder<T> {

    /**
     * Builds an action that processes a chain of actions.
     *
     * @param actions
     *            Actions to perform.in sequence.
     * @return Chain action.
     */
    @SafeVarargs
    public static <S> Action<S> buildChain(final Action<S>... actions) {
	return buildChain(Arrays.asList(actions));
    }

    /**
     * Builds an action that processes a chain of actions.
     *
     * @param actions
     *            Actions to perform.in sequence.
     * @return Chain action.
     */
    public static <S> Action<S> buildChain(final List<? extends Action<S>> actions) {
	return newBuilder(actions).build();
    }

    /**
     * Creates a new builder instance.
     *
     * @param action
     *            Starting action.
     *
     * @return New builder.
     */
    public static <S> MultiActionBuilder<S> newBuilder(final Action<S> action) {
	return new MultiActionBuilder<S>().then(action);
    }

    /**
     * Creates a new builder instance.
     *
     * @param actions
     *            Starting actions.
     *
     * @return New builder.
     */
    public static <S> MultiActionBuilder<S> newBuilder(final List<? extends Action<S>> actions) {
	return new MultiActionBuilder<S>().then(actions);
    }

    private final MultiAction<T> multiAction;

    private MultiActionBuilder() {
	multiAction = new MultiAction<>();
    }

    /**
     * Builds the multi-action.
     *
     * @return The multi-action.
     */
    public Action<T> build() {
	if (!multiAction.hasOperations()) {
	    throw new IllegalStateException("No Actions have been added");
	}
	return multiAction;
    }

    /**
     * Builds and schedules the multi-action.
     *
     * @param engine
     *            Engine to schedule with.
     * @return Context for the action.
     */
    public MutableActionContext<?> buildAndSchedule(final ActionEngine engine) {
	return buildAndSchedule(engine, null);
    }

    /**
     * Builds and schedules the multi-action for a supplied actor.
     *
     * @param engine
     *            Engine to schedule with.
     *
     * @param actor
     *            Actor to reference.
     * @return Context for the action.
     */
    public MutableActionContext<T> buildAndSchedule(final ActionEngine engine, final T actor) {
	return engine.schedule(build(), actor);
    }

    /**
     * Adds an action to be performed next.
     *
     * @param action
     *            Action to perform next.
     * @return This builder.
     */
    public MultiActionBuilder<T> then(final Action<T> action) {
	multiAction.addOperation(action, MultiActionOperation.PERFORM);
	return this;
    }

    /**
     * Adds a chain of actions to be performed in sequence.
     *
     * @param actions
     *            Actions to perform.
     * @return This builder.
     */
    public MultiActionBuilder<T> then(final List<? extends Action<T>> actions) {
	multiAction.addOperation(actions, MultiActionOperation.PERFORM);
	return this;
    }

    /**
     * Adds an action to be scheduled.
     *
     * @param action
     *            Action to schedule.
     * @return This builder.
     */
    public MultiActionBuilder<T> thenSchedule(final Action<T> action) {
	multiAction.addOperation(action, MultiActionOperation.SCHEDULE);
	return this;
    }

    /**
     * Adds a number of actions to be scheduled.
     *
     * @param actions
     *            Actions to schedule.
     * @return This builder.
     */
    public MultiActionBuilder<T> thenSchedule(final Collection<? extends Action<T>> actions) {
	multiAction.addOperation(actions, MultiActionOperation.SCHEDULE);
	return this;
    }

    /**
     * Adds a number of actions to be scheduled then awaited.
     *
     * @param actions
     *            Actions to schedule and await.
     * @return This builder.
     */
    public MultiActionBuilder<T> thenScheduleAndAwait(final Collection<? extends Action<T>> actions) {
	multiAction.addOperation(actions, MultiActionOperation.SCHEDULE_AWAIT);
	return this;
    }
}
