package jalse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This represents the current tick state of the engine.
 *
 * @author Elliot Ford
 *
 */
public final class TickInfo {

    private final AtomicInteger currentTps;
    private final AtomicLong delta;

    private final long interval;
    private final AtomicInteger ticks;
    private final int tps;

    /**
     * Creates a new instance of TickInfo.
     *
     * @param tps
     *            Ticks per second.
     */
    protected TickInfo(final int tps) {

	this.tps = tps;

	interval = Math.round(TimeUnit.SECONDS.toNanos(1) / (double) tps);

	ticks = new AtomicInteger();
	currentTps = new AtomicInteger();
	delta = new AtomicLong();
    }

    /**
     * Gets the current ticks per second the engine is achieving.
     *
     * @return Current ticks per second.
     */
    public int getCurrentTPS() {

	return currentTps.get();
    }

    /**
     * Gets the delta between the previous tick.
     *
     * @return Time elapsed between the last tick.
     */
    public long getDelta() {

	return delta.get();
    }

    /**
     * Gets the ticks per second interval in nanoseconds.
     *
     * @return TPS interval.
     */
    public long getIntervalAsNanos() {

	return interval;
    }

    /**
     * Gets ticks per second.
     *
     * @return Ideal ticks per second.
     */
    public int getTPS() {

	return tps;
    }

    /**
     * Increments the current total ticks of the engine.
     */
    protected void incrementTicks() {

	ticks.incrementAndGet();
    }

    /**
     * Sets the current ticks per second.
     *
     * @param currentTps
     *            Current ticks per second.
     */
    protected void setCurrentTPS(final int currentTps) {

	this.currentTps.set(currentTps);
    }

    /**
     * Sets the current delta between the previous tick.
     *
     * @param delta
     *            time elapsed between last tick.
     */
    protected void setDelta(final long delta) {

	this.delta.set(delta);
    }
}
