package jalse.engine.actions;

import jalse.misc.AbstractIdentifiable;

import java.util.Objects;
import java.util.UUID;

/**
 * A simple yet fully-featured {@link ActionContext} implementation.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 */
public class DefaultActionContext<T> extends AbstractIdentifiable implements ActionContext<T> {

    private final ActionEngine engine;
    private final T actor;
    private final long period;

    /**
     * Create a new instance of DefaultActionContext with the supplied values.
     *
     * @param engine
     *            Engine the action is associated to.
     * @param id
     *            ID of the action.
     * @param actor
     *            Actor the action will be performed against.
     * @param period
     *            Recurring interval (should be 0 for run once actions).
     */
    public DefaultActionContext(final ActionEngine engine, final UUID id, final T actor, final long period) {
	super(id);
	this.engine = Objects.requireNonNull(engine);
	this.actor = Objects.requireNonNull(actor);
	this.period = period;
    }

    @Override
    public T getActor() {
	return actor;
    }

    @Override
    public ActionEngine getEngine() {
	return engine;
    }

    @Override
    public long getPeriod() {
	return period;
    }
}
