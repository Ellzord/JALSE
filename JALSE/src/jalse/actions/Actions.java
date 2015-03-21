package jalse.actions;

import static jalse.misc.JALSEExceptions.ENGINE_SHUTDOWN;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.misc.JALSEExceptions;

import java.util.concurrent.ExecutorService;

/**
 * A utility for {@link Action} related functionality.
 *
 * @author Elliot Ford
 *
 */
public final class Actions {

    /**
     * Creates an empty {@link ActionContext}.
     *
     * @return Empty context.
     */
    public static <T> MutableActionContext<T> emptyActionContext() {
	return new UnmodifiableDelegateActionContext<>(null);
    }

    /**
     * Validates the supplied service is not shutdown.
     *
     * @param executorService
     *            Service to check.
     * @return The service.
     * @throws IllegalArgumentException
     */
    public static <T extends ExecutorService> T requireNotShutdown(final T executorService) {
	if (executorService.isShutdown()) {
	    throw new IllegalArgumentException("ExecutorService is shutdown");
	}
	return executorService;
    }

    /**
     * Validates the supplied engine is not stopped.
     *
     * @param engine
     *            Engine to check.
     * @return The engine.
     *
     * @see JALSEExceptions#ENGINE_SHUTDOWN
     */
    public static ActionEngine requireNotStopped(final ActionEngine engine) {
	if (engine.isStopped()) {
	    throwRE(ENGINE_SHUTDOWN);
	}
	return engine;
    }

    /**
     * Creates an immutable {@ActionContext} that {@link ActionContext#cancel()} and
     * {@link MutableActionContext#await()} can still be called.
     *
     * @param context
     *            Context to wrap.
     * @return Immutable context.
     */
    public static <T> MutableActionContext<T> unmodifiableActionContext(final MutableActionContext<T> context) {
	return new UnmodifiableDelegateActionContext<>(context);
    }

    private Actions() {
	throw new UnsupportedOperationException();
    }
}
