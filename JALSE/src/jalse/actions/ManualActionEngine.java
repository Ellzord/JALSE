package jalse.actions;

import static jalse.misc.JALSEExceptions.ENGINE_SHUTDOWN;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.listeners.EngineListener;
import jalse.listeners.ListenerSet;
import jalse.misc.AbstractIdentifiable;
import jalse.misc.Engine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a manually ticked {@link ActionEngine} implementation. This engine must be ticked using
 * {@link #tick()} so that {@link Action} can be performed via an external executor. Due to the
 * manual nature this engine operational state only switches between 2 states while active (
 * {@link Engine#PAUSED } and {@link Engine#IN_TICK}).
 *
 * @author Elliot Ford
 *
 */
public class ManualActionEngine implements ActionEngine {

    @SuppressWarnings("rawtypes")
    private class ScheduledWork extends AbstractIdentifiable implements Comparable<ScheduledWork> {

	private final Action action;
	private final Object actor;
	private long estimated;
	private final long period;

	private ScheduledWork(final UUID key, final Action action, final Object actor, final long initialDelay,
		final long period) {
	    super(key);
	    this.period = period;
	    this.action = action;
	    this.actor = actor;
	    estimated = System.nanoTime() + initialDelay;
	}

	@Override
	public int compareTo(final ScheduledWork o) {
	    return estimated < o.estimated ? -1 : estimated == o.estimated ? 0 : 1;
	}
    }

    private static final Logger logger = Logger.getLogger(ContinuousActionEngine.class.getName());

    private final DefaultTickInfo tick;
    private final Queue<ScheduledWork> work;
    private final ActionWithActor first;
    private final ActionWithActor last;
    private final ListenerSet<EngineListener> listeners;
    private volatile int state;
    private long lastStart;

    /**
     * Creates a new manual action engine instance.
     */
    public ManualActionEngine() {
	tick = new DefaultTickInfo(0);
	work = new PriorityQueue<>();
	first = new ActionWithActor();
	last = new ActionWithActor();
	listeners = new ListenerSet<>(EngineListener.class);
	state = PAUSED;
	lastStart = System.nanoTime();
    }

    @Override
    public boolean addEngineListener(final EngineListener listener) {
	return listeners.add(listener);
    }

    @Override
    public synchronized boolean cancel(final UUID action) {
	validateActive();
	return work.remove(new AbstractIdentifiable(Objects.requireNonNull(action)) {});
    }

    @Override
    public Set<? extends EngineListener> getEngineListeners() {
	return Collections.unmodifiableSet(listeners);
    }

    @Override
    public int getState() {
	return state;
    }

    @Override
    public TickInfo getTickInfo() {
	return tick;
    }

    @Override
    public synchronized boolean isActive(final UUID action) {
	return work.contains(new AbstractIdentifiable(Objects.requireNonNull(action)) {});
    }

    @Override
    public void pause() {}

    @Override
    public boolean removeEngineListener(final EngineListener listener) {
	return listeners.remove(listener);
    }

    @Override
    public synchronized <T> UUID scheduleAction(final Action<T> action, final T actor, final long initialDelay,
	    final long period, final TimeUnit unit) {
	if (initialDelay < 0 || period < 0) {
	    throw new IllegalArgumentException();
	}

	validateActive();

	final UUID key = UUID.randomUUID();

	work.add(new ScheduledWork(key, Objects.requireNonNull(action), Objects.requireNonNull(actor), unit
		.toNanos(initialDelay), unit.toNanos(period)));

	return key;
    }

    @Override
    public synchronized <T> void setFirstAction(final Action<T> action, final T actor) {
	validateActive();
	first.set(action, actor);
    }

    @Override
    public synchronized <T> void setLastAction(final Action<T> action, final T actor) {
	validateActive();
	last.set(action, actor);
    }

    @Override
    public synchronized void stop() {
	validateActive();
	work.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void tick() {
	validateActive();

	state = IN_TICK;
	final long start = System.nanoTime();
	tick.setDelta(start - lastStart);

	first.perform(tick);

	final Set<ScheduledWork> periodic = new HashSet<>();

	for (;;) {
	    final ScheduledWork w = work.peek();
	    if (w == null || w.estimated >= start) {
		break;
	    }

	    work.remove();

	    try {
		w.action.perform(w.actor, tick);
	    } catch (final Exception e) {
		logger.log(Level.WARNING, "Error performing action", e);
	    }

	    if (w.period > 0) {
		w.estimated = System.nanoTime() + w.period;
		periodic.add(w);
	    }
	}

	last.perform(tick);

	periodic.forEach(work::add);

	tick.incrementTicks();
	lastStart = start;
	state = PAUSED;
    }

    private void validateActive() {
	if (getState() == STOPPED) {
	    throwRE(ENGINE_SHUTDOWN);
	}
    }
}
