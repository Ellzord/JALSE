package jalse.tags;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
public class TagSet extends AbstractSet<Tag> {

    private class TagsOfType {

	private final Lock r;
	private final Lock w;

	private Set<Tag> t;

	private TagsOfType() {
	    final ReadWriteLock rw = new ReentrantReadWriteLock();
	    r = rw.readLock();
	    w = rw.writeLock();

	    t = null;
	}
    }

    private final ConcurrentMap<Class<?>, TagsOfType> tags;

    /**
     * Creates a new instance of tag set.
     */
    public TagSet() {
	tags = new ConcurrentHashMap<>();
    }

    @Override
    public boolean add(final Tag e) {
	final TagsOfType tot = tags.compute(e.getClass(), (k, v) -> {
	    if (v == null) {
		v = new TagsOfType();
	    }
	    v.w.lock();
	    if (v.t == null) {
		v.t = new CopyOnWriteArraySet<>();
	    }
	    return v;
	});
	try {

	    return tot.t.add(e);
	} finally {
	    tot.w.unlock();
	}
    }

    @Override
    public void clear() {
	tags.clear();
    }

    @Override
    public boolean contains(final Object o) {
	final TagsOfType tot = tags.get(o.getClass());
	if (tot == null) {
	    return false;
	}

	tot.r.lock();
	try {
	    return tot.t.contains(o);
	} finally {
	    tot.r.unlock();
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
	return tags.containsKey(type);
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
	final TagsOfType tot = tags.get(type);
	if (tot == null) {
	    return Collections.emptySet();
	}

	tot.r.lock();
	try {
	    return Collections.unmodifiableSet((Set<T>) tot.t);
	} finally {
	    tot.r.unlock();
	}
    }

    @Override
    public boolean isEmpty() {
	return tags.isEmpty();
    }

    @Override
    public Iterator<Tag> iterator() {
	return tags.values().stream().flatMap(tot -> {
	    tot.r.lock();
	    try {
		return tot.t.stream();
	    } finally {
		tot.r.unlock();
	    }
	}).iterator();
    }

    @Override
    public boolean remove(final Object o) {
	final Class<?> type = o.getClass();
	final TagsOfType tot = tags.get(type);
	if (tot == null) {
	    return false;
	}

	tot.w.lock();
	try {
	    if (tot.t.remove(o)) {
		if (tot.t.isEmpty()) {
		    tot.t = null;
		    tags.remove(type);
		}
		return true;
	    }
	    return false;
	} finally {
	    tot.w.unlock();
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
	return tags.remove(type) != null;
    }

    @Override
    public int size() {
	return stream().mapToInt(t -> 1).sum();
    }
}
