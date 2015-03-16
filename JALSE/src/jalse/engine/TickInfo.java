package jalse.engine;

import java.util.concurrent.TimeUnit;

/**
 * This represents the current {@link Engine} tick information. TickInfo uses nanoseconds as its
 * default time unit.
 *
 * @author Elliot Ford
 *
 * @see TimeUnit#NANOSECONDS
 *
 */
public interface TickInfo {

    /**
     * Gets the current ticks per second the engine is achieving.
     *
     * @return Current ticks per second.
     */
    int getCurrentTPS();

    /**
     * Gets the delta between the previous tick in nanoseconds.
     *
     * @return Time elapsed between the last tick.
     */
    long getDeltaAsNanos();

    /**
     * Gets the ticks per second interval in nanoseconds.
     *
     * @return TPS interval.
     */
    long getIntervalAsNanos();

    /**
     * Gets the current tick.
     *
     * @return Current tick.
     */
    int getTicks();

    /**
     * Gets ticks per second.
     *
     * @return Ideal ticks per second.
     */
    int getTPS();
}
