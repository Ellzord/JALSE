package jalse.misc;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is an {@link Iterator} wrapper that locks on {@link #next()} before calling
 * {@link Iterator#next()} on the wrapped {@link Iterator}.
 *
 * @author Elliot Ford
 *
 * @param <E>
 *            Elements to be returned by this iterator.
 *
 * @see #lockingStream(Iterator, Lock, int)
 */
public class LockingIterator<E> implements Iterator<E> {

    /**
     * Creates a new Stream using LockingIterator. This Stream will always have
     * {@link Spliterator#CONCURRENT}, {@link Spliterator#NONNULL} and {@link Spliterator#DISTINCT}
     * as characteristics.
     *
     * @param it
     *            Iterator to wrap.
     * @param lock
     *            Lock to use
     * @param estSize
     *            Estimated size.
     * @return New locking stream.
     */
    public static <T> Stream<T> lockingStream(final Iterator<T> it, final Lock lock, final int estSize) {
	return lockingStream(it, lock, estSize, Spliterator.NONNULL | Spliterator.DISTINCT);
    }

    /**
     * Creates a new Stream using LockingIterator. This Stream will always have
     * {@link Spliterator#CONCURRENT} as a characteristic.
     *
     * @param it
     *            Iterator to wrap.
     * @param lock
     *            Lock to use
     * @param estSize
     *            Estimated size.
     * @param characteristics
     *            Stream characteristics.
     * @return New locking stream.
     */
    public static <T> Stream<T> lockingStream(final Iterator<T> it, final Lock lock, final int estSize,
	    int characteristics) {
	// New locking iterator
	final Iterator<T> lockingIt = new LockingIterator<T>(it, lock);

	// Spliterator characteristics
	characteristics |= Spliterator.CONCURRENT;

	// New non-concurrent stream
	return StreamSupport.stream(Spliterators.spliterator(lockingIt, estSize, characteristics), false);
    }

    private final Iterator<E> it;

    private final Lock lock;

    /**
     * Creates a new LockingIterator.
     *
     * @param it
     *            Iterator to wrap.
     * @param lock
     *            Lock to use.
     */
    public LockingIterator(final Iterator<E> it, final Lock lock) {
	this.it = Objects.requireNonNull(it);
	this.lock = Objects.requireNonNull(lock);
    }

    @Override
    public boolean hasNext() {
	return it.hasNext();
    }

    @Override
    public E next() {
	lock.lock();
	try {
	    return it.next();
	} finally {
	    lock.unlock();
	}
    }
}
