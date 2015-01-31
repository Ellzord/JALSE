package jalse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class TickInfo {

    private final AtomicInteger currentTps;
    private final AtomicLong delta;

    private final long interval;
    private final AtomicInteger ticks;
    private final int tps;

    protected TickInfo(final int tps) {

	this.tps = tps;

	interval = Math.round(TimeUnit.SECONDS.toNanos(1) / (double) tps);

	ticks = new AtomicInteger();
	currentTps = new AtomicInteger();
	delta = new AtomicLong();
    }

    public int getCurrentTPS() {

	return currentTps.get();
    }

    public long getDelta() {

	return delta.get();
    }

    public long getIntervalAsNanos() {

	return interval;
    }

    public int getTPS() {

	return tps;
    }

    protected void incrementTicks() {

	ticks.incrementAndGet();
    }

    protected void setCurrentTPS(final int currentTps) {

	this.currentTps.set(currentTps);
    }

    protected void setDelta(final long delta) {

	this.delta.set(delta);
    }
}
