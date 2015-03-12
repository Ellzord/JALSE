package jalse.actions;

import jalse.misc.Engine.TickInfo;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class DefaultTickInfo implements TickInfo {

    private final AtomicInteger currentTps;
    private final AtomicLong delta;
    private final long interval;
    private final AtomicInteger tick;
    private final int tps;

    DefaultTickInfo(final int tps) {
	this.tps = tps;
	interval = Math.round(TimeUnit.SECONDS.toNanos(1) / (double) tps);
	tick = new AtomicInteger();
	currentTps = new AtomicInteger();
	delta = new AtomicLong();
    }

    @Override
    public int getCurrentTPS() {
	return currentTps.get();
    }

    @Override
    public long getDelta() {
	return delta.get();
    }

    @Override
    public long getIntervalAsNanos() {
	return interval;
    }

    @Override
    public int getTick() {
	return tick.get();
    }

    @Override
    public int getTPS() {
	return tps;
    }

    public void incrementTicks() {
	tick.incrementAndGet();
    }

    public void setCurrentTPS(final int currentTps) {
	this.currentTps.set(currentTps);
    }

    public void setDelta(final long delta) {
	this.delta.set(delta);
    }
}
