package jalse.entities;

import java.util.UUID;

import jalse.actions.ActionEngine;

class UnmodifiableDelegateEntityFactory implements EntityFactory {

    private final EntityFactory delegate;

    UnmodifiableDelegateEntityFactory(final EntityFactory delegate) {
	this.delegate = delegate;
    }

    @Override
    public void exportEntity(final Entity e) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Entity newEntity(final UUID id, final EntityContainer target) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void setEngine(final ActionEngine engine) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryImportEntity(final Entity e, final EntityContainer target) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryKillEntity(final Entity e) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryTakeFromTree(final Entity e, final EntityContainer target) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean withinSameTree(final EntityContainer source, final EntityContainer target) {
	return delegate != null ? delegate.withinSameTree(source, target) : false;
    }

}
