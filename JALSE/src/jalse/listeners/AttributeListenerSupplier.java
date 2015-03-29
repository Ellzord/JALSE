package jalse.listeners;

import jalse.attributes.AttributeType;
import jalse.entities.Entity;

import java.util.Objects;
import java.util.function.Supplier;

class AttributeListenerSupplier<T> extends EntityAdapter {

    private final AttributeType<T> type;
    private final Supplier<AttributeListener<T>> supplier;
    private final boolean deep;

    AttributeListenerSupplier(final AttributeType<T> type, final Supplier<AttributeListener<T>> supplier,
	    final boolean deep) {
	this.type = Objects.requireNonNull(type);
	this.supplier = Objects.requireNonNull(supplier);
	this.deep = deep;
    }

    @Override
    public void entityCreated(final EntityEvent event) {
	final Entity e = event.getEntity();
	if (deep) { // Recursive
	    e.addEntityListener(this);
	}
	e.addAttributeListener(type, supplier.get());
    }
}