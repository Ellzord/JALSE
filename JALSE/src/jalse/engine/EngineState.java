package jalse.engine;

/**
 * This represents current {@link Engine} operational state.
 *
 * @author Elliot Ford
 *
 */
public enum EngineState {

    /**
     * The engine is not ticking and has been shutdown.
     */
    STOPPED,

    /**
     * The engine is ready to be ticked.
     */
    PAUSED,

    /**
     * The engine is currently in tick (processing).
     */
    IN_TICK,

    /**
     * The engine is currently waiting.
     */
    IN_WAIT
}
