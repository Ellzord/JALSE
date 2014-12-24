package org.jalse;

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

import org.jalse.actions.Action;
import org.jalse.actions.Scheduler;
import org.jalse.attributes.Attributable;
import org.jalse.attributes.Attribute;
import org.jalse.listeners.AttributeListener;
import org.jalse.misc.JALSEException;
import org.jalse.tags.Tag;
import org.jalse.tags.Taggable;
import org.jalse.wrappers.AgentWrapper;

abstract class Core<T> implements Attributable, Taggable, Scheduler<T> {

    static Class<?> requireNonNullAttrSub(final Class<?> clazz) {

	if (Attribute.class.equals(Objects.requireNonNull(clazz))) {

	    throw JALSEException.INVALID_ATTRIBUTE_CLASS.get();
	}

	return clazz;
    }

    private final Map<Class<?>, Attribute> attributes;
    protected UUID id;
    protected JALSE jalse;
    private final Map<Class<?>, Set<AttributeListener<?>>> listeners;
    private final Set<Tag> tags;
    private final Set<UUID> tasks;

    Core(final JALSE jalse, final UUID id) {

	this.jalse = jalse;
	this.id = id;

	tasks = Collections.newSetFromMap(new WeakHashMap<>());

	attributes = new ConcurrentHashMap<>();
	listeners = new HashMap<>();
	tags = new CopyOnWriteArraySet<>();
    }

    @Override
    public <S extends Attribute> boolean addListener(final Class<S> attr, final AttributeListener<S> listener) {

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
    public <S extends Attribute> Optional<S> associate(final S attr) {

	final Class<?> clazz = attr.getClass();

	final S previous = (S) attributes.put(clazz, attr);

	if (previous != null) {

	    fireAttributeRemoved(previous);
	}

	Set<? extends AttributeListener<S>> ls;

	synchronized (listeners) {

	    ls = (Set<? extends AttributeListener<S>>) listeners.get(clazz);
	}

	if (ls != null) {

	    for (final AttributeListener<S> l : ls) {

		l.attributeAdded(attr);
	    }
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public boolean cancel(final UUID action) {

	return jalse.cancel(action);
    }

    void cancelTasks() {

	synchronized (tasks) {

	    for (final UUID id : tasks) {

		cancel(id);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Attribute> Optional<S> disassociate(final Class<S> attr) {

	final S previous = (S) attributes.remove(requireNonNullAttrSub(attr));

	if (previous != null) {

	    fireAttributeRemoved(previous);
	}

	return Optional.ofNullable(previous);
    }

    @Override
    public boolean equals(final Object obj) {

	return this == obj || obj != null && obj.getClass().equals(getClass())
		&& Objects.equals(id, ((AgentWrapper) obj).getID());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Attribute> boolean fireAttributeChanged(final Class<S> attr) {

	final Optional<S> op = getAttribute(attr);

	op.ifPresent(a -> {

	    Set<? extends AttributeListener<S>> ls;

	    synchronized (listeners) {

		ls = (Set<? extends AttributeListener<S>>) listeners.get(attr);
	    }

	    if (ls != null) {

		for (final AttributeListener<S> l : ls) {

		    l.attributeChanged(a);
		}
	    }
	});

	return op.isPresent();
    }

    @SuppressWarnings("unchecked")
    private <S extends Attribute> void fireAttributeRemoved(final S attr) {

	Set<? extends AttributeListener<S>> ls;

	synchronized (listeners) {

	    ls = (Set<? extends AttributeListener<S>>) listeners.get(attr.getClass());
	}

	if (ls != null) {

	    for (final AttributeListener<S> l : ls) {

		l.attributeRemoved(attr);
	    }
	}
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Attribute> Optional<S> getAttribute(final Class<S> attr) {

	return Optional.ofNullable((S) attributes.get(requireNonNullAttrSub(attr)));
    }

    public UUID getID() {

	return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends Attribute> Set<AttributeListener<S>> getListeners(final Class<S> attr) {

	Set<AttributeListener<?>> ls;

	synchronized (listeners) {

	    ls = listeners.get(requireNonNullAttrSub(requireNonNullAttrSub(attr)));
	}

	return ls != null ? Collections.unmodifiableSet((Set<? extends AttributeListener<S>>) ls) : Collections
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

	return jalse.isActive(action);
    }

    @Override
    public <S extends Attribute> boolean removeListener(final Class<S> attr, final AttributeListener<S> listener) {

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
    public UUID schedule(final Action<T> action) {

	return schedule(action, 0L, TimeUnit.NANOSECONDS);
    }

    @Override
    public UUID schedule(final Action<T> action, final long initialDelay, final long period, final TimeUnit unit) {

	UUID task;

	synchronized (tasks) {

	    tasks.add(task = jalse.schedule0(action, this, initialDelay, period, unit));
	}

	return task;
    }

    @Override
    public UUID schedule(final Action<T> action, final long initialDelay, final TimeUnit unit) {

	return schedule(action, initialDelay, 0L, unit);
    }
}