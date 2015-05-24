package jalse.listeners;

import jalse.entities.Entity;

import java.util.Objects;
import java.util.function.Supplier;

class EntityListenerSupplier implements EntityContainerListener {

    private final Supplier<EntityListener> supplier;
    private final boolean deep;

    EntityListenerSupplier(final Supplier<EntityListener> supplier, final boolean deep) {
	this.supplier = Objects.requireNonNull(supplier);
	this.deep = deep;
    }

    @Override
    public void entityCreated(final EntityContainerEvent event) {
	final Entity e = event.getEntity();
	if (deep) { // Recursive
	    e.addEntityContainerListener(this);
	}
	e.addEntityListener(supplier.get());
    }
}
