package jalse.listeners;

import jalse.attributes.Attribute;
import jalse.entities.Entity;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A utility for Event listener related functionality.
 *
 * @author Elliot Ford
 *
 * @see EntityListener
 * @see EntityEvent
 * @see AttributeListener
 * @see AttributeEvent
 *
 */
public final class Listeners {

    private static class AttributeListenerSupplier extends EntityAdapter {

	private final Supplier<AttributeListener<? extends Attribute>> supplier;
	private final boolean deep;

	private AttributeListenerSupplier(final Supplier<AttributeListener<? extends Attribute>> supplier,
		final boolean deep) {

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

    private static class EntityListenerSupplier extends EntityAdapter {

	private final Supplier<EntityListener> supplier;
	private final boolean deep;

	private EntityListenerSupplier(final Supplier<EntityListener> supplier, final boolean deep) {

	    this.supplier = Objects.requireNonNull(supplier);
	    this.deep = deep;
	}

	@Override
	public void entityCreated(final EntityEvent event) {

	    final Entity e = event.getEntity();

	    if (deep) {

		e.addEntityListener(this);
	    }

	    e.addEntityListener(supplier.get());
	}
    }

    /**
     * Creates an AttributeListener supplier that will supply an
     * AttributeListener to all newly created direct Entity descendants.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to
     *         descendants.
     */
    public static EntityListener createAttributeListenerSupplier(
	    final Supplier<AttributeListener<? extends Attribute>> supplier) {

	return new AttributeListenerSupplier(supplier, false);
    }

    /**
     * Creates an AttributeListener supplier that will supply an
     * AttributeListener to all newly created Entities and their descendants
     * recursively.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to
     *         descendants.
     */
    public static EntityListener createRecursiveAttributeListenerSupplier(
	    final Supplier<AttributeListener<? extends Attribute>> supplier) {

	return new AttributeListenerSupplier(supplier, true);
    }

    /**
     * Creates an EntityListener supplier that will supply an EntityListener to
     * all newly created Entities and their descendants recursively.
     *
     * @param supplier
     *            EntityListener supplier.
     * @return EntityLitener that adds supplied EntityLiteners to descendants.
     */
    public static EntityListener createRecursiveEntityListenerSupplier(final Supplier<EntityListener> supplier) {

	return new EntityListenerSupplier(supplier, true);
    }

    /**
     * Creates an EntityListener supplier that will supply an EntityListener to
     * all newly created direct Entity descendants.
     *
     * @param supplier
     *            EntityListener supplier.
     * @return EntityLitener that adds supplied EntityLitenes to descendants.
     */
    public static EntityListener createEntityListenerSupplier(final Supplier<EntityListener> supplier) {

	return new EntityListenerSupplier(supplier, false);
    }

    /**
     * Creates a ListenerSet for EntityListener.
     *
     * @return New EntityListener ListenerSet.
     */
    public static ListenerSet<EntityListener> createEntityListenerSet() {

	return new ListenerSet<>(EntityListener.class);
    }

    /**
     * Creates a ListenerSet for AttributeListener.
     *
     * @return New AttributeListener ListenerSet.
     */
    @SuppressWarnings("rawtypes")
    public static ListenerSet<AttributeListener> createAttributeListenerSet() {

	return new ListenerSet<>(AttributeListener.class);
    }

    private Listeners() {

	throw new UnsupportedOperationException();
    }
}
