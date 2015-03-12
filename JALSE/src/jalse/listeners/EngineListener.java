package jalse.listeners;

import jalse.actions.ContinuousActionEngine;
import jalse.actions.ManualActionEngine;
import jalse.misc.Engine;

/**
 * Listener for {@link Engine} operational state changes.<br>
 * <br>
 * States include:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@link Engine#PAUSED}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@link Engine#IN_TICK}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@link Engine#IN_WAIT}<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;{@link Engine#STOPPED}<br>
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
    void stateChanged(int newState, int oldState);
}
