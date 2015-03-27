package jalse.attributes;

import static jalse.attributes.Attributes.requireNonNullAttrSub;
import static jalse.listeners.Listeners.createAttributeListenerSet;
import jalse.listeners.AttributeEvent;
import jalse.listeners.AttributeListener;
import jalse.listeners.ListenerSet;
import jalse.misc.TypeParameterResolver;

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
 * An AttributeSet is a thread-safe {@link Set} implementation for {@link Attribute}. An
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
public class AttributeSet extends AbstractSet<Attribute> {

    private static final TypeParameterResolver RESOLVER = new TypeParameterResolver(AttributeListener.TYPE_PARAMETER);

    private final ConcurrentMap<Class<? extends Attribute>, ListenerSet<AttributeListener>> attributeListeners;
    private final ConcurrentMap<Class<? extends Attribute>, Attribute> attributes;
    private final AttributeContainer delegateContainer;
    private final Lock read;
    private final Lock write;

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
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public boolean add(final Attribute e) {
	return addOfType(e) != null;
    }

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    public boolean addListener(final AttributeListener<? extends Attribute> listener) {
	final Class<? extends Attribute> attr = requireNonNullAttrSub(RESOLVER.resolve(listener));
	final ListenerSet<AttributeListener> ls = attributeListeners.computeIfAbsent(attr,
		k -> createAttributeListenerSet());
	return ls.add(listener);
    }

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> T addOfType(final T attr) {
	final Class<? extends Attribute> type = attr.getClass();

	write.lock();
	try {
	    final T previous = (T) attributes.put(type, attr);

	    final ListenerSet<?> ls = attributeListeners.get(type);
	    if (ls != null) {
		((ListenerSet<? extends AttributeListener<T>>) ls).getProxy().attributeAdded(
			new AttributeEvent<>(delegateContainer, attr, previous));
	    }

	    return previous;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void clear() {
	write.lock();
	try {
	    new HashSet<>(attributes.keySet()).forEach(this::removeOfType);
	} finally {
	    write.unlock();
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(final Object o) {
	if (!(o instanceof Attribute)) {
	    throw new IllegalArgumentException();
	}
	return Objects.equals(o, getOfType((Class<? extends Attribute>) o.getClass()));
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param attr
     *            Attribute type to fire for.
     * @return Whether the collection contains an attribute matching the supplied type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> boolean fireChanged(final Class<T> attr) {
	final T associated = getOfType(attr);
	if (associated == null) {
	    return false;
	}

	final ListenerSet<?> ls = attributeListeners.get(attr);
	if (ls != null) {
	    ((ListenerSet<? extends AttributeListener<T>>) ls).getProxy().attributeChanged(
		    new AttributeEvent<>(delegateContainer, associated));
	}

	return true;
    }

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<Class<? extends Attribute>> getAttributeTypes() {
	read.lock();
	try {
	    return Collections.unmodifiableSet(attributes.keySet()); // Read-only.
	} finally {
	    read.unlock();
	}
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
    public Set<? extends AttributeListener<? extends Attribute>> getListeners() {
	return attributeListeners.values().stream().flatMap(ListenerSet::stream).collect(Collectors.toSet());
    }

    /**
     * Gets all attribute listeners associated to the supplied attribute type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> Set<? extends AttributeListener<T>> getListeners(final Class<T> attr) {
	final Set<AttributeListener> ls = attributeListeners.get(requireNonNullAttrSub(attr));
	return ls != null ? Collections.unmodifiableSet((Set<? extends AttributeListener<T>>) ls) : Collections
		.emptySet();
    }

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<Class<? extends Attribute>> getListenerTypes() {
	return Collections.unmodifiableSet(attributeListeners.keySet());
    }

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> T getOfType(final Class<T> attr) {
	read.lock();
	try {
	    return (T) attributes.get(requireNonNullAttrSub(attr));
	} finally {
	    read.unlock();
	}
    }

    @Override
    public boolean isEmpty() {
	read.lock();
	try {
	    return attributes.isEmpty();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Iterator<Attribute> iterator() {
	return attributes.values().iterator(); // Read-only
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(final Object o) {
	if (!(o instanceof Attribute)) {
	    throw new IllegalArgumentException();
	}
	return removeOfType((Class<? extends Attribute>) o.getClass()) != null;
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
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    public <T extends Attribute> boolean removeListener(final AttributeListener<T> listener) {
	final Class<?> attr = requireNonNullAttrSub(RESOLVER.resolve(listener));

	final Set<AttributeListener> ls = attributeListeners.get(attr);
	if (ls != null && ls.remove(listener)) {
	    if (attributeListeners.isEmpty()) { // No more listeners of type
		attributeListeners.remove(attr);
	    }

	    return true;
	}

	return false;
    }

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param attr
     *            Attribute type.
     */
    public <T extends Attribute> void removeListeners(final Class<T> attr) {
	attributeListeners.remove(requireNonNullAttrSub(attr));
    }

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to remove.
     * @return The removed attribute or null if none was removed.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> T removeOfType(final Class<T> attr) {
	write.lock();
	try {
	    final T previous = (T) attributes.remove(requireNonNullAttrSub(attr));

	    if (previous != null) {
		final ListenerSet<?> ls = attributeListeners.get(attr);
		if (ls != null) {
		    ((ListenerSet<? extends AttributeListener<T>>) ls).getProxy().attributeRemoved(
			    new AttributeEvent<>(delegateContainer, previous));
		}
	    }

	    return previous;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public int size() {
	read.lock();
	try {
	    return attributes.size();
	} finally {
	    read.unlock();
	}
    }
}
