package jalse.entities;

import java.util.Objects;
import java.util.function.Supplier;

class RecursiveEntityListener implements EntityListener {

    private final Supplier<EntityListener> supplier;
    private final int depth;

    RecursiveEntityListener(final Supplier<EntityListener> supplier, final int depth) {
	this.supplier = Objects.requireNonNull(supplier);
	this.depth = depth;
    }

    /**
     * Add listener from supplier and recursive listener to created entity.
     *
     * @param event
     *            The entity event for this trigger.
     */
    @Override
    public void entityCreated(final EntityEvent event) {
	if (depth > 0) {
	    event.getEntity().addEntityListener(supplier.get());
	    event.getEntity().addEntityListener(new RecursiveEntityListener(supplier, depth - 1));
	}
    }
}
