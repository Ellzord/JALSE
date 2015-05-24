package jalse.attributes;

import static jalse.attributes.Attributes.requireNotEmpty;
import jalse.listeners.AttributeContainerEvent;
import jalse.listeners.AttributeContainerListener;
import jalse.listeners.ListenerSet;
import jalse.listeners.Listeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * An DefaultAttributeContainer is a thread-safe implementation of {@link AttributeContainer}.<br>
 * <br>
 *
 * DefaultAttributeContainer can take a delegate AttributeContainer to supply to
 * {@link AttributeContainerEvent}. Attribute updates will trigger these events using
 * {@link AttributeContainerListener}.
 *
 * @author Elliot Ford
 *
 */
public class DefaultAttributeContainer implements AttributeContainer {

    private static void checkNameAndType(final String name, final AttributeType<?> type) {
	requireNotEmpty(name);
	Objects.requireNonNull(type);
    }

    private final Map<String, Map<AttributeType<?>, ListenerSet<?>>> listeners;
    private final Map<String, Map<AttributeType<?>, Object>> attributes;
    private final AttributeContainer delegateContainer;
    private final Lock read;
    private final Lock write;

    /**
     * Creates a new instance of DefaultAttributeContainer with no delegate container (self).
     */
    public DefaultAttributeContainer() {
	this(null);
    }

    /**
     * Creates a new instance of DefaultAttributeContainer with a delegate container.
     *
     * @param delegateContainer
     *            Delegate AttributeContainer for events.
     */
    public DefaultAttributeContainer(final AttributeContainer delegateContainer) {
	this.delegateContainer = delegateContainer != null ? delegateContainer : this;
	attributes = new ConcurrentHashMap<>();
	listeners = new ConcurrentHashMap<>();
	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
    }

    @Override
    public <T> boolean addAttributeContainerListener(final String name, final AttributeType<T> type,
	    final AttributeContainerListener<T> listener) {
	checkNameAndType(name, type);
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    final Map<AttributeType<?>, ListenerSet<?>> lsn = listeners.computeIfAbsent(name,
		    k -> new ConcurrentHashMap<>());

	    @SuppressWarnings({ "unchecked" })
	    final ListenerSet<AttributeContainerListener<T>> lst = (ListenerSet<AttributeContainerListener<T>>) lsn
		    .computeIfAbsent(type, k -> {
			return Listeners.<T> newAttributeContainerListenerSet();
		    });

	    return lst.add(listener);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == this) {
	    return true;
	}

	if (!(obj instanceof DefaultAttributeContainer)) {
	    return false;
	}

	final DefaultAttributeContainer other = (DefaultAttributeContainer) obj;
	return attributes.equals(other.attributes) && listeners.equals(other.listeners);
    }

    @Override
    public <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
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

	    @SuppressWarnings("unchecked")
	    final ListenerSet<AttributeContainerListener<T>> ls = (ListenerSet<AttributeContainerListener<T>>) getAttributeContainerListeners0(
		    name, type);
	    if (ls != null) {
		ls.getProxy().attributeChanged(new AttributeContainerEvent<>(delegateContainer, name, type, current));
	    }
	} finally {
	    read.unlock();
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);

	read.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.get(name);
	    return atrn != null ? (T) atrn.get(type) : null;
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<String> getAttributeContainerListenerNames() {
	read.lock();
	try {
	    return new HashSet<>(listeners.keySet());
	} finally {
	    read.unlock();
	}
    }

    @Override
    public <T> Set<? extends AttributeContainerListener<T>> getAttributeContainerListeners(final String name,
	    final AttributeType<T> type) {
	checkNameAndType(name, type);
	read.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final Set<? extends AttributeContainerListener<T>> ls = (Set<? extends AttributeContainerListener<T>>) getAttributeContainerListeners0(
		    name, type);
	    return ls != null ? new HashSet<>(ls) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    private ListenerSet<?> getAttributeContainerListeners0(final String name, final AttributeType<?> type) {
	final Map<AttributeType<?>, ListenerSet<?>> ls = listeners.get(name);
	return ls != null ? ls.get(type) : null;
    }

    @Override
    public Set<AttributeType<?>> getAttributeContainerListenerTypes(final String name) {
	requireNotEmpty(name);

	read.lock();
	try {
	    final Map<AttributeType<?>, ListenerSet<?>> ls = listeners.get(name);
	    return ls != null ? new HashSet<>(ls.keySet()) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public int getAttributeCount() {
	read.lock();
	try {
	    return attributes.values().stream().mapToInt(Map::size).sum();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<String> getAttributeNames() {
	read.lock();
	try {
	    return new HashSet<>(attributes.keySet());
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<AttributeType<?>> getAttributeTypes(final String name) {
	requireNotEmpty(name);

	read.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.get(name);
	    return atrn != null ? new HashSet<>(atrn.keySet()) : Collections.emptySet();
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

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + attributes.hashCode();
	result = prime * result + listeners.hashCode();
	return result;
    }

    @Override
    public <T> T removeAttribute(final String name, final AttributeType<T> type) {
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

		@SuppressWarnings("unchecked")
		final ListenerSet<AttributeContainerListener<T>> ls = (ListenerSet<AttributeContainerListener<T>>) getAttributeContainerListeners0(
			name, type);
		if (ls != null) {
		    ls.getProxy().attributeRemoved(new AttributeContainerEvent<>(delegateContainer, name, type, prev));
		}
	    }

	    return prev;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public <T> boolean removeAttributeContainerListener(final String name, final AttributeType<T> type,
	    final AttributeContainerListener<T> listener) {
	checkNameAndType(name, type);
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    final Map<AttributeType<?>, ListenerSet<?>> lsn = listeners.get(name);
	    if (lsn == null) {
		return false;
	    }

	    @SuppressWarnings("unchecked")
	    final ListenerSet<AttributeContainerListener<T>> lst = (ListenerSet<AttributeContainerListener<T>>) lsn
		    .get(type);
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

    @Override
    public void removeAttributeContainerListeners() {
	write.lock();
	try {
	    listeners.clear();
	} finally {
	    write.unlock();
	}
    }

    @Override
    public <T> void removeAttributeContainerListeners(final String name, final AttributeType<T> type) {
	checkNameAndType(name, type);

	write.lock();
	try {
	    final Map<AttributeType<?>, ListenerSet<?>> lsn = listeners.get(name);
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

    @Override
    public void removeAttributes() {
	write.lock();
	try {
	    final Map<String, Set<AttributeType<?>>> namesToTypes = new HashMap<>();
	    attributes.entrySet().forEach(e -> {
		namesToTypes.put(e.getKey(), new HashSet<>(e.getValue().keySet()));
	    });
	    namesToTypes.forEach((n, ts) -> {
		ts.forEach(t -> {
		    removeAttribute(n, t);
		});
	    });
	} finally {
	    write.unlock();
	}
    }

    @Override
    public <T> T setAttribute(final String name, final AttributeType<T> type, final T attr) {
	checkNameAndType(name, type);
	Objects.requireNonNull(attr);

	write.lock();
	try {
	    final Map<AttributeType<?>, Object> atrn = attributes.computeIfAbsent(name, k -> new ConcurrentHashMap<>());

	    @SuppressWarnings("unchecked")
	    final T prev = (T) atrn.put(type, attr);

	    @SuppressWarnings("unchecked")
	    final ListenerSet<AttributeContainerListener<T>> ls = (ListenerSet<AttributeContainerListener<T>>) getAttributeContainerListeners0(
		    name, type);
	    if (ls != null) {
		ls.getProxy().attributeAdded(new AttributeContainerEvent<>(delegateContainer, name, type, attr, prev));
	    }

	    return prev;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public Stream<?> streamAttributes() {
	read.lock();
	try {
	    return new HashSet<>(attributes.values()).stream().flatMap(m -> m.values().stream());
	} finally {
	    read.unlock();
	}
    }
}
