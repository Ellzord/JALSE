package jalse.actions;

import static jalse.actions.Actions.requireNotShutdown;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract implementation of {@link ActionEngine} that is designed to be used with
 * {@link ExecutorService}. This is a convenience class for creating an {@link ActionEngine}. <br>
 * <br>
 * If the engine is paused the incoming resumed state change can be awaited (
 * {@link #awaitResumed()}).
 *
 * @author Elliot Ford
 *
 * @see #TERMINATION_TIMEOUT
 * @see DefaultActionBindings
 */
public abstract class AbstractActionEngine implements ActionEngine {

    /**
     * How long the engine will wait until it times out and interrupts running threads on shutdown
     * (configured via {@code jalse.actions.termination_timeout} system property).
     */
    public static final long TERMINATION_TIMEOUT = Long
	    .valueOf(System.getProperty("jalse.actions.termination_timeout", "2000"));

    private static final Logger LOGGER = Logger.getLogger(AbstractActionEngine.class.getName());

    /**
     * Executor service to be used for action scheduling.
     */
    protected final ExecutorService executorService;

    private final ActionBindings bindings;
    private final Lock lock;
    private final Condition resumed;
    private final AtomicBoolean paused;

    /**
     * Creates a new instance of AbstractActionEngine with the supplied executor service.
     *
     * @param executorService
     *            Service to use.
     *
     * @see Actions#requireNotShutdown(ExecutorService)
     */
    protected AbstractActionEngine(final ExecutorService executorService) {
	this.executorService = requireNotShutdown(executorService);
	bindings = new DefaultActionBindings();
	lock = new ReentrantLock();
	resumed = lock.newCondition();
	paused = new AtomicBoolean();
    }

    /**
     * Will wait until the engine has resumed (or stopped).
     *
     * @throws InterruptedException
     *             If the current thread was interrupted.
     */
    protected void awaitResumed() throws InterruptedException {
	lock.lockInterruptibly();
	try {
	    while (isPaused()) {
		resumed.await();
	    }
	} finally {
	    lock.unlock();
	}
    }

    @Override
    public ActionBindings getBindings() {
	return bindings;
    }

    @Override
    public boolean isPaused() {
	return paused.get() && !isStopped();
    }

    @Override
    public boolean isStopped() {
	return executorService.isShutdown();
    }

    @Override
    public void pause() {
	requireNotShutdown(executorService);

	if (!paused.getAndSet(true)) {
	    LOGGER.info("Engine paused");
	}
    }

    @Override
    public void resume() {
	requireNotShutdown(executorService);

	if (paused.getAndSet(false)) {
	    lock.lock();
	    try {
		resumed.signalAll(); // Wake up and work!
	    } finally {
		lock.unlock();
	    }
	    LOGGER.info("Engine resumed");
	}
    }

    @Override
    public void stop() {
	requireNotShutdown(executorService);

	executorService.shutdown();
	try {
	    if (!executorService.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS)) {
		executorService.shutdownNow(); // Uh-oh
	    }
	} catch (final InterruptedException e) {
	    LOGGER.log(Level.WARNING, "Error terminating executor", e);
	    Thread.currentThread().interrupt();
	}
	LOGGER.info("Engine shutdown");
    }
}
