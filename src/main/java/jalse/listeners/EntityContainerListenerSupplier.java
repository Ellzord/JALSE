package jalse.listeners;

import jalse.entities.Entity;

import java.util.Objects;
import java.util.function.Supplier;

class EntityContainerListenerSupplier implements EntityContainerListener {

    private final Supplier<EntityContainerListener> supplier;
    private final boolean deep;

    EntityContainerListenerSupplier(final Supplier<EntityContainerListener> supplier, final boolean deep) {
	this.supplier = Objects.requireNonNull(supplier);
	this.deep = deep;
    }

    @Override
    public void entityCreated(final EntityContainerEvent event) {
	final Entity e = event.getEntity();
	if (deep) { // Recursive
	    e.addEntityContainerListener(this);
	}
	e.addEntityContainerListener(supplier.get());
    }
}
