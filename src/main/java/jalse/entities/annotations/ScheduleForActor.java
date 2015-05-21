package jalse.entities.annotations;

import jalse.actions.Action;
import jalse.actions.ActionScheduler;
import jalse.entities.Entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An {@link Entity} type annotation for:
 * <ul>
 * <li>{@link ActionScheduler#scheduleForActor(Action)}</li>
 * <li>{@link ActionScheduler#scheduleForActor(Action, long, TimeUnit)}</li>
 * <li>{@link ActionScheduler#scheduleForActor(Action, long, long, TimeUnit)}</li>
 * </ul>
 * See {@link ScheduleForActorFunction} for acceptable method signatures.
 *
 * @author Elliot Ford
 *
 * @see ScheduleForActorMethod
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduleForActor {

    /**
     * Default initial delay ({@code 0L}).
     *
     * @see #initialDelay()
     */
    public static final long DEFAULT_INITIAL_DELAY = 0L;

    /**
     * Default repeat period ({@code 0L}).
     *
     * @see #period()
     */
    public static final long DEFAULT_PERIOD = 0L;

    /**
     * Default time unit (nanoseconds).
     *
     * @see #unit()
     */
    public static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.NANOSECONDS;

    /**
     * Action to schedule.
     *
     * @return Action type.
     */
    Class<? extends Action<Entity>> action();

    /**
     * Initial delay before executing.
     *
     * @return Initial delay.
     */
    long initialDelay() default DEFAULT_INITIAL_DELAY;

    /**
     * Repeat period.
     *
     * @return Period.
     */
    long period() default DEFAULT_PERIOD;

    /**
     * Time unit for scheduling information.
     *
     * @return Time unit.
     */
    TimeUnit unit() default TimeUnit.NANOSECONDS;
}
