package jalse.attributes;

import static jalse.attributes.Attributes.requireNonNullAttrSub;
import static jalse.misc.TypeParameterResolver.toClass;
import jalse.listeners.AttributeEvent;
import jalse.listeners.AttributeListener;
import jalse.listeners.ListenerSet;
import jalse.listeners.Listeners;
import jalse.misc.TypeParameterResolver;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<Class<? extends Attribute>, ListenerSet<AttributeListener>> attributeListeners;
    private final Map<Class<? extends Attribute>, Attribute> attributes;
    private final AttributeContainer delegateContainer;

    /**
     * Creates a new AttributeSet with an empty delegate container.
     */
    public AttributeSet() {
	this(Attributes.emptyAttributeContainer());
    }

    /**
     * Creates a new AttributeSet with the supplied delegate container.
     *
     * @param delegateContainer
     *            Delegate container for events.
     */
    public AttributeSet(final AttributeContainer delegateContainer) {
	this.delegateContainer = Objects.requireNonNull(delegateContainer);
	attributes = new ConcurrentHashMap<>();
	attributeListeners = new HashMap<>();
    }

    @Override
    public boolean add(final Attribute e) {
	return addOfType(e).isPresent();
    }

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    @SuppressWarnings("unchecked")
    public boolean addListener(final AttributeListener<? extends Attribute> listener) {
	final Class<?> attr = toClass(RESOLVER.resolve(listener));

	ListenerSet<AttributeListener> ls;
	synchronized (attributeListeners) {
	    ls = attributeListeners.get(requireNonNullAttrSub(attr));
	    if (ls == null) {
		attributeListeners.put((Class<? extends Attribute>) attr, ls = Listeners.createAttributeListenerSet());
	    }
	}

	return ls.add(listener);
    }

    /**
     * Adds the specified attribute to the collection.
     *
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty optional if none
     *         found
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> Optional<T> addOfType(final T attr) {
	final Class<? extends Attribute> type = attr.getClass();

	final T previous = (T) attributes.put(type, attr);

	ListenerSet<? extends AttributeListener<T>> ls;
	synchronized (attributeListeners) {
	    ls = (ListenerSet<? extends AttributeListener<T>>) attributeListeners.get(type);
	}

	if (ls != null) {
	    ls.getProxy().attributeAdded(new AttributeEvent<>(delegateContainer, attr, previous));
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public void clear() {
	new HashSet<>(attributes.values()).forEach(this::remove);
    }

    @Override
    public boolean contains(final Object o) {
	if (!(o instanceof Attribute)) {
	    throw new IllegalArgumentException();
	}
	return Objects.equals(o, attributes.get(o.getClass()));
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
	final Optional<T> op = getOfType(attr);

	op.ifPresent(a -> {
	    ListenerSet<? extends AttributeListener<T>> ls;
	    synchronized (attributeListeners) {
		ls = (ListenerSet<? extends AttributeListener<T>>) attributeListeners.get(attr);
	    }
	    if (ls != null) {
		ls.getProxy().attributeChanged(new AttributeEvent<>(delegateContainer, a));
	    }
	});

	return op.isPresent();
    }

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<Class<? extends Attribute>> getAttributeListenerTypes() {
	return Collections.unmodifiableSet(attributeListeners.keySet());
    }

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    public Set<Class<? extends Attribute>> getAttributeTypes() {
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
    public Set<? extends AttributeListener<? extends Attribute>> getListeners() {
	return attributeListeners.values().stream().flatMap(a -> a.stream()).collect(Collectors.toSet());
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
	Set<AttributeListener> ls;
	synchronized (attributeListeners) {
	    ls = attributeListeners.get(requireNonNullAttrSub(attr));
	}
	return ls != null ? Collections.unmodifiableSet((Set<? extends AttributeListener<T>>) ls) : Collections
		.emptySet();
    }

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> Optional<T> getOfType(final Class<T> attr) {
	return Optional.ofNullable((T) attributes.get(requireNonNullAttrSub(attr)));
    }

    @Override
    public boolean isEmpty() {
	return attributes.isEmpty();
    }

    @Override
    public Iterator<Attribute> iterator() {
	return attributes.values().iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(final Object o) {
	if (!(o instanceof Attribute)) {
	    throw new IllegalArgumentException();
	}
	return removeOfType((Class<? extends Attribute>) o.getClass()).isPresent();
    }

    /**
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    public <T extends Attribute> boolean removeListener(final AttributeListener<T> listener) {
	final Class<?> attr = toClass(RESOLVER.resolve(listener));

	Set<AttributeListener> ls;
	synchronized (attributeListeners) {
	    ls = attributeListeners.get(requireNonNullAttrSub(attr));
	}

	boolean removed = false;

	if (ls != null) {
	    removed = ls.remove(listener);
	    if (ls.isEmpty()) {
		synchronized (attributeListeners) {
		    attributeListeners.remove(attr);
		}
	    }
	}

	return removed;
    }

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to remove.
     * @return Optional containing the removed attribute or else empty optional if none found
     */
    @SuppressWarnings("unchecked")
    public <T extends Attribute> Optional<T> removeOfType(final Class<T> attr) {
	final T previous = (T) attributes.remove(requireNonNullAttrSub(attr));

	if (previous != null) {
	    ListenerSet<? extends AttributeListener<T>> ls;
	    synchronized (attributeListeners) {
		ls = (ListenerSet<? extends AttributeListener<T>>) attributeListeners.get(attr);
	    }
	    if (ls != null) {
		ls.getProxy().attributeRemoved(new AttributeEvent<>(delegateContainer, previous));
	    }
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public int size() {
	return attributes.size();
    }

}
