package jalse.actions;

import static jalse.actions.Actions.unmodifiableActionContext;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
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
	engine = ForkJoinActionEngine.commonPoolEngine();
	contexts = Collections.newSetFromMap(new WeakHashMap<>());
    }

    /**
     * Cancel all tasks scheduled to the current engine for the actor by this scheduler.
     */
    @Override
    public void cancelAllScheduledForActor() {
	synchronized (contexts) {
	    contexts.forEach(c -> c.cancel());
	    contexts.clear();
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
	    return Actions.emptyActionContext();
	}

	final MutableActionContext<T> context;
	synchronized (contexts) {
	    context = engine.createContext(action);
	    context.setActor(actor);
	    context.setInitialDelay(initialDelay, unit);
	    context.setPeriod(period, unit);

	    contexts.add(context);
	}

	context.schedule();

	return unmodifiableActionContext(context);
    }

    /**
     * Associates a engine to this scheduler (if the engine changes all task references are lost).
     *
     * @param engine
     *            Engine to schedule actions against.
     */
    public void setEngine(final ActionEngine engine) {
	if (!Objects.equals(this.engine, engine)) {
	    synchronized (contexts) {
		contexts.clear();
	    }
	}
	this.engine = engine;
    }
}
