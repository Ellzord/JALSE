package jalse.tags;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A tag set is a thread-safe {@link Set} implementation for {@link Tag}. A tag
 * set will allow multiple tags of the same type as long as none of them are
 * considered equal. It is possible to query and remove tags by type as well as
 * by instance.
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

    /**
     * Creates a new instance of tag set.
     */
    public TagSet() {

	tags = new HashMap<>();
    }

    @Override
    public synchronized boolean add(final Tag e) {

	Set<Tag> tagsOfType = tags.get(e.getClass());

	if (tagsOfType == null) {

	    tags.put(e.getClass(), tagsOfType = new HashSet<>());
	}

	return tagsOfType.add(e);
    }

    @Override
    public synchronized void clear() {

	tags.clear();
    }

    @Override
    public synchronized boolean contains(final Object o) {

	final Set<Tag> tagsOfType = tags.get(o.getClass());

	return tagsOfType != null && tagsOfType.contains(o);
    }

    /**
     * Checks whether the tag set contains any tags of that type.
     *
     * @param type
     *            Type of tag.
     * @return {@code true} if the set contains any tags of the specified type
     *         or {@code false} if it does not.
     */
    public synchronized boolean containsOfType(final Class<? extends Tag> type) {

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
    public synchronized <T extends Tag> Set<T> getOfType(final Class<T> type) {

	final Set<T> tagsOfType = (Set<T>) tags.get(type);

	return tagsOfType != null ? Collections.unmodifiableSet(tagsOfType) : Collections.emptySet();
    }

    @Override
    public synchronized boolean isEmpty() {

	return tags.isEmpty();
    }

    @Override
    public synchronized Iterator<Tag> iterator() {

	return tags.values().stream().flatMap(s -> s.stream()).iterator();
    }

    @Override
    public synchronized boolean remove(final Object o) {

	boolean removed = false;

	final Set<Tag> tagsOfType = tags.get(o.getClass());

	if (tagsOfType != null) {

	    removed = tagsOfType.remove(o);

	    if (tagsOfType.isEmpty()) {

		tags.remove(o.getClass());
	    }
	}

	return removed;
    }

    /**
     * Removes all tags of the specified type.
     *
     * @param type
     *            Type of tag.
     * @return Whether any tags were removed.
     */
    public synchronized boolean removeOfType(final Class<? extends Tag> type) {

	return tags.remove(type) != null;
    }

    @Override
    public synchronized int size() {

	return tags.values().stream().mapToInt(s -> s.size()).sum();
    }
}
