package jalse.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import jalse.misc.ListenerSet;

/**
 * An DefaultAttributeContainer is a thread-safe implementation of {@link AttributeContainer}.<br>
 * <br>
 *
 * DefaultAttributeContainer can take a delegate AttributeContainer to supply to
 * {@link AttributeEvent}. Attribute updates will trigger these events using
 * {@link AttributeListener}.
 *
 * @author Elliot Ford
 *
 */
public class DefaultAttributeContainer implements AttributeContainer {

    /**
     * Chaining builder for DefaultAttributeContainer.
     *
     * @author Elliot Ford
     *
     */
    public static final class Builder {

	private final Map<NamedAttributeType<?>, Object> builderAttributes;
	private final Map<NamedAttributeType<?>, Set<AttributeListener<?>>> builderListeners;
	private AttributeContainer builderDelegateContainer;

	/**
	 * Creates a new builder.
	 */
	public Builder() {
	    builderAttributes = new HashMap<>();
	    builderListeners = new HashMap<>();
	    builderDelegateContainer = null;
	}

	/**
	 * Adds an attribute listener.
	 *
	 * @param namedType
	 *            Named attribute type.
	 * @param listener
	 *            Listener to add.
	 * @return This builder.
	 */
	public <T> Builder addListener(final NamedAttributeType<T> namedType, final AttributeListener<T> listener) {
	    Objects.requireNonNull(namedType);
	    Objects.requireNonNull(listener);

	    Set<AttributeListener<?>> lst = builderListeners.get(namedType);
	    if (lst == null) {
		builderAttributes.put(namedType, lst = new HashSet<>());
	    }

	    lst.add(listener);

	    return this;
	}

	/**
	 * Adds an attribute listener.
	 *
	 * @param name
	 *            Attribute name.
	 * @param type
	 *            Attribute type.
	 * @param listener
	 *            Listener to add.
	 * @return This builder.
	 */
	public <T> Builder addListener(final String name, final AttributeType<T> type,
		final AttributeListener<T> listener) {
	    return addListener(new NamedAttributeType<>(name, type), listener);
	}

	/**
	 * Builds the container.
	 *
	 * @return The new container.
	 */
	public DefaultAttributeContainer build() {
	    final DefaultAttributeContainer container = new DefaultAttributeContainer(builderAttributes,
		    builderListeners);
	    if (builderDelegateContainer != null) {
		container.setDelegateContainer(builderDelegateContainer);
	    }
	    return container;
	}

	/**
	 * Sets an attribute value.
	 *
	 * @param namedType
	 *            Named attribute type.
	 * @param value
	 *            Value to set.
	 * @return This builder.
	 */
	public <T> Builder setAttribute(final NamedAttributeType<T> namedType, final T value) {
	    Objects.requireNonNull(namedType);
	    Objects.requireNonNull(value);

	    builderAttributes.put(namedType, value);

	    return this;
	}

	/**
	 * Sets an attribute value.
	 *
	 * @param name
	 *            Attribute name.
	 * @param type
	 *            Attribute type.
	 * @param value
	 *            Value to set.
	 * @return This builder.
	 */
	public <T> Builder setAttribute(final String name, final AttributeType<T> type, final T value) {
	    return setAttribute(new NamedAttributeType<>(name, type), value);
	}

	/**
	 * Sets the delegate attribute container.
	 *
	 * @param builderDelegateContainer
	 *            Delegate container to set.
	 * @return This builder.
	 */
	public Builder setDelegateContainer(final AttributeContainer builderDelegateContainer) {
	    this.builderDelegateContainer = Objects.requireNonNull(builderDelegateContainer);
	    return this;
	}
    }

    private final Map<NamedAttributeType<?>, ListenerSet<?>> listeners;
    private final Map<NamedAttributeType<?>, Object> attributes;
    private AttributeContainer delegateContainer;
    private final Lock read;
    private final Lock write;

    /**
     * Creates a new instance of DefaultAttributeContainer with no delegate container (self).
     */
    public DefaultAttributeContainer() {
	this(null, null);
    }

    /**
     * Creates a new instance of DefaultAttributeContainer with a delegate container.
     *
     * @param delegateContainer
     *            Delegate AttributeContainer for events.
     */
    public DefaultAttributeContainer(final AttributeContainer delegateContainer) {
	this(null, null);
	setDelegateContainer(delegateContainer);
    }

    private DefaultAttributeContainer(final Map<NamedAttributeType<?>, Object> attributes,
	    final Map<NamedAttributeType<?>, Set<AttributeListener<?>>> listeners) {
	delegateContainer = this;
	this.attributes = new HashMap<>();
	this.listeners = new HashMap<>();

	final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	read = rwLock.readLock();
	write = rwLock.writeLock();
	// Add starting attributes
	if (attributes != null) {
	    this.attributes.putAll(attributes);
	}
	// Add starting listeners
	if (listeners != null) {
	    for (final Entry<NamedAttributeType<?>, Set<AttributeListener<?>>> entry : listeners.entrySet()) {
		this.listeners.put(entry.getKey(), new ListenerSet<>(AttributeListener.class, entry.getValue()));
	    }
	}
    }

    @Override
    public <T> boolean addAttributeListener(final NamedAttributeType<T> namedType,
	    final AttributeListener<T> listener) {
	Objects.requireNonNull(namedType);
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    @SuppressWarnings({ "unchecked" })
	    ListenerSet<AttributeListener<T>> lst = (ListenerSet<AttributeListener<T>>) listeners.get(namedType);

	    if (lst == null) {
		// No existing listeners
		listeners.put(namedType, lst = new ListenerSet<>(AttributeListener.class));
	    }

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
    public <T> void fireAttributeChanged(final NamedAttributeType<T> namedType) {
	Objects.requireNonNull(namedType);

	read.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final T current = (T) attributes.get(namedType);
	    if (current == null) {
		return;
	    }

	    @SuppressWarnings("unchecked")
	    final ListenerSet<AttributeListener<T>> ls = (ListenerSet<AttributeListener<T>>) listeners.get(namedType);
	    if (ls != null) {
		ls.getProxy().attributeChanged(new AttributeEvent<>(delegateContainer, namedType, current));
	    }
	} finally {
	    read.unlock();
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(final NamedAttributeType<T> namedType) {
	Objects.requireNonNull(namedType);

	read.lock();
	try {
	    return (T) attributes.get(namedType);
	} finally {
	    read.unlock();
	}
    }

    @Override
    public int getAttributeCount() {
	read.lock();
	try {
	    return attributes.size();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public <T> Set<? extends AttributeListener<T>> getAttributeListeners(final NamedAttributeType<T> namedType) {
	Objects.requireNonNull(namedType);

	read.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final Set<? extends AttributeListener<T>> ls = (Set<? extends AttributeListener<T>>) listeners
		    .get(namedType);
	    return ls != null ? new HashSet<>(ls) : Collections.emptySet();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<NamedAttributeType<?>> getAttributeListenerTypes() {
	read.lock();
	try {
	    return new HashSet<>(listeners.keySet());
	} finally {
	    read.unlock();
	}
    }

    @Override
    public Set<NamedAttributeType<?>> getAttributeTypes() {
	read.lock();
	try {
	    return new HashSet<>(attributes.keySet());
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
    public <T> T removeAttribute(final NamedAttributeType<T> namedType) {
	Objects.requireNonNull(namedType);

	write.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final T prev = (T) attributes.remove(namedType);

	    if (prev != null) {
		@SuppressWarnings("unchecked")
		final ListenerSet<AttributeListener<T>> ls = (ListenerSet<AttributeListener<T>>) listeners
			.get(namedType);
		if (ls != null) {
		    ls.getProxy().attributeRemoved(new AttributeEvent<>(delegateContainer, namedType, prev));
		}
	    }

	    return prev;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public <T> boolean removeAttributeListener(final NamedAttributeType<T> namedType,
	    final AttributeListener<T> listener) {
	Objects.requireNonNull(namedType);
	Objects.requireNonNull(listener);

	write.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final ListenerSet<AttributeListener<T>> lst = (ListenerSet<AttributeListener<T>>) listeners.get(namedType);

	    // Try and remove
	    if (lst == null || !lst.remove(listener)) {
		return false;
	    }

	    if (lst.isEmpty()) {
		// No more listeners
		listeners.remove(namedType);
	    }

	    return true;
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void removeAttributeListeners() {
	write.lock();
	try {
	    listeners.clear();
	} finally {
	    write.unlock();
	}
    }

    @Override
    public <T> void removeAttributeListeners(final NamedAttributeType<T> namedType) {
	Objects.requireNonNull(namedType);

	write.lock();
	try {
	    listeners.remove(namedType);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public void removeAttributes() {
	write.lock();
	try {
	    new ArrayList<>(attributes.keySet()).forEach(this::removeAttribute);
	} finally {
	    write.unlock();
	}
    }

    @Override
    public <T> T setAttribute(final NamedAttributeType<T> namedType, final T attr) {
	Objects.requireNonNull(namedType);
	Objects.requireNonNull(attr);

	write.lock();
	try {
	    @SuppressWarnings("unchecked")
	    final T prev = (T) attributes.put(namedType, attr);

	    @SuppressWarnings("unchecked")
	    final ListenerSet<AttributeListener<T>> ls = (ListenerSet<AttributeListener<T>>) listeners.get(namedType);
	    if (ls != null) {
		ls.getProxy().attributeAdded(new AttributeEvent<>(delegateContainer, namedType, attr, prev));
	    }

	    return prev;
	} finally {
	    write.unlock();
	}
    }

    private void setDelegateContainer(final AttributeContainer delegateContainer) {
	this.delegateContainer = Objects.requireNonNull(delegateContainer);
    }

    @Override
    public Stream<?> streamAttributes() {
	read.lock();
	try {
	    return new ArrayList<>(attributes.values()).stream();
	} finally {
	    read.unlock();
	}
    }

    @Override
    public String toString() {
	return "DefaultAttributeContainer [listeners=" + listeners + ", attributes=" + attributes + "]";
    }
}
