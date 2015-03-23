package jalse.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe queue for {@link AbstractManualActionContext} using the estimated perform time (
 * {@link AbstractManualActionContext#getEstimated()}). This is a convenience class for creating an
 * {@link ActionEngine}.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Actor type.
 *
 * @see ManualActionEngine
 * @see ForkJoinActionEngine
 */
public class ManualWorkQueue<T extends AbstractManualActionContext<?>> {

    private final Queue<T> waitingWork;
    private final Lock lock;
    private final Condition workChanged;

    /**
     * Creates a new instance of ManualWorkQueue.
     */
    public ManualWorkQueue() {
	waitingWork = new PriorityQueue<>();
	lock = new ReentrantLock();
	workChanged = lock.newCondition();
    }

    /**
     * Adds work to the queue.
     *
     * @param context
     *            Work to add.
     * @return Whether the work was not previously within the queue.
     */
    public boolean addWaitingWork(final T context) {
	boolean result;
	synchronized (waitingWork) {
	    result = !waitingWork.contains(context);
	    if (result) {
		waitingWork.add(context);
	    }
	}

	if (result) {
	    signalWorkChanged();
	}

	return result;
    }

    /**
     * Awaits until the next work is ready (or the work queue is empty).
     *
     * @throws InterruptedException
     *             Whether the wait is interrupted.
     */
    public void awaitNextReadyWork() throws InterruptedException {
	lock.lockInterruptibly();
	try {
	    long next = getEarliestReadyEstimate();
	    while (!isWorkReady() && isWorkWaiting()) {
		if (next > 0L) {
		    workChanged.awaitNanos(next - System.nanoTime());
		    next = getEarliestReadyEstimate();
		}
	    }
	} finally {
	    lock.unlock();
	}
    }

    /**
     * Clears waiting work.
     */
    public void clearWaitingWork() {
	synchronized (waitingWork) {
	    waitingWork.clear();
	}
    }

    private long getEarliestReadyEstimate() {
	synchronized (waitingWork) {
	    final T context = waitingWork.peek();
	    return context != null ? context.getEstimated() : 0L;
	}
    }

    /**
     * Gets all waiting work in the queue.
     *
     * @return All waiting work.
     */
    public List<? extends T> getWaitingWork() {
	synchronized (waitingWork) {
	    return new ArrayList<>(waitingWork);
	}
    }

    /**
     * Whether the work is contained in the queue.
     *
     * @param context
     *            Work to check.
     * @return Whether the work was already waiting.
     */
    public boolean isWaitingWork(final T context) {
	synchronized (waitingWork) {
	    return waitingWork.contains(context);
	}
    }

    /**
     * Whether there is work ready.
     *
     * @return Whether work is ready.
     */
    public boolean isWorkReady() {
	synchronized (waitingWork) {
	    return workAvailable();
	}
    }

    /**
     * Whether the queue has waiting work.
     *
     * @return Whether the queue is not empty.
     */
    public boolean isWorkWaiting() {
	return waitingWorkSize() > 0;
    }

    /**
     * Polls for ready work (removes if work is available).
     *
     * @return Work if available (or else null).
     */
    public T pollReadyWork() {
	synchronized (waitingWork) {
	    return workAvailable() ? waitingWork.poll() : null;
	}
    }

    /**
     * Removes work from the queue.
     *
     * @param context
     *            Work to remove.
     * @return Whether the work was already in the queue.
     */
    public boolean removeWaitingWork(final T context) {
	boolean result;
	synchronized (waitingWork) {
	    result = waitingWork.remove(context);
	}

	if (result) {
	    signalWorkChanged();
	}

	return result;
    }

    private void signalWorkChanged() {
	lock.lock();
	try {
	    workChanged.signalAll();
	} finally {
	    lock.unlock();
	}
    }

    /**
     * Gets the waiting work count.
     *
     * @return Work count.
     */
    public int waitingWorkSize() {
	synchronized (waitingWork) {
	    return waitingWork.size();
	}
    }

    private boolean workAvailable() {
	final T work = waitingWork.peek();
	return work != null && System.nanoTime() >= work.getEstimated();
    }
}
