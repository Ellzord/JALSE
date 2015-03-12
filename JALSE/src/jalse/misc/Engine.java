package jalse.misc;

import jalse.listeners.EngineListener;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A abstract engine and tick state definition. This outlines the different operational states and
 * controls available for managing an engine. {@link TickInfo} defines tick state information for
 * each cycle. Once an engine has been stopped it cannot be used after.
 *
 * @author Elliot Ford
 *
 * @see #PAUSED
 * @see #IN_TICK
 * @see #IN_WAIT
 * @see #STOPPED
 */
public interface Engine {

    /**
     * This represents the current tick state of the engine.
     *
     * @author Elliot Ford
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
	 * Gets the delta between the previous tick.
	 *
	 * @return Time elapsed between the last tick.
	 */
	long getDelta();

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
	int getTick();

	/**
	 * Gets ticks per second.
	 *
	 * @return Ideal ticks per second.
	 */
	int getTPS();
    }

    /**
     * The engine is not ticking and has been shutdown.
     */
    int STOPPED = 0;

    /**
     * The engine is ready to be ticked.
     */
    int PAUSED = 1;

    /**
     * The engine is currently in tick (processing).
     */
    int IN_TICK = 2;

    /**
     * The engine is currently waiting.
     */
    int IN_WAIT = 3;

    /**
     * Adds an engine listener.
     *
     * @param listener
     *            Engine listener.
     * @return Whether the engine did not already contain this listener.
     */
    boolean addEngineListener(EngineListener listener);

    /**
     * Gets all the listeners for the engine.
     *
     * @return Engine listeners.
     */
    Set<? extends EngineListener> getEngineListeners();

    /**
     * Gets the current state of the engine.
     *
     * @return Current state.
     *
     */
    int getState();

    /**
     * Gets the current TickInfo.
     *
     * @return Current tick information.
     */
    TickInfo getTickInfo();

    /**
     * This is a convenience method for getting the tick interval converted to the supplied time
     * unit.
     *
     * @param unit
     *            Unit to convert to.
     * @return Tick interval as unit.
     *
     * @see TickInfo#getIntervalAsNanos()
     */
    default long getTickIntervalAs(final TimeUnit unit) {
	return unit.convert(getTickInfo().getIntervalAsNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Pauses the engine ticking.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     */
    void pause();

    /**
     * Removes an engine listener.
     *
     * @param listener
     *            Engine listener.
     * @return Whether the engine contained this listener.
     */
    boolean removeEngineListener(EngineListener listener);

    /**
     * Permanently stops the engine. All work that has not yet been executed will be cancelled and
     * all work currently executing will be given a timeout before interruption.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     *
     */
    void stop();

    /**
     * Starts ticking the engine.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     */
    void tick();
}