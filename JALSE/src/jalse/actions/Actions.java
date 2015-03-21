package jalse.actions;

import static jalse.misc.JALSEExceptions.ENGINE_SHUTDOWN;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.misc.JALSEExceptions;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A utility for {@link Action} related functionality.
 *
 * @author Elliot Ford
 *
 */
public final class Actions {

    /**
     * Copies context information to a target context (actor, bindings, initial delay & period).
     *
     * @param source
     *            Source context.
     * @param target
     *            Target context.
     */
    public static <T> void copy(final ActionContext<T> source, final MutableActionContext<T> target) {
	target.setActor(source.getOrNullActor());
	target.putAll(source.toMap());
	target.setInitialDelay(target.getInitialDelay(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
	target.setPeriod(source.getPeriod(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    /**
     * Creates an immutable empty {@link MutableActionBindings}.
     *
     * @return Empty bindings.
     */
    public static <T> MutableActionContext<T> emptyActionBindings() {
	return new UnmodifiableDelegateActionContext<>(null);
    }

    /**
     * Creates an immutable empty {@link MutableActionContext}.
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
     * Creates an immutable {MutableActionBindings}.
     *
     * @param bindings
     *            Bindings to wrap.
     * @return Immutable bindings.
     */
    public static MutableActionBindings unmodifiableActionBindings(final MutableActionBindings bindings) {
	return new UnmodifiableDelegateActionBindings(Objects.requireNonNull(bindings));
    }

    /**
     * Creates an immutable {@link MutableActionContext} that {@link ActionContext#cancel()} and
     * {@link MutableActionContext#await()} can still be called.
     *
     * @param context
     *            Context to wrap.
     * @return Immutable context.
     */
    public static <T> MutableActionContext<T> unmodifiableActionContext(final MutableActionContext<T> context) {
	return new UnmodifiableDelegateActionContext<>(Objects.requireNonNull(context));
    }

    private Actions() {
	throw new UnsupportedOperationException();
    }
}
