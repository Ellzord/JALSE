package jalse.engine;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is an abstract implementation of {@link TickInfo}. AbstractTickInfo provides {@code public}
 * scope get methods but {@code protected} scope set methods. To manipulate the tick information
 * this class should be inherited.
 *
 * @author Elliot Ford
 *
 */
public abstract class AbstractTickInfo implements TickInfo {

    private final int tps;
    private final long interval;
    private final AtomicInteger currentTps;
    private final AtomicLong delta;
    private final AtomicInteger ticks;

    /**
     * Creates an instance of AbstractTickInfo with the supplied ticks per second.
     *
     * @param tps
     *            Ticks per second.
     */
    protected AbstractTickInfo(final int tps) {
	this.tps = tps;
	interval = Math.round(TimeUnit.SECONDS.toNanos(1) / (double) tps);
	ticks = new AtomicInteger();
	currentTps = new AtomicInteger();
	delta = new AtomicLong();
    }

    @Override
    public int getCurrentTPS() {
	return currentTps.get();
    }

    @Override
    public long getDeltaAsNanos() {
	return delta.get();
    }

    @Override
    public long getIntervalAsNanos() {
	return interval;
    }

    @Override
    public int getTicks() {
	return ticks.get();
    }

    @Override
    public int getTPS() {
	return tps;
    }

    /**
     * Increments the current total ticks.
     */
    protected void incrementTicks() {
	ticks.incrementAndGet();
    }

    /**
     * Sets the currently achieved ticks per second.
     *
     * @param currentTps
     *            Current ticks per second to set.
     */
    protected void setCurrentTPS(final int currentTps) {
	this.currentTps.set(currentTps);
    }

    /**
     * Sets the current tick delta in nanoseconds.
     *
     * @param delta
     *            Current tick delta.
     */
    protected void setDeltaAsNanos(final long delta) {
	this.delta.set(delta);
    }
}
