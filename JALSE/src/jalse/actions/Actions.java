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
     * An empty ActionEngine instance.
     */
    public static final ActionEngine EMPTY_ACTIONENGINE = new UnmodifiableDelegateActionEngine(null);

    /**
     * An empty MutableActionBindings instance.
     */
    public static final MutableActionBindings EMPTY_ACTIONBINDINGS = new UnmodifiableDelegateActionBindings(null);

    /**
     * An empty MutableActionContext instance.
     */
    @SuppressWarnings("rawtypes")
    public static final MutableActionContext EMPTY_ACTIONCONTEXT = new UnmodifiableDelegateActionContext<>(null);

    /**
     * Copies context information to a target context (actor, bindings, initial delay and period).
     *
     * @param source
     *            Source context.
     * @param target
     *            Target context.
     */
    public static <T> void copy(final ActionContext<T> source, final MutableActionContext<T> target) {
	target.setActor(source.getActor());
	target.putAll(source.toMap());
	target.setInitialDelay(target.getInitialDelay(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
	target.setPeriod(source.getPeriod(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    /**
     * Creates an immutable empty {@link MutableActionBindings}.
     *
     * @return Empty bindings.
     */
    public static MutableActionBindings emptyActionBindings() {
	return EMPTY_ACTIONBINDINGS;
    }

    /**
     * Creates an immutable empty {@link MutableActionContext}.
     *
     * @return Empty context.
     */
    @SuppressWarnings("unchecked")
    public static <T> MutableActionContext<T> emptyActionContext() {
	return EMPTY_ACTIONCONTEXT;
    }

    /**
     * Creates an immutable empty {@link ActionEngine}.
     *
     * @return Empty action engine.
     */
    public static ActionEngine emptyActionEngine() {
	return EMPTY_ACTIONENGINE;
    }

    /**
     * Validates the supplied service is not shutdown.
     *
     * @param executorService
     *            Service to check.
     * @return The service.
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
     * Creates an immutable {@link MutableActionBindings}.
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

    /**
     * Creates an immutable {@link ActionEngine}.
     *
     * @param engine
     *            engine to wrap.
     * @return Immutable bindings.
     */
    public static ActionEngine unmodifiableActionEngine(final ActionEngine engine) {
	return new UnmodifiableDelegateActionEngine(Objects.requireNonNull(engine));
    }

    private Actions() {
	throw new UnsupportedOperationException();
    }
}
