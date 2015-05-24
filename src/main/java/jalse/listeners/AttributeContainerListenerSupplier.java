package jalse.listeners;

import static jalse.attributes.Attributes.requireNotEmpty;
import jalse.attributes.AttributeType;
import jalse.entities.Entity;

import java.util.Objects;
import java.util.function.Supplier;

class AttributeContainerListenerSupplier<T> implements EntityContainerListener {

    private final String name;
    private final AttributeType<T> type;
    private final Supplier<AttributeContainerListener<T>> supplier;
    private final boolean deep;

    AttributeContainerListenerSupplier(final String name, final AttributeType<T> type,
	    final Supplier<AttributeContainerListener<T>> supplier, final boolean deep) {
	this.name = requireNotEmpty(name);
	this.type = Objects.requireNonNull(type);
	this.supplier = Objects.requireNonNull(supplier);
	this.deep = deep;
    }

    @Override
    public void entityCreated(final EntityContainerEvent event) {
	final Entity e = event.getEntity();
	if (deep) { // Recursive
	    e.addEntityContainerListener(this);
	}
	e.addAttributeContainerListener(name, type, supplier.get());
    }
}