package jalse.entities.methods;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import jalse.actions.Action;
import jalse.actions.ActionScheduler;
import jalse.entities.Entity;
import jalse.entities.annotations.ScheduleForActor;
import jalse.entities.functions.ScheduleForActorFunction;

/**
 * This is used for mapping calls to
 * {@link ActionScheduler#scheduleForActor(Action, long, long, TimeUnit)}.
 *
 * @author Elliot Ford
 *
 * @see ScheduleForActor
 * @see ScheduleForActorFunction
 *
 */
public class ScheduleForActorMethod implements EntityMethod {

    private final Constructor<?> constructor;
    private final long initialDelay;
    private final long period;
    private final TimeUnit unit;

    /**
     * Creates a schedule actor method.
     *
     * @param constructor
     *            Action constructor.
     *
     * @param initialDelay
     *            Initial delay before running action.
     * @param period
     *            Period repeat.
     * @param unit
     *            Time unit for action.
     */
    public ScheduleForActorMethod(final Constructor<?> constructor, final long initialDelay, final long period,
	    final TimeUnit unit) {
	this.constructor = Objects.requireNonNull(constructor);
	this.initialDelay = initialDelay;
	this.period = period;
	this.unit = Objects.requireNonNull(unit);

	if (initialDelay < 0L || period < 0L) {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Gets the action constructor
     *
     * @return Action constructor.
     */
    public Constructor<?> getConstructor() {
	return constructor;
    }

    /**
     * Gets the initial delay.
     *
     * @return Initial delay.
     */
    public long getInitialDelay() {
	return initialDelay;
    }

    /**
     * Gets the period.
     *
     * @return Period.
     */
    public long getPeriod() {
	return period;
    }

    /**
     * Gets the time unit.
     *
     * @return time unit.
     */
    public TimeUnit getUnit() {
	return unit;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	// Get new action.
	@SuppressWarnings("unchecked")
	final Action<Entity> action = (Action<Entity>) constructor.newInstance();
	// Schedule
	return entity.scheduleForActor(action, initialDelay, period, unit);
    }
}
