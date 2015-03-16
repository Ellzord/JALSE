package jalse.listeners;

import jalse.engine.Engine;
import jalse.engine.EngineState;
import jalse.engine.actions.ContinuousActionEngine;
import jalse.engine.actions.ManualActionEngine;

/**
 * Listener for {@link Engine} operational state changes ({@link EngineState}).
 *
 * @author Elliot Ford
 *
 * @see ContinuousActionEngine
 * @see ManualActionEngine
 *
 */
@FunctionalInterface
public interface EngineListener {

    /**
     * Triggered on engine operational state change.
     *
     * @param newState
     *            Current state.
     * @param oldState
     *            Previous state.
     */
    void stateChanged(EngineState newState, EngineState oldState);
}
