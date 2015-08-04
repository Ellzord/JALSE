package jalse.tags;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * A tag set is a thread-safe {@link Set} implementation for {@link Tag}. A tag set will allow
 * multiple tags of the same type as long as none of them are considered equal (unless
 * {@link SingletonTag} is present on the {@link Tag}). It is possible to query and remove tags by
 * type as well as by instance.
 *
 * @author Elliot Ford
 *
 * @see SingletonTag
 *
 */
public class TagTypeSet extends AbstractSet<Tag>implements Serializable {

    private static final long serialVersionUID = 4251919034814631329L;

    private final ConcurrentMap<Class<?>, Set<Tag>> tags;
    private final Lock read;
    private final Lock write;

    /**
     * Creates a new instance of tag set.
     */
    public TagTypeSet() {
	tags = new ConcurrentHashMap<>();
	final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public boolean add(final Tag e) {
	write.lock();
	try {
	    final Class<?> tagType = e.getClass();
	    final Set<Tag> tagsOfType = tags.computeIfAbsent(tagType,
		    k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

	    // Only allow one of each singleton
	    if (!tagsOfType.isEmpty() && tagType.isAnnotationPresent(SingletonTag.class)) {
		tagsOfType.clear();
	    }

	    return tagsOfType.add(e);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void clear() {
	write.lock();
	try {
	    tags.clear();
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean contains(final Object o) {
	read.lock();
	try {
	    final Set<Tag> tagsOfType = tags.get(o.getClass());
	    return tagsOfType != null && tagsOfType.contains(o);
	} finally {
	    read.unlock();
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
	read.lock();
	try {
	    return tags.containsKey(type);
	} finally {
	    read.unlock();
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
	read.lock();
	try {
	    final Set<T> tagsOfType = (Set<T>) tags.get(type);
	    return tagsOfType != null ? Collections.unmodifiableSet(tagsOfType) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public boolean isEmpty() {
	read.lock();
	try {
	    return tags.isEmpty();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Iterator<Tag> iterator() {
	read.lock();
	try {
	    return tags.values().stream().flatMap(Set::stream).collect(Collectors.toList()).iterator();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public boolean remove(final Object o) {
	write.lock();
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
	    write.unlock();
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
	write.lock();
	try {
	    return tags.remove(type) != null;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public int size() {
	read.lock();
	try {
	    return tags.values().stream().mapToInt(Set::size).sum();
	} finally {
	    read.unlock();
	}
    }
}