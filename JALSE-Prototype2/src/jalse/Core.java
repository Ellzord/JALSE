package jalse;

import static jalse.misc.JALSEExceptions.INVALID_ATTRIBUTE_TYPE;
import static jalse.misc.JALSEExceptions.NOT_ATTACHED;
import static jalse.misc.JALSEExceptions.throwRE;
import jalse.actions.Action;
import jalse.actions.Scheduler;
import jalse.attributes.Attributable;
import jalse.attributes.Attribute;
import jalse.listeners.AttributeListener;
import jalse.listeners.ListenerSet;
import jalse.misc.Identifiable;
import jalse.tags.Tag;
import jalse.tags.TagSet;
import jalse.tags.Taggable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Core can have its own {@link Attribute}, {@link Tag} and it can be attached
 * to {@link Engine}. {@link Action} can be performed on core and it also holds
 * its own tasks for cancellation. Core also offers {@link AttributeListener} so
 * on the change of {@link Attribute} trigger code can be added.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Engine type attached to core.
 * @param <S>
 *            The subclass of core that actions can be performed on.
 *
 * @see Attributable
 * @see Taggable
 * @see Scheduler
 */
public abstract class Core<T extends Engine, S> implements Identifiable, Attributable, Taggable, Scheduler<S> {

    /**
     * Validates whether the class is not null and a subclass of
     * {@link Attribute}.
     *
     * @param clazz
     *            Class to validate.
     * @return Whether the class is a subclass of {@link Attribute}.
     */
    protected static Class<?> requireNonNullAttrSub(final Class<?> clazz) {

	if (clazz == null || !Attribute.class.isAssignableFrom(clazz) || Attribute.class.equals(clazz)) {

	    throwRE(INVALID_ATTRIBUTE_TYPE);
	}

	return clazz;
    }

    /**
     * Unique identifier of core.
     */
    protected final UUID id;

    /**
     * Current attached engine.
     */
    protected T engine;

    /**
     * Current state information.
     */
    protected final TagSet tags;

    private final Map<Class<?>, Attribute> attributes;
    private final Map<Class<?>, ListenerSet<AttributeListener<?>>> listeners;
    private final Set<UUID> tasks;

    /**
     * Creates a new instance of core.
     *
     * @param id
     *            Unique identifier.
     * @param engine
     *            engine to attach to.
     */
    protected Core(final UUID id, final T engine) {

	this.id = id;

	attach(engine);

	tasks = Collections.newSetFromMap(new WeakHashMap<>());

	attributes = new ConcurrentHashMap<>();
	listeners = new HashMap<>();
	tags = new TagSet();
    }

    /**
     * Detaches from the engine.
     */
    protected void detatch() {

	engine = null;
    }

    /**
     * Attaches to an engine.
     *
     * @param engine
     *            Engine to attach to.
     */
    protected void attach(final T engine) {

	this.engine = engine;
    }

    /**
     * Whether core is attached to an engine.
     *
     * @return Engine attachment.
     */
    protected boolean isAttached() {

	return engine != null;
    }

    @Override
    public <U extends Attribute> boolean addListener(final Class<U> attr, final AttributeListener<U> listener) {

	return addListener0(attr, listener);
    }

    @SuppressWarnings("unchecked")
    boolean addListener0(final Class<?> attr, final Object listener) {

	ListenerSet<AttributeListener<?>> ls;

	synchronized (listeners) {

	    ls = listeners.get(requireNonNullAttrSub(attr));

	    if (ls == null) {

		/*
		 * Defeating type erasure..
		 */
		final Class<?> listenerClazz = AttributeListener.class;
		listeners.put(attr, ls = new ListenerSet<>((Class<AttributeListener<?>>) listenerClazz));
	    }
	}

	return ls.add((AttributeListener<?>) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> Optional<U> associate(final U attr) {

	final Class<?> type = attr.getClass();

	final U previous = (U) attributes.put(type, attr);

	if (previous != null) {

	    fireAttributeRemoved(previous);
	}

	ListenerSet<? extends AttributeListener<U>> ls;

	synchronized (listeners) {

	    ls = (ListenerSet<? extends AttributeListener<U>>) listeners.get(type);
	}

	if (ls != null) {

	    ls.getProxy().attributeAdded(attr);
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public boolean cancel(final UUID action) {

	if (!isAttached()) {

	    throwRE(NOT_ATTACHED);
	}

	return engine.cancel(action);
    }

    /**
     * Cancels all active/unscheduled tasks by core.
     */
    public void cancelTasks() {

	synchronized (tasks) {

	    for (final UUID id : tasks) {

		cancel(id);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> Optional<U> disassociate(final Class<U> attr) {

	final U previous = (U) attributes.remove(requireNonNullAttrSub(attr));

	if (previous != null) {

	    fireAttributeRemoved(previous);
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public boolean equals(final Object obj) {

	return obj instanceof Identifiable && Identifiable.equals(this, (Identifiable) obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> boolean fireAttributeChanged(final Class<U> attr) {

	final Optional<U> op = getOfType(attr);

	op.ifPresent(a -> {

	    ListenerSet<? extends AttributeListener<U>> ls;

	    synchronized (listeners) {

		ls = (ListenerSet<? extends AttributeListener<U>>) listeners.get(attr);
	    }

	    if (ls != null) {

		ls.getProxy().attributeChanged(a);
	    }
	});

	return op.isPresent();
    }

    @SuppressWarnings("unchecked")
    private <U extends Attribute> void fireAttributeRemoved(final U attr) {

	ListenerSet<? extends AttributeListener<U>> ls;

	synchronized (listeners) {

	    ls = (ListenerSet<? extends AttributeListener<U>>) listeners.get(attr.getClass());
	}

	if (ls != null) {

	    ls.getProxy().attributeRemoved(attr);
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> Optional<U> getOfType(final Class<U> attr) {

	return Optional.ofNullable((U) attributes.get(requireNonNullAttrSub(attr)));
    }

    @Override
    public UUID getID() {

	return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> Set<AttributeListener<U>> getListeners(final Class<U> attr) {

	Set<AttributeListener<?>> ls;

	synchronized (listeners) {

	    ls = listeners.get(requireNonNullAttrSub(requireNonNullAttrSub(attr)));
	}

	return ls != null ? Collections.unmodifiableSet((Set<? extends AttributeListener<U>>) ls) : Collections
		.emptySet();
    }

    @Override
    public Set<Tag> getTags() {

	return Collections.unmodifiableSet(tags);
    }

    @Override
    public int hashCode() {

	return Objects.hash(id);
    }

    @Override
    public boolean isActive(final UUID action) {

	if (!isAttached()) {

	    throwRE(NOT_ATTACHED);
	}

	return engine.isActive(action);
    }

    @Override
    public <U extends Attribute> boolean removeListener(final Class<U> attr, final AttributeListener<U> listener) {

	return removeListener0(attr, listener);
    }

    boolean removeListener0(final Class<?> attr, final Object listener) {

	Set<AttributeListener<?>> ls;

	synchronized (listeners) {

	    ls = listeners.get(requireNonNullAttrSub(attr));
	}

	boolean removed = false;

	if (ls != null) {

	    removed = ls.remove(listener);

	    if (ls.isEmpty()) {

		synchronized (listeners) {

		    listeners.remove(attr);
		}
	    }
	}

	return removed;
    }

    @Override
    public UUID schedule(final Action<S> action, final long initialDelay, final long period, final TimeUnit unit) {

	if (!isAttached()) {

	    throwRE(NOT_ATTACHED);
	}

	UUID task;

	synchronized (tasks) {

	    tasks.add(task = engine.schedule0(action, this, initialDelay, period, unit));
	}

	return task;
    }

    @Override
    public String toString() {

	return toString(this);
    }
}