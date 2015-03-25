package jalse.entities;

import jalse.listeners.EntityListener;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

class EntitySetContainer implements EntityContainer {

    private final EntitySet delegate;

    EntitySetContainer(final EntitySet delegate) {
	this.delegate = delegate;
    }

    @Override
    public boolean addEntityListener(final EntityListener listener) {
	return delegate.addListener(listener);
    }

    @Override
    public Set<Entity> getEntities() {
	return Collections.unmodifiableSet(delegate);
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesAsType(final Class<T> type) {
	return delegate.getAsType(type);
    }

    @Override
    public <T extends Entity> Set<T> getEntitiesOfType(final Class<T> type) {
	return delegate.getOfType(type);
    }

    @Override
    public int getEntityCount() {
	return delegate.size();
    }

    @Override
    public Set<UUID> getEntityIDs() {
	return delegate.getEntityIDs();
    }

    @Override
    public Set<? extends EntityListener> getEntityListeners() {
	return delegate.getListeners();
    }

    @Override
    public Entity getOrNullEntity(final UUID id) {
	return delegate.getEntity(id);
    }

    @Override
    public void killEntities() {
	delegate.clear();
    }

    @Override
    public boolean killEntity(final UUID id) {
	return delegate.killEntity(id);
    }

    @Override
    public Entity newEntity() {
	return delegate.newEntity();
    }

    @Override
    public <T extends Entity> T newEntity(final Class<T> type) {
	return delegate.newEntity(type);
    }

    @Override
    public Entity newEntity(final UUID id) {
	return delegate.newEntity(id);
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type) {
	return delegate.newEntity(id, type);
    }

    @Override
    public boolean removeEntityListener(final EntityListener listener) {
	return delegate.removeListener(listener);
    }

    @Override
    public Stream<Entity> streamEntities() {
	return delegate.stream();
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesAsType(final Class<T> type) {
	return delegate.streamAsType(type);
    }

    @Override
    public <T extends Entity> Stream<T> streamEntitiesOfType(final Class<T> type) {
	return delegate.streamOfType(type);
    }
}
