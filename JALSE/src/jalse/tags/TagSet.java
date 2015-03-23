package jalse.tags;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;

/**
 * A tag set is a thread-safe {@link Set} implementation for {@link Tag}. A tag set will allow
 * multiple tags of the same type as long as none of them are considered equal. It is possible to
 * query and remove tags by type as well as by instance.
 *
 * @author Elliot Ford
 *
 * @see Object#equals(Object)
 * @see Object#hashCode()
 *
 */
public class TagSet extends AbstractSet<Tag> implements Serializable {

    private static final long serialVersionUID = 4251919034814631329L;

    private final Map<Class<?>, Set<Tag>> tags;
    private final StampedLock lock;

    /**
     * Creates a new instance of tag set.
     */
    public TagSet() {
	tags = new HashMap<>();
	lock = new StampedLock();
    }

    @Override
    public boolean add(final Tag e) {
	final long stamp = lock.writeLock();
	try {
	    Set<Tag> tagsOfType = tags.get(e.getClass());
	    if (tagsOfType == null) {
		tags.put(e.getClass(), tagsOfType = new HashSet<>());
	    }
	    return tagsOfType.add(e);
	} finally {
	    lock.unlockWrite(stamp);
	}
    }

    @Override
    public void clear() {
	final long stamp = lock.writeLock();
	try {
	    tags.clear();
	} finally {
	    lock.unlockWrite(stamp);
	}
    }

    @Override
    public boolean contains(final Object o) {
	final long stamp = lock.readLock();
	try {
	    final Set<Tag> tagsOfType = tags.get(o.getClass());
	    return tagsOfType != null && tagsOfType.contains(o);
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Checks whether the tag set contains any tags of that type.
     *
     * @param type
     *            Type of tag.
     * @return {@code true} if the set contains any tags of the specified type or {@code false} if
     *         it does not.
     */
    public boolean containsOfType(final Class<? extends Tag> type) {
	final long stamp = lock.readLock();
	try {
	    return tags.containsKey(type);
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    /**
     * Gets all of the tags matching the specified type.
     *
     * @param type
     *            Type of tag.
     * @return All tags of that type or an empty set if none are found.
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> Set<T> getOfType(final Class<T> type) {
	final long stamp = lock.readLock();
	try {
	    final Set<T> tagsOfType = (Set<T>) tags.get(type);
	    return tagsOfType != null ? Collections.unmodifiableSet(tagsOfType) : Collections.emptySet();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    @Override
    public boolean isEmpty() {
	final long stamp = lock.readLock();
	try {
	    return tags.isEmpty();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    @Override
    public Iterator<Tag> iterator() {
	final long stamp = lock.readLock();
	try {
	    return tags.values().stream().flatMap(s -> s.stream()).iterator();
	} finally {
	    lock.unlockRead(stamp);
	}
    }

    @Override
    public boolean remove(final Object o) {
	final long stamp = lock.writeLock();
	try {
	    final Set<Tag> tagsOfType = tags.get(o.getClass());
	    boolean removed = false;

	    if (tagsOfType != null) {
		removed = tagsOfType.remove(o);
		if (tagsOfType.isEmpty()) {
		    tags.remove(o.getClass());
		}
	    }
	    return removed;
	} finally {
	    lock.unlockWrite(stamp);
	}
    }

    /**
     * Removes all tags of the specified type.
     *
     * @param type
     *            Type of tag.
     * @return Whether any tags were removed.
     */
    public boolean removeOfType(final Class<? extends Tag> type) {
	final long stamp = lock.writeLock();
	try {
	    return tags.remove(type) != null;
	} finally {
	    lock.unlockWrite(stamp);
	}
    }

    @Override
    public int size() {
	final long stamp = lock.readLock();
	try {
	    return tags.values().stream().mapToInt(s -> s.size()).sum();
	} finally {
	    lock.unlockRead(stamp);
	}
    }
}
