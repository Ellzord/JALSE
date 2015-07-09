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

    @Override
    public void entityCreated(final EntityEvent event) {
	if (depth > 0) {
	    final Entity e = event.getEntity();
	    e.addEntityListener(supplier.get());
	    e.addEntityListener(new RecursiveEntityListener(supplier, depth - 1));
	}
    }
}
