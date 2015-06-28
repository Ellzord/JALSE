package jalse.actions;

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
     * An empty ActionBindings instance.
     */
    public static final ActionBindings EMPTY_ACTIONBINDINGS = new UnmodifiableDelegateActionBindings(null);

    /**
     * An empty SchedulableActionContext instance.
     */
    @SuppressWarnings("rawtypes")
    public static final SchedulableActionContext EMPTY_ACTIONCONTEXT = new UnschedulableDelegateActionContext<>(null);

    /**
     * Copies context information to a target context (actor, bindings, initial delay and period).
     *
     * @param source
     *            Source context.
     * @param target
     *            Target context.
     */
    public static <T> void copy(final ActionContext<T> source, final SchedulableActionContext<T> target) {
	target.setActor(source.getActor());
	target.putAll(source.toMap());
	target.setInitialDelay(target.getInitialDelay(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
	target.setPeriod(source.getPeriod(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    /**
     * Creates an immutable empty {@link ActionBindings}.
     *
     * @return Empty bindings.
     */
    public static ActionBindings emptyActionBindings() {
	return EMPTY_ACTIONBINDINGS;
    }

    /**
     * Creates an immutable empty {@link SchedulableActionContext}.
     *
     * @return Empty context.
     */
    @SuppressWarnings("unchecked")
    public static <T> SchedulableActionContext<T> emptyActionContext() {
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
     */
    public static ActionEngine requireNotStopped(final ActionEngine engine) throws IllegalStateException {
	if (engine.isStopped()) {
	    throw new IllegalStateException("Engine has already been stopped");
	}
	return engine;
    }

    /**
     * Creates an immutable {@link ActionBindings}.
     *
     * @param bindings
     *            Bindings to wrap.
     * @return Immutable bindings.
     */
    public static ActionBindings unmodifiableActionBindings(final ActionBindings bindings) {
	return new UnmodifiableDelegateActionBindings(Objects.requireNonNull(bindings));
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

    /**
     * Creates an immutable {@link SchedulableActionContext} that {@link ActionContext#cancel()} and
     * {@link SchedulableActionContext#await()} can still be called.
     *
     * @param context
     *            Context to wrap.
     * @return Immutable context.
     */
    public static <T> SchedulableActionContext<T> unschedulableActionContext(final SchedulableActionContext<T> context) {
	return new UnschedulableDelegateActionContext<>(Objects.requireNonNull(context));
    }

    private Actions() {
	throw new UnsupportedOperationException();
    }
}
