package jalse.attributes;

import static jalse.attributes.Attributes.requireNotEmpty;
import jalse.listeners.AttributeEvent;
import jalse.listeners.AttributeListener;
import jalse.listeners.ListenerSet;
import jalse.listeners.Listeners;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An AttributeSet is a thread-safe {@link Set} implementation for {@link AttributeType} values. An
 * AttributeSet follows the same pattern defined in {@link AttributeContainer} but offers a
 * collections based implementation (so it may be used more generically). This set considers types
 * to be unique on name only.<br>
 * <br>
 *
 * AttributeSet can take a delegate AttributeContainer to supply to {@link AttributeEvent}.
 * Attribute updates will trigger these events using {@link AttributeListener}.
 *
 * @author Elliot Ford
 *
 */
public class AttributeSet extends AbstractSet<Object> {

    private static void checkNameAndType(final String name, final AttributeType<?> type) {
	requireNotEmpty(name);
	Objects.requireNonNull(type);
    }

    @SuppressWarnings("rawtypes")
    private final Map<String, Map<AttributeType<?>, ListenerSet<AttributeListener>>> listeners;
    private final Map<String, Map<AttributeType<?>, Object>> attributes;
    private final AttributeContainer delegateContainer;
    private final Lock read;
    private final Lock write;

    /**
     * Creates a new instance of AttributeSet with no delegate container (self).
     */
    public AttributeSet() {
	this(null);
    }

    /**
     * Creates a new instance of AttributeSet with a delegate container.
     *
     * @param delegateContainer
     *            Delegate AttributeContainer for events.
     */
    public AttributeSet(final AttributeContainer delegateContainer) {
	this.delegateContainer = delegateContainer != null ? delegateContainer : Attributes.toAttributeContainer(this);
	attributes = new ConcurrentHashMap<>();
	listeners = new ConcurrentHashMap<>();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    public <T> boolean addListener(final String name, final AttributeType<T> type, final AttributeListener<T> listener) {
	checkNameAndType(name, type);
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    @SuppressWarnings("rawtypes")
	    final Map<AttributeType<?>, ListenerSet<AttributeListener>> lsn = listeners.computeIfAbsent(name,
		    k -> new ConcurrentHashMap<>());

	    @SuppressWarnings("rawtypes")
	    final ListenerSet<AttributeListener> lst = lsn.computeIfAbsent(type,
		    k -> Listeners.newAttributeListenerSet());

	    return lst.add(listener);
	} finally {
	    write.unlock();
	}
    }

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    public <T> T addOfType(final String name, final AttributeType<T> type, final T attr) {
	checkNameAndType(name, type);
	Objects.requireNonNull(attr);

	write.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.computeIfAbsent(name, k -> new ConcurrentHashMap<>());

	    @SuppressWarnings("unchecked")
	    final T prev = (T) atrn.put(type, attr);

	    @SuppressWarnings("rawtypes")
	    final ListenerSet<AttributeListener> ls = getListeners0(name, type);
	    if (ls != null) {
		@SuppressWarnings("unchecked")
		final AttributeListener<T> als = ls.getProxy();
		als.attributeAdded(new AttributeEvent<>(delegateContainer, type, attr, prev));
	    }

	    return prev;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void clear() {
	write.lock();
	try {
	    final Map<String, Set<AttributeType<?>>> namesToTypes = new HashMap<>();
	    attributes.entrySet().forEach(e -> {
		namesToTypes.put(e.getKey(), new HashSet<>(e.getValue().keySet()));
	    });
	    namesToTypes.forEach((n, ts) -> {
		ts.forEach(t -> {
		    removeOfType(n, t);
		});
	    });
	} finally {
	    write.unlock();
	}
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to fire for.
     */
    public <T> void fireChanged(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);

	read.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.get(name);
	    if (atrn == null) {
		return;
	    }

	    @SuppressWarnings("unchecked")
	    final T current = (T) atrn.get(type);
	    if (current == null) {
		return;
	    }

	    @SuppressWarnings("rawtypes")
	    final ListenerSet<AttributeListener> ls = getListeners0(name, type);
	    if (ls != null) {
		@SuppressWarnings("unchecked")
		final AttributeListener<T> als = ls.getProxy();
		als.attributeChanged(new AttributeEvent<>(delegateContainer, type, current));
	    }
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets the delegate container.
     *
     * @return Delegate event container.
     */
    public AttributeContainer getDelegateContainer() {
	return delegateContainer;
    }

    /**
     * Gets the attribute type names with listeners associated.
     *
     * @return Associated attribute type names to listeners.
     */
    public Set<String> getListenerNames() {
	return Collections.unmodifiableSet(listeners.keySet());
    }

    /**
     * Gets all attribute listeners associated to the supplied named attribute type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    public <T> Set<? extends AttributeListener<T>> getListeners(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);
	read.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final Set<? extends AttributeListener<T>> ls = (Set<? extends AttributeListener<T>>) getListeners0(name,
		    type);
	    return ls != null ? Collections.unmodifiableSet(ls) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    @SuppressWarnings("rawtypes")
    private ListenerSet<AttributeListener> getListeners0(final String name, final AttributeType<?> type) {
	final Map<AttributeType<?>, ListenerSet<AttributeListener>> ls = listeners.get(name);
	return ls != null ? ls.get(type) : null;
    }

    /**
     * Gets all the attribute listener types.
     *
     * @param name
     *            Attribute type name.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<AttributeType<?>> getListenerTypes(final String name) {
	requireNotEmpty(name);

	read.lock();
	try {
	    @SuppressWarnings("rawtypes")
	    final Map<AttributeType<?>, ListenerSet<AttributeListener>> ls = listeners.get(name);
	    return ls != null ? Collections.unmodifiableSet(ls.keySet()) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets all the attribute type names assigned to attributes.
     *
     * @return Attribute type names with values.
     */
    public Set<String> getNames() {
	return Collections.unmodifiableSet(attributes.keySet());
    }

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOfType(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);

	read.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.get(name);
	    return atrn != null ? (T) atrn.get(type) : null;
	} finally {
	    read.unlock();
	}
    }

    /**
     * Gets all of the attribute types within the container.
     *
     * @param name
     *            Attribute type name.
     *
     * @return All of the types of the attributes or an empty set if none were found.
     */
    public Set<AttributeType<?>> getTypes(final String name) {
	requireNotEmpty(name);

	read.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.get(name);
	    return atrn != null ? Collections.unmodifiableSet(atrn.keySet()) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Iterator<Object> iterator() {
	return attributes.values().stream().flatMap(atrn -> atrn.values().stream()).iterator();
    }

    /**
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    public <T> boolean removeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	checkNameAndType(name, type);
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    @SuppressWarnings("rawtypes")
	    final Map<AttributeType<?>, ListenerSet<AttributeListener>> lsn = listeners.get(name);
	    if (lsn == null) {
		return false;
	    }

	    @SuppressWarnings("rawtypes")
	    final ListenerSet<AttributeListener> lst = lsn.get(type);
	    if (lst == null) {
		return false;
	    }

	    if (!lst.remove(listener)) {
		return false;
	    }

	    lsn.computeIfPresent(type, (k, v) -> v.isEmpty() ? null : v);
	    listeners.computeIfPresent(name, (k, v) -> v.isEmpty() ? null : v);

	    return true;
	} finally {
	    write.unlock();
	}
    }

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     */
    public <T> void removeListeners(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);

	write.lock();
	try {
	    @SuppressWarnings("rawtypes")
	    final Map<AttributeType<?>, ListenerSet<AttributeListener>> lsn = listeners.get(name);
	    if (lsn == null) {
		return;
	    }

	    if (lsn.remove(type) != null) {
		listeners.computeIfPresent(name, (k, v) -> v.isEmpty() ? null : v);
	    }
	} finally {
	    write.unlock();
	}
    }

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to remove.
     * @return The removed attribute or null if none was removed.
     */
    public <T> T removeOfType(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);

	write.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.get(name);
	    if (atrn == null) {
		return null;
	    }

	    @SuppressWarnings("unchecked")
	    final T prev = (T) atrn.remove(type);

	    if (prev != null) {
		attributes.computeIfPresent(name, (k, v) -> v.isEmpty() ? null : v);

		@SuppressWarnings("rawtypes")
		final ListenerSet<AttributeListener> ls = getListeners0(name, type);
		if (ls != null) {
		    @SuppressWarnings("unchecked")
		    final AttributeListener<T> als = ls.getProxy();
		    als.attributeRemoved(new AttributeEvent<>(delegateContainer, type, prev));
		}
	    }

	    return prev;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public int size() {
	return attributes.values().stream().mapToInt(Map::size).sum();
    }
}
