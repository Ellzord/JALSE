package jalse;

import jalse.actions.Action;
import jalse.actions.Scheduler;
import jalse.agents.Agent;
import jalse.attributes.Attributable;
import jalse.attributes.Attribute;
import jalse.listeners.AttributeListener;
import jalse.misc.Identifiable;
import jalse.misc.JALSEExceptions;
import jalse.tags.Tag;
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
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public abstract class Core<T extends Engine, S> implements Identifiable, Attributable, Taggable, Scheduler<S> {

    protected static Class<?> requireNonNullAttrSub(final Class<?> clazz) {

	if (Attribute.class.equals(Objects.requireNonNull(clazz))) {

	    throw JALSEExceptions.INVALID_ATTRIBUTE_CLASS.get();
	}

	return clazz;
    }

    private final Map<Class<?>, Attribute> attributes;
    protected UUID id;
    protected T engine;
    private final Map<Class<?>, Set<AttributeListener<?>>> listeners;
    private final Set<Tag> tags;
    private final Set<UUID> tasks;

    protected Core(final T jalse, final UUID id) {

	this.engine = jalse;
	this.id = id;

	tasks = Collections.newSetFromMap(new WeakHashMap<>());

	attributes = new ConcurrentHashMap<>();
	listeners = new HashMap<>();
	tags = new CopyOnWriteArraySet<>();
    }

    @Override
    public <U extends Attribute> boolean addListener(final Class<U> attr, final AttributeListener<U> listener) {

	return addListener0(attr, listener);
    }

    boolean addListener0(final Class<?> attr, final Object listener) {

	Set<AttributeListener<?>> ls;

	synchronized (listeners) {

	    ls = listeners.get(requireNonNullAttrSub(attr));

	    if (ls == null) {

		listeners.put(attr, ls = new CopyOnWriteArraySet<>());
	    }
	}

	return ls.add((AttributeListener<?>) listener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> Optional<U> associate(final U attr) {

	final Class<?> clazz = attr.getClass();

	final U previous = (U) attributes.put(clazz, attr);

	if (previous != null) {

	    fireAttributeRemoved(previous);
	}

	Set<? extends AttributeListener<U>> ls;

	synchronized (listeners) {

	    ls = (Set<? extends AttributeListener<U>>) listeners.get(clazz);
	}

	if (ls != null) {

	    for (final AttributeListener<U> l : ls) {

		l.attributeAdded(attr);
	    }
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public boolean cancel(final UUID action) {

	return engine.cancel(action);
    }

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

	return this == obj || obj != null && obj.getClass().equals(getClass())
		&& Objects.equals(id, ((Agent) obj).getID());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> boolean fireAttributeChanged(final Class<U> attr) {

	final Optional<U> op = getAttribute(attr);

	op.ifPresent(a -> {

	    Set<? extends AttributeListener<U>> ls;

	    synchronized (listeners) {

		ls = (Set<? extends AttributeListener<U>>) listeners.get(attr);
	    }

	    if (ls != null) {

		for (final AttributeListener<U> l : ls) {

		    l.attributeChanged(a);
		}
	    }
	});

	return op.isPresent();
    }

    @SuppressWarnings("unchecked")
    private <U extends Attribute> void fireAttributeRemoved(final U attr) {

	Set<? extends AttributeListener<U>> ls;

	synchronized (listeners) {

	    ls = (Set<? extends AttributeListener<U>>) listeners.get(attr.getClass());
	}

	if (ls != null) {

	    for (final AttributeListener<U> l : ls) {

		l.attributeRemoved(attr);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends Attribute> Optional<U> getAttribute(final Class<U> attr) {

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

    Set<Tag> getTags0() {

	return tags;
    }

    @Override
    public int hashCode() {

	return Objects.hash(id);
    }

    @Override
    public boolean isActive(final UUID action) {

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

	    if (removed) {

		synchronized (listeners) {

		    if (ls.isEmpty()) {

			listeners.remove(attr);
		    }
		}
	    }
	}

	return removed;
    }

    @Override
    public UUID schedule(final Action<S> action) {

	return schedule(action, 0L, TimeUnit.NANOSECONDS);
    }

    @Override
    public UUID schedule(final Action<S> action, final long initialDelay, final long period, final TimeUnit unit) {

	UUID task;

	synchronized (tasks) {

	    tasks.add(task = engine.schedule0(action, this, initialDelay, period, unit));
	}

	return task;
    }

    @Override
    public UUID schedule(final Action<S> action, final long initialDelay, final TimeUnit unit) {

	return schedule(action, initialDelay, 0L, unit);
    }
}