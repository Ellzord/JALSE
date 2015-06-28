package jalse.actions;

import jalse.actions.MultiAction.ActionOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An multi-{@link Action} for creating Actions that chain, schedule or await other actions. This is
 * useful for when some operations cannot be done out of sequence but the {@link ActionEngine} is a
 * concurrent one.<br>
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
 * @see ManualActionEngine
 */
public final class MultiAction<T> extends CopyOnWriteArrayList<ActionOperation<T>> implements Action<T> {

    /**
     * An {@link Action} operation to be executed by a {@link MultiAction}.
     *
     * @author Elliot Ford
     *
     * @param <T>
     *            Actor type.
     */
    public static final class ActionOperation<T> {

	private final Collection<? extends Action<T>> actions;
	private final OperationType type;

	/**
	 * Creates a new operation.
	 *
	 * @param type
	 *            Operation type.
	 * @param action
	 *            Action to execute.
	 */
	public ActionOperation(final OperationType type, final Action<T> action) {
	    this(type, Collections.singleton(Objects.requireNonNull(action)));
	}

	/**
	 * Creates a new operation.
	 *
	 * @param type
	 *            Operation type.
	 * @param actions
	 *            Actions to execute.
	 */
	public ActionOperation(final OperationType type, final Collection<? extends Action<T>> actions) {
	    this.type = Objects.requireNonNull(type);
	    if (actions.isEmpty()) {
		throw new IllegalArgumentException();
	    }
	    this.actions = actions;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (obj == this) {
		return true;
	    }

	    if (!(obj instanceof ActionOperation<?>)) {
		return false;
	    }

	    final ActionOperation<?> other = (ActionOperation<?>) obj;
	    return type == other.type && actions.equals(other.actions);
	}

	/**
	 * Gets the actions to execute.
	 *
	 * @return Actions to execute.
	 */
	public Collection<? extends Action<T>> getActions() {
	    return Collections.unmodifiableCollection(actions);
	}

	/**
	 * Gets the operation type.
	 *
	 * @return Operation type.
	 */
	public OperationType getType() {
	    return type;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + actions.hashCode();
	    result = prime * result + type.hashCode();
	    return result;
	}
    }

    /**
     * A {@link MultiAction} instance builder.
     *
     * @author Elliot Ford
     *
     * @param <T>
     *            Actor type.
     */
    public static final class Builder<T> {

	private final List<ActionOperation<T>> builderOperations;

	/**
	 * Creates a new builder instance.
	 */
	public Builder() {
	    builderOperations = new ArrayList<>();
	}

	/**
	 * Adds an action to be performed next.
	 *
	 * @param action
	 *            Action to perform next.
	 * @return This builder.
	 */
	public Builder<T> addPerform(final Action<T> action) {
	    builderOperations.add(new ActionOperation<>(OperationType.PERFORM, action));
	    return this;
	}

	/**
	 * Adds a chain of actions to be performed in sequence.
	 *
	 * @param actions
	 *            Actions to perform.
	 * @return This builder.
	 */
	public Builder<T> addPerform(final List<? extends Action<T>> actions) {
	    builderOperations.add(new ActionOperation<>(OperationType.PERFORM, actions));
	    return this;
	}

	/**
	 * Adds an action to be scheduled.
	 *
	 * @param action
	 *            Action to schedule.
	 * @return This builder.
	 */
	public Builder<T> addSchedule(final Action<T> action) {
	    builderOperations.add(new ActionOperation<>(OperationType.SCHEDULE, action));
	    return this;
	}

	/**
	 * Adds a number of actions to be scheduled.
	 *
	 * @param actions
	 *            Actions to schedule.
	 * @return This builder.
	 */
	public Builder<T> addSchedule(final Collection<? extends Action<T>> actions) {
	    builderOperations.add(new ActionOperation<>(OperationType.SCHEDULE, actions));
	    return this;
	}

	/**
	 * Adds an action to be scheduled then awaited.
	 *
	 * @param action
	 *            Action to schedule and await.
	 * @return This builder.
	 */
	public Builder<T> addScheduleAndAwait(final Action<T> action) {
	    builderOperations.add(new ActionOperation<>(OperationType.SCHEDULE_AWAIT, action));
	    return this;
	}

	/**
	 * Adds a number of actions to be scheduled then awaited.
	 *
	 * @param actions
	 *            Actions to schedule and await.
	 * @return This builder.
	 */
	public Builder<T> addScheduleAndAwait(final Collection<? extends Action<T>> actions) {
	    builderOperations.add(new ActionOperation<>(OperationType.SCHEDULE_AWAIT, actions));
	    return this;
	}

	/**
	 * Builds the multi-action.
	 *
	 * @return The multi-action.
	 */
	public MultiAction<T> build() {
	    final MultiAction<T> ma = new MultiAction<>();
	    ma.addAll(builderOperations);
	    return ma;
	}
    }

    /**
     * The {@link Action} operations this multi-action supports.
     *
     * @author Elliot Ford
     *
     */
    public enum OperationType {

	/**
	 * The {@link Action}(s) will be performed.
	 */
	PERFORM,

	/**
	 * The {@link Action}(s) will be scheduled.
	 */
	SCHEDULE,

	/**
	 * The {@link Action}(s) will be scheduled and then awaited.
	 */
	SCHEDULE_AWAIT
    }

    private static final long serialVersionUID = 4768157900074722640L;

    /**
     * Builds an action that processes a chain of actions.
     *
     * @param actions
     *            Actions to perform.in sequence.
     * @return Chain action.
     */
    @SafeVarargs
    public static <S> MultiAction<S> buildChain(final Action<S>... actions) {
	return buildChain(Arrays.asList(actions));
    }

    /**
     * Builds an action that processes a chain of actions.
     *
     * @param actions
     *            Actions to perform.in sequence.
     * @return Chain action.
     */
    public static <S> MultiAction<S> buildChain(final List<? extends Action<S>> actions) {
	return new Builder<S>().addPerform(actions).build();
    }

    @Override
    public void perform(final ActionContext<T> context) throws InterruptedException {
	for (final ActionOperation<T> aao : this) {
	    switch (aao.getType()) {
	    case PERFORM:
		performAll(context, aao.getActions());
		break;
	    case SCHEDULE:
		scheduleAll(context, aao.getActions());
		break;
	    case SCHEDULE_AWAIT:
		scheduleAwaitAll(context, aao.getActions());
		break;
	    }
	}
    }

    private void performAll(final ActionContext<T> context, final Collection<? extends Action<T>> actions)
	    throws InterruptedException {
	for (final Action<T> action : actions) {
	    action.perform(context); // Execute action
	}
    }

    private Collection<SchedulableActionContext<T>> scheduleAll(final ActionContext<T> context,
	    final Collection<? extends Action<T>> actions) {
	final Collection<SchedulableActionContext<T>> newContexts = new ArrayList<>();

	final ActionEngine engine = context.getEngine();
	final T actor = context.getActor();

	for (final Action<T> action : actions) {
	    final SchedulableActionContext<T> newContext = engine.newContext(action);
	    newContext.setActor(actor); // Same actor
	    newContext.putAll(context.toMap()); // Copy bindings (current)
	    newContext.schedule();

	    newContexts.add(newContext);
	}

	return newContexts;
    }

    private void scheduleAwaitAll(final ActionContext<T> context, final Collection<? extends Action<T>> actions)
	    throws InterruptedException {
	for (final SchedulableActionContext<T> newContext : scheduleAll(context, actions)) {
	    if (!newContext.isDone()) { // Stops us for waiting forever
		newContext.await();
	    }
	}
    }
}
