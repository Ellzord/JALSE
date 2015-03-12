package jalse.listeners;

import jalse.attributes.Attribute;
import jalse.entities.Entity;

import java.util.Objects;
import java.util.function.Supplier;

class AttributeListenerSupplier extends EntityAdapter {

    private final Supplier<AttributeListener<? extends Attribute>> supplier;
    private final boolean deep;

    AttributeListenerSupplier(final Supplier<AttributeListener<? extends Attribute>> supplier, final boolean deep) {
	this.supplier = Objects.requireNonNull(supplier);
	this.deep = deep;
    }

    @Override
    public void entityCreated(final EntityEvent event) {
	final Entity e = event.getEntity();
	if (deep) {
	    e.addEntityListener(this);
	}
	e.addAttributeListener(supplier.get());
    }
}