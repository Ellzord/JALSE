package jalse.actions;

import static jalse.actions.Actions.unmodifiableActionContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ActionScheduler} implementation that schedules all actions against the supplied actor.
 * Weak references are kept against all scheduled tasks so they can be bulk cancelled (these are
 * also cleared on {@link ActionEngine} change).<br>
 * <br>
 * By default if no {@link ActionEngine} is supplied {@link ForkJoinActionEngine#commonPoolEngine()}
 * will be used.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public class DefaultActionScheduler<T> implements ActionScheduler<T> {

    private final T actor;
    private ActionEngine engine;
    private final Set<ActionContext<T>> contexts;

    /**
     * Creates a DefaultScheduler for the supplied actor.
     *
     * @param actor
     *            Actor to schedule actions against.
     */
    public DefaultActionScheduler(final T actor) {
	this.actor = Objects.requireNonNull(actor);
	engine = ForkJoinActionEngine.commonPoolEngine(); // Defaults use common engine
	contexts = new HashSet<>();
    }

    /**
     * Cancel all tasks scheduled to the current engine for the actor by this scheduler.
     */
    @Override
    public void cancelAllScheduledForActor() {
	synchronized (contexts) {
	    final Iterator<ActionContext<T>> it = contexts.iterator();
	    while (it.hasNext()) {
		final ActionContext<T> cxt = it.next();
		if (!cxt.isDone()) {
		    cxt.cancel();
		}
		it.remove();
	    }
	}
    }

    /**
     * Gets the action Actor.
     *
     * @return Actor to schedule events against.
     */
    public T getActor() {
	return actor;
    }

    /**
     * Gets the associated engine.
     *
     * @return Associated engine or null if it has not been set.
     */
    public ActionEngine getEngine() {
	return engine;
    }

    @Override
    public MutableActionContext<T> scheduleForActor(final Action<T> action, final long initialDelay, final long period,
	    final TimeUnit unit) {
	if (engine.isStopped()) {
	    return Actions.emptyActionContext(); // Case of post cancel scheduling
	}

	final MutableActionContext<T> context = engine.newContext(action);
	context.setActor(actor);
	context.setInitialDelay(initialDelay, unit);
	context.setPeriod(period, unit);
	context.schedule();

	synchronized (contexts) {
	    contexts.add(context);
	    contexts.removeIf(ActionContext<T>::isDone);
	}

	return unmodifiableActionContext(context); // Don't allow for mutation (it's running)
    }

    /**
     * Associates a engine to this scheduler (if the engine changes all task references are lost).
     *
     * @param engine
     *            Engine to schedule actions against.
     */
    public void setEngine(final ActionEngine engine) {
	if (!Objects.equals(this.engine, engine)) { // Only if changed
	    synchronized (contexts) {
		contexts.clear();
	    }
	}
	this.engine = engine;
    }
}
