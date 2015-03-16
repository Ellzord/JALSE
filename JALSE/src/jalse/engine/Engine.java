package jalse.engine;

import jalse.listeners.EngineListener;
import jalse.misc.JALSEExceptions;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A abstract engine and tick state definition. This outlines the different operational states and
 * controls available for managing an engine. Once an engine has been stopped it cannot be used
 * after.
 *
 * @author Elliot Ford
 *
 * @see EngineState
 */
public interface Engine {

    /**
     * Adds an engine listener.
     *
     * @param listener
     *            Engine listener.
     * @return Whether the engine did not already contain this listener.
     */
    boolean addEngineListener(EngineListener listener);

    /**
     * Bindings for this engine.
     *
     * @return Engine bindings.
     */
    EngineBindings getBindings();

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
    EngineState getState();

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
     * Puts a key-value pair in the engine bindings.
     *
     * @param key
     *            Key.
     * @param value
     *            Value.
     * @return Previously assigned value for the key.
     *
     * @see EngineBindings#put(String, Object)
     */
    default <T> T putInBindings(final String key, final T value) {
	return getBindings().put(key, value);
    }

    /**
     * Removes an engine listener.
     *
     * @param listener
     *            Engine listener.
     * @return Whether the engine contained this listener.
     */
    boolean removeEngineListener(EngineListener listener);

    /**
     * Removes a key-value pair from the engine bindings.
     *
     * @param key
     *            Key.
     * @return The previously assigned value for this key.
     *
     * @see EngineBindings#remove(String)
     */
    default <T> T removeFromBindings(final String key) {
	return getBindings().remove(key);
    }

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