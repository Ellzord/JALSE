package jalse.entities;

import jalse.listeners.EntityListener;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

class UnmodifiableDelegateEntityContainer implements EntityContainer {

    private final EntityContainer delegate;

    UnmodifiableDelegateEntityContainer(final EntityContainer delegate) {
	this.delegate = delegate;
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entity> getEntities() {
	return delegate != null ? delegate.getEntities() : Collections.emptySet();
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesAsType(final Class<T> type) {
	return delegate != null ? delegate.getEntitiesAsType(type) : Collections.emptySet();
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {
	return delegate != null ? delegate.getEntitiesOfType(type) : Collections.emptySet();
    }

    @Override
    public int getEntityCount() {
	return delegate != null ? delegate.getEntityCount() : 0;
    }

    @Override
    public Set<UUID> getEntityIDs() {
	return delegate != null ? delegate.getEntityIDs() : Collections.emptySet();
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {
	return delegate != null ? delegate.getEntityListeners() : Collections.emptySet();
    }

    @Override
    public Entity getOrNullEntity(final UUID id) {
	return delegate != null ? delegate.getOrNullEntity(id) : null;
    }

    @Override
    public void killEntities() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean killEntity(final UUID id) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entity newEntity() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Entity> T newEntity(final Class<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entity newEntity(final UUID id) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Entity> streamEntities() {
	return delegate != null ? delegate.streamEntities() : Stream.empty();
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesAsType(final Class<T> type) {
	return delegate != null ? delegate.streamEntitiesAsType(type) : Stream.empty();
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {
	return delegate != null ? delegate.streamEntitiesOfType(type) : Stream.empty();
    }
}