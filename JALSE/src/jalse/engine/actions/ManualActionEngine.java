package jalse.engine.actions;

import static jalse.engine.EngineState.IN_TICK;
import static jalse.engine.EngineState.PAUSED;
import static jalse.engine.EngineState.STOPPED;
import static jalse.misc.JALSEExceptions.ENGINE_SHUTDOWN;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.engine.AbstractEngineBindings;
import jalse.engine.AbstractTickInfo;
import jalse.engine.EngineBindings;
import jalse.engine.EngineState;
import jalse.engine.TickInfo;
import jalse.listeners.EngineListener;
import jalse.listeners.ListenerSet;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a manually ticked {@link ActionEngine} implementation. This engine must be ticked using
 * {@link #tick()} so that {@link Action} can be performed via an external executor. Due to the
 * manual nature this engine operational state only switches between 2 states while active (
 * {@link EngineState#PAUSED } and {@link EngineState#IN_TICK}).
 *
 * @author Elliot Ford
 *
 */
public final class ManualActionEngine implements ActionEngine {

    private class ManualEngineBindings extends AbstractEngineBindings {

	@Override
	protected void clearTickBindings() {
	    super.clearTickBindings();
	}
    }

    private class ManualTickInfo extends AbstractTickInfo {

	private ManualTickInfo() {
	    super(0);
	}

	@Override
	public void incrementTicks() {
	    super.incrementTicks();
	}

	@Override
	protected void setDeltaAsNanos(final long delta) {
	    super.setDeltaAsNanos(delta);
	}
    }

    private static final Logger logger = Logger.getLogger(ContinuousActionEngine.class.getName());

    private final ManualTickInfo tickInfo;
    private final ActionJobQueue jobQueue;
    private final ManualEngineBindings bindings;
    private final AtomicReference<ActionJob<?>> firstJob;
    private final AtomicReference<ActionJob<?>> lastJob;
    private final ListenerSet<EngineListener> listeners;
    private volatile EngineState state;
    private long lastStart;

    /**
     * Creates a new manual action engine instance.
     */
    public ManualActionEngine() {
	tickInfo = new ManualTickInfo();
	bindings = new ManualEngineBindings();
	jobQueue = new ActionJobQueue();
	firstJob = new AtomicReference<>();
	lastJob = new AtomicReference<>();
	listeners = new ListenerSet<>(EngineListener.class);
	state = EngineState.PAUSED;
	lastStart = System.nanoTime();
    }

    @Override
    public boolean addEngineListener(final EngineListener listener) {
	return listeners.add(listener);
    }

    @Override
    public synchronized boolean cancel(final UUID action) {
	validateNotStopped();
	return jobQueue.removeJob(action);
    }

    private <T> ActionJob<T> createJob(final Action<T> action, final T actor, final long initialDelay, final long period) {
	final ActionContext<T> context = new DefaultActionContext<>(this, UUID.randomUUID(), actor, period);
	return new ActionJob<>(action, context, initialDelay);
    }

    @Override
    public EngineBindings getBindings() {
	return bindings;
    }

    @Override
    public Set<? extends EngineListener> getEngineListeners() {
	return Collections.unmodifiableSet(listeners);
    }

    @Override
    public EngineState getState() {
	return state;
    }

    @Override
    public TickInfo getTickInfo() {
	return tickInfo;
    }

    @Override
    public synchronized boolean isActive(final UUID action) {
	return jobQueue.containsJob(action);
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
	validateNotStopped();
	if (initialDelay < 0 || period < 0) {
	    throw new IllegalArgumentException();
	}

	final ActionJob<T> job = createJob(action, actor, unit.toNanos(initialDelay), unit.toNanos(period));
	jobQueue.add(job);

	return job.getContext().getID();
    }

    @Override
    public synchronized <T> void setFirstAction(final Action<T> action, final T actor) {
	validateNotStopped();
	firstJob.set(createJob(action, actor, 0, tickInfo.getIntervalAsNanos()));
    }

    @Override
    public synchronized <T> void setLastAction(final Action<T> action, final T actor) {
	validateNotStopped();
	lastJob.set(createJob(action, actor, 0, tickInfo.getIntervalAsNanos()));
    }

    @Override
    public synchronized void stop() {
	validateNotStopped();
	jobQueue.clear();
    }

    @Override
    public synchronized void tick() {
	validateNotStopped();

	state = IN_TICK;

	final long start = System.nanoTime();
	tickInfo.setDeltaAsNanos(start - lastStart);
	lastStart = start;

	/*
	 * First first.
	 */
	final ActionJob<?> first = firstJob.get();
	if (first != null) {
	    first.run();
	}

	/*
	 * Work for this tick.
	 */
	for (final ActionJob<?> job : jobQueue.drainBeforeEstimated(start)) {
	    try {
		job.run();
	    } catch (final Exception e) {
		logger.log(Level.WARNING, "Error performing action", e);
	    }

	    final ActionContext<?> context = job.getContext();
	    if (context.isPeriodic()) {
		job.estimateForReschedule();
		jobQueue.add(job);
	    }
	}

	/*
	 * Last job.
	 */
	final ActionJob<?> last = lastJob.get();
	if (last != null) {
	    last.run();
	}

	bindings.clearTickBindings(); // Clear tick bindings.

	tickInfo.incrementTicks();

	state = PAUSED;
    }

    private void validateNotStopped() {
	if (state == STOPPED) {
	    throwRE(ENGINE_SHUTDOWN);
	}
    }
}
