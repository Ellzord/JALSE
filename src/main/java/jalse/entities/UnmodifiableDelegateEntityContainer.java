package jalse.entities;

import jalse.attributes.AttributeContainer;
import jalse.listeners.EntityContainerListener;

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
    public boolean addEntityContainerListener(final EntityContainerListener listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entity getEntity(final UUID id) {
	return delegate != null ? delegate.getEntity(id) : null;
    }

    @Override
    public Set<? extends EntityContainerListener> getEntityContainerListeners() {
	return delegate != null ? delegate.getEntityContainerListeners() : Collections.emptySet();
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
    public void killEntities() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean killEntity(final UUID id) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entity newEntity(final UUID id, final AttributeContainer attributes) {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Entity> T newEntity(final UUID id, final Class<T> type, final AttributeContainer attributes) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean receiveEntity(final Entity e) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEntityContainerListener(final EntityContainerListener listener) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void removeEntityContainerListeners() {
	throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Entity> streamEntities() {
	return delegate != null ? delegate.streamEntities() : Stream.empty();
    }

    @Override
    public boolean transferEntity(final UUID id, final EntityContainer destination) {
	throw new UnsupportedOperationException();
    }
}