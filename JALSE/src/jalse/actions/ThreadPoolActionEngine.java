package jalse.actions;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of {@link ActionEngine} based on {@link ScheduledThreadPoolExecutor} .
 *
 * @author Elliot Ford
 *
 */
public class ThreadPoolActionEngine extends AbstractActionEngine {

    /**
     * Thread pool context.
     *
     * @author Elliot Ford
     *
     * @param <T>
     *            Actor type.
     */
    protected class ThreadPoolContext<T> extends AbstractFutureActionContext<T> implements Runnable {

	/**
	 * Creates new instance of ThreadPoolContext.
	 *
	 * @param action
	 *            Action this context is for.
	 */
	protected ThreadPoolContext(final Action<T> action) {
	    super(ThreadPoolActionEngine.this, action, getBindings());
	}

	@Override
	public void run() {
	    try {
		getAction().perform(this);
	    } catch (final Exception e) {
		if (e instanceof InterruptedException) {
		    Thread.currentThread().interrupt();
		}

		logger.log(Level.WARNING, "Error performing action", e);
	    }
	}

	@Override
	public void schedule() {
	    if (!isDone()) {
		final ScheduledThreadPoolExecutor stpe = (ScheduledThreadPoolExecutor) executorService;
		final long initialDelay = getInitialDelay(TimeUnit.NANOSECONDS);

		if (isPeriodic()) {
		    setFuture(stpe.scheduleAtFixedRate(this, initialDelay, getPeriod(TimeUnit.NANOSECONDS),
			    TimeUnit.NANOSECONDS));
		} else {
		    setFuture(stpe.schedule(this, initialDelay, TimeUnit.NANOSECONDS));
		}
	    }
	}
    }

    private static final Logger logger = Logger.getLogger(ThreadPoolActionEngine.class.getName());

    /**
     * Creates a new instance of ThreadPoolActionEngine with the supplied core pool size.
     *
     * @param corePoolSize
     *            Number of threads to process actions using.
     */
    public ThreadPoolActionEngine(final int corePoolSize) {
	super(new ScheduledThreadPoolExecutor(corePoolSize));
    }

    @Override
    public <T> MutableActionContext<T> createContext(final Action<T> action) {
	return new ThreadPoolContext<>(action);
    }
}