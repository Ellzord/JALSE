package jalse.attributes;

import static jalse.listeners.Listeners.createAttributeListenerSet;
import jalse.listeners.AttributeEvent;
import jalse.listeners.AttributeListener;
import jalse.listeners.ListenerSet;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * An AttributeSet is a thread-safe {@link Set} implementation for {@link AttributeType}. An
 * AttributeSet follows the same pattern defined in {@link AttributeContainer} but offers a
 * collections based implementation (so it may be used more generically).<br>
 * <br>
 *
 * AttributeSet can take a delegate AttributeContainer to supply to {@link AttributeEvent}.
 * Attribute updates will trigger these events using {@link AttributeListener}.
 *
 * @author Elliot Ford
 *
 */
@SuppressWarnings("rawtypes")
public class AttributeSet extends AbstractSet<Object> {

    private class AttributeValue {

	private final Lock r;
	private final Lock w;

	private Object a;
	private Object p;

	private AttributeValue() {
	    final ReadWriteLock rw = new ReentrantReadWriteLock();
	    r = rw.readLock();
	    w = rw.writeLock();

	    a = null;
	    p = null;
	}
    }

    private final ConcurrentMap<AttributeType<?>, ListenerSet<AttributeListener>> attributeListeners;
    private final ConcurrentMap<AttributeType<?>, AttributeValue> attributes;
    private final AttributeContainer delegateContainer;

    /**
     * Creates a new AttributeSet with no delegate container.
     */
    public AttributeSet() {
	this(null);
    }

    /**
     * Creates a new AttributeSet with the supplied delegate container.
     *
     * @param delegateContainer
     *            Delegate container for events.
     */
    public AttributeSet(final AttributeContainer delegateContainer) {
	this.delegateContainer = delegateContainer != null ? delegateContainer : Attributes.toAttributeContainer(this);
	attributes = new ConcurrentHashMap<>();
	attributeListeners = new ConcurrentHashMap<>();
    }

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    public <T> boolean addListener(final AttributeType<T> type, final AttributeListener<T> listener) {
	final ListenerSet<AttributeListener> ls = attributeListeners.computeIfAbsent(type,
		k -> createAttributeListenerSet());
	return ls.add(listener);
    }

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param type
     *            Attribute type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    @SuppressWarnings("unchecked")
    public <T> T addOfType(final AttributeType<T> type, final T attr) {
	final AttributeValue av = attributes.compute(type, (k, v) -> {
	    if (v == null) {
		v = new AttributeValue();
	    }
	    v.w.lock();
	    v.p = v.a;
	    v.a = attr;
	    return v;
	});

	try {
	    final T prev = (T) av.p;
	    final ListenerSet<?> ls = attributeListeners.get(type);
	    if (ls != null) {
		((ListenerSet<? extends AttributeListener<T>>) ls).getProxy().attributeAdded(
			new AttributeEvent<>(delegateContainer, type, attr, prev));
	    }
	    return prev;
	} finally {
	    av.w.unlock();
	}
    }

    @Override
    public void clear() {
	new HashSet<>(attributes.keySet()).forEach(this::removeOfType);
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param type
     *            Attribute type.
     */
    @SuppressWarnings("unchecked")
    public <T> void fireChanged(final AttributeType<T> type) {
	final AttributeValue av = attributes.get(Objects.requireNonNull(type));
	if (av == null) {
	    return;
	}

	av.r.lock();
	try {
	    final ListenerSet<?> ls = attributeListeners.get(type);
	    if (ls != null) {
		((ListenerSet<? extends AttributeListener<T>>) ls).getProxy().attributeChanged(
			new AttributeEvent<>(delegateContainer, type, (T) av.a));
	    }
	} finally {
	    av.r.unlock();
	}
    }

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<AttributeType<?>> getAttributeTypes() {
	return Collections.unmodifiableSet(attributes.keySet());
    }

    /**
     * Gets the delegate container for the events.
     *
     * @return Delegate event container.
     */
    public AttributeContainer getDelegateContainer() {
	return delegateContainer;
    }

    /**
     * Gets all the attribute listeners.
     *
     * @return Set of attribute listeners or an empty set if none were found.
     */
    public Set<? extends AttributeListener<?>> getListeners() {
	return attributeListeners.values().stream().flatMap(ListenerSet::stream).collect(Collectors.toSet());
    }

    /**
     * Gets all attribute listeners associated to the supplied attribute type.
     *
     * @param type
     *            Attribute type.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    @SuppressWarnings("unchecked")
    public <T> Set<? extends AttributeListener<T>> getListeners(final AttributeType<T> type) {
	final Set<AttributeListener> ls = attributeListeners.get(Objects.requireNonNull(type));
	return ls != null ? Collections.unmodifiableSet((Set<? extends AttributeListener<T>>) ls) : Collections
		.emptySet();
    }

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<AttributeType<?>> getListenerTypes() {
	return Collections.unmodifiableSet(attributeListeners.keySet());
    }

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param type
     *            Attribute type.
     * @return The attribute matching the supplied type or null if none found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOfType(final AttributeType<T> type) {
	final AttributeValue av = attributes.get(Objects.requireNonNull(type));
	if (av == null) {
	    return null;
	}

	av.r.lock();
	try {
	    return (T) av.a;
	} finally {
	    av.r.unlock();
	}
    }

    @Override
    public boolean isEmpty() {
	return attributes.isEmpty();
    }

    @Override
    public Iterator<Object> iterator() {
	return attributes.values().stream().map(av -> {
	    av.r.lock();
	    try {
		return av.a;
	    } finally {
		av.r.unlock();
	    }
	}).iterator();
    }

    /**
     * Removes all listeners for all attribute types.
     */
    public void removeAllListeners() {
	attributeListeners.clear();
    }

    /**
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    public <T> boolean removeListener(final AttributeType<T> type, final AttributeListener<T> listener) {
	final Set<AttributeListener> ls = attributeListeners.get(Objects.requireNonNull(type));
	if (ls != null && ls.remove(listener)) {
	    if (attributeListeners.isEmpty()) { // No more listeners of type
		attributeListeners.remove(type);
	    }
	    return true;
	}
	return false;
    }

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param type
     *            Attribute type.
     */
    public <T> void removeListeners(final AttributeType<T> type) {
	attributeListeners.remove(Objects.requireNonNull(type));
    }

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param type
     *            Attribute type.
     * @return The removed attribute or null if none was removed.
     */
    @SuppressWarnings("unchecked")
    public <T> T removeOfType(final AttributeType<T> type) {
	final AttributeValue av = attributes.get(Objects.requireNonNull(type));
	if (av == null) {
	    return null;
	}

	av.w.lock();
	try {
	    attributes.remove(type);
	    final T prev = (T) av.a;

	    av.a = null;
	    av.p = null;

	    final ListenerSet<?> ls = attributeListeners.get(type);
	    if (ls != null) {
		((ListenerSet<? extends AttributeListener<T>>) ls).getProxy().attributeRemoved(
			new AttributeEvent<>(delegateContainer, type, prev));
	    }

	    return prev;
	} finally {
	    av.w.unlock();
	}
    }

    @Override
    public int size() {
	return attributes.size();
    }
}
