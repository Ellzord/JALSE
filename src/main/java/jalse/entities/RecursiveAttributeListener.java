package jalse.entities;

import java.util.Objects;
import java.util.function.Supplier;

import jalse.attributes.AttributeListener;
import jalse.attributes.NamedAttributeType;

class RecursiveAttributeListener<T> implements EntityListener {

    private final Supplier<AttributeListener<T>> supplier;
    private final NamedAttributeType<T> namedType;
    private final int depth;

    RecursiveAttributeListener(final NamedAttributeType<T> namedType, final Supplier<AttributeListener<T>> supplier,
	    final int depth) {
	this.namedType = Objects.requireNonNull(namedType);
	this.supplier = Objects.requireNonNull(supplier);
	this.depth = depth;
    }

    /**
     * Add attribute listener from supplier and recursive listener to created entity.
     *
     * @param event
     *            The entity event for this trigger.
     */
    @Override
    public void entityCreated(final EntityEvent event) {
	if (depth > 0) {
	    event.getEntity().addAttributeListener(namedType, supplier.get());
	    event.getEntity().addEntityListener(new RecursiveAttributeListener<>(namedType, supplier, depth - 1));
	}
    }
}
