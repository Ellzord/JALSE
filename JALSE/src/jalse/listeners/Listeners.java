package jalse.listeners;

import jalse.attributes.Attribute;

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

    /**
     * Creates a ListenerSet for AttributeListener.
     *
     * @return New AttributeListener ListenerSet.
     */
    @SuppressWarnings("rawtypes")
    public static ListenerSet<AttributeListener> createAttributeListenerSet() {

	return new ListenerSet<>(AttributeListener.class);
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
     * Creates a ListenerSet for EntityListener.
     *
     * @return New EntityListener ListenerSet.
     */
    public static ListenerSet<EntityListener> createEntityListenerSet() {

	return new ListenerSet<>(EntityListener.class);
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

    private Listeners() {

	throw new UnsupportedOperationException();
    }
}
