package jalse.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private final Lock read;
    private final Lock write;
    private final Condition workChanged;

    /**
     * Creates a new instance of ManualWorkQueue.
     */
    public ManualWorkQueue() {
	waitingWork = new PriorityQueue<>();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
	workChanged = write.newCondition();
    }

    /**
     * Adds work to the queue.
     *
     * @param context
     *            Work to add.
     * @return Whether the work was not previously within the queue.
     */
    public boolean addWaitingWork(final T context) {
	write.lock();
	try {
	    boolean result;
	    if (result = !waitingWork.contains(context)) {
		waitingWork.add(context);
		workChanged.signalAll(); // Wake up!
	    }
	    return result;
	} finally {
	    write.unlock();
	}
    }

    /**
     * Awaits until the next work is ready (or the work queue is empty).
     *
     * @throws InterruptedException
     *             Whether the wait is interrupted.
     */
    public void awaitNextReadyWork() throws InterruptedException {
	write.lockInterruptibly();
	try {
	    long next = getEarliestReadyEstimate();
	    while (!workAvailable() && !waitingWork.isEmpty()) {
		if (next > 0L) {
		    workChanged.awaitNanos(next - System.nanoTime()); // Or signal
		    next = getEarliestReadyEstimate(); // Might be new work added?
		}
	    }
	} finally {
	    write.unlock();
	}
    }

    /**
     * Clears waiting work.
     */
    public void clearWaitingWork() {
	write.lock();
	try {
	    waitingWork.clear();
	    workChanged.signalAll(); // Wake up!
	} finally {
	    write.unlock();
	}
    }

    private long getEarliestReadyEstimate() {
	final T context = waitingWork.peek();
	return context != null ? context.getEstimated() : 0L;
    }

    /**
     * Gets all waiting work in the queue.
     *
     * @return All waiting work.
     */
    public List<? extends T> getWaitingWork() {
	read.lock();
	try {
	    return new ArrayList<>(waitingWork);
	} finally {
	    read.unlock();
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
	read.lock();
	try {
	    return waitingWork.contains(context);
	} finally {
	    read.unlock();
	}
    }

    /**
     * Whether there is work ready.
     *
     * @return Whether work is ready.
     */
    public boolean isWorkReady() {
	read.lock();
	try {
	    return workAvailable();
	} finally {
	    read.unlock();
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
	write.lock();
	try {
	    return workAvailable() ? waitingWork.poll() : null;
	} finally {
	    write.unlock();
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
	write.lock();
	try {
	    boolean result;
	    if (result = waitingWork.remove(context)) {
		workChanged.signalAll();
	    }
	    return result;
	} finally {
	    write.unlock();
	}
    }

    /**
     * Gets the waiting work count.
     *
     * @return Work count.
     */
    public int waitingWorkSize() {
	read.lock();
	try {
	    return waitingWork.size();
	} finally {
	    read.unlock();
	}
    }

    private boolean workAvailable() {
	final T work = waitingWork.peek();
	return work != null && System.nanoTime() >= work.getEstimated();
    }
}
