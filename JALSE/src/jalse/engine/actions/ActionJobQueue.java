package jalse.engine.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * This is a simple {@link PriorityQueue} implementation for {@link ActionJob}. It provides the
 * ability to get and remove jobs based on the matching {@link Action} ID. The queue can be drained
 * of work that should have executed by a set time ({@link System#nanoTime()}).
 *
 * @author Elliot Ford
 *
 * @see #drainBeforeEstimated(long)
 *
 */
public class ActionJobQueue extends PriorityQueue<ActionJob<?>> {

    private static final long serialVersionUID = 800017783405972878L;

    /**
     * Whether the queue contains a job matching the scheduled action ID.
     *
     * @param action
     *            Action ID.
     * @return Whether a matching job was found.
     */
    public boolean containsJob(final UUID action) {
	return getJob(action) != null;
    }

    /**
     * Drains all of the jobs from the queue that should start execution before or on the supplied
     * estimated time.
     *
     * @param estimated
     *            Estimated end.
     * @return A list of all the jobs to be executed before the specified time or an empty list if
     *         none should be.
     */
    public List<ActionJob<?>> drainBeforeEstimated(final long estimated) {
	final List<ActionJob<?>> result = new ArrayList<>();

	for (;;) {
	    final ActionJob<?> job = peek();
	    if (job == null || job.getEstimated() >= estimated) {
		break;
	    }
	    remove();
	    result.add(job);
	}

	return result;
    }

    /**
     * Gets the job matching the scheduled action ID.
     *
     * @param action
     *            Action ID.
     * @return The matching job or null if none were found.
     */
    public ActionJob<?> getJob(final UUID action) {
	Objects.requireNonNull(action);
	return stream().filter(job -> job.getContext().getID().equals(action)).findFirst().orElse(null);
    }

    /**
     * Removes the job matching the scheduled action ID.
     *
     * @param action
     *            Action ID.
     * @return Whether there was a job matching the action ID.
     */
    public boolean removeJob(final UUID action) {
	Objects.requireNonNull(action);
	return removeIf(job -> job.getContext().getID().equals(action));
    }
}
