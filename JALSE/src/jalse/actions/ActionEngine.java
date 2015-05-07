package jalse.actions;

import static jalse.actions.Actions.unmodifiableActionContext;
import jalse.misc.JALSEExceptions;

/**
 * An engine for scheduling {@link Action} for execution. Work can be scheduled immediately or with
 * a given delay and can be periodic. This work can be tracked, cancelled and awaited.
 *
 * @author Elliot Ford
 *
 * @see Action#perform(ActionContext)
 * @see ForkJoinActionEngine
 * @see ThreadPoolActionEngine
 * @see ManualActionEngine
 *
 */
public interface ActionEngine {

    /**
     * Bindings for this engine.
     *
     * @return Engine bindings.
     */
    MutableActionBindings getBindings();

    /**
     * Whether the engine is paused.
     *
     * @return Paused state.
     */
    boolean isPaused();

    /**
     * Whether the engine is stopped.
     *
     * @return Stopped state.
     */
    boolean isStopped();

    /**
     * Creates an action context to define how the action will be scheduled for execution.
     *
     * @param action
     *            Action to create context for.
     * @return Context associated to the action.
     */
    <T> MutableActionContext<T> newContext(Action<T> action);

    /**
     * Pauses action processing.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     */
    void pause();

    /**
     * Puts a key-value pair in the engine bindings.
     *
     * @param key
     *            Key.
     * @param value
     *            Value.
     * @return Previously assigned value for the key.
     *
     * @see MutableActionBindings#put(String, Object)
     */
    default <T> T putInBindings(final String key, final T value) {
	return getBindings().put(key, value);
    }

    /**
     * Removes a key-value pair from the engine bindings.
     *
     * @param key
     *            Key.
     * @return The previously assigned value for this key.
     *
     * @see MutableActionBindings#remove(String)
     */
    default <T> T removeFromBindings(final String key) {
	return getBindings().remove(key);
    }

    /**
     * Resumes performing actions.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     */
    void resume();

    /**
     * Schedules an action for immediate execution.
     *
     * @param action
     *            Action to schedule.
     * @return Context associated to action (immutable).
     */
    default MutableActionContext<?> schedule(final Action<?> action) {
	return schedule(action, null);
    }

    /**
     * Schedules an action referencing an actor for immediate execution.
     *
     * @param action
     *            Action to schedule.
     * @param actor
     *            Actor to reference.
     * @return Context associated to action (immutable).
     */
    default <T> MutableActionContext<T> schedule(final Action<T> action, final T actor) {
	if (isStopped()) {
	    return Actions.emptyActionContext(); // Case of post cancel scheduling
	}

	final MutableActionContext<T> context = newContext(action);
	context.setActor(actor);
	context.schedule();

	return unmodifiableActionContext(context); // Don't allow for mutation (it's running)
    }

    /**
     * Permanently stops the engine. All work that has not yet been executed will be cancelled and
     * all work currently executing will may be interrupted.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     *
     */
    void stop();
}
