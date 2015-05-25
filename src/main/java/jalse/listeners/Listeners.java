package jalse.listeners;

import jalse.attributes.AttributeType;
import jalse.attributes.NamedAttributeType;

import java.util.function.Supplier;

/**
 * A utility for Event listener related functionality.
 *
 * @author Elliot Ford
 *
 * @see EntityContainerListener
 * @see EntityContainerEvent
 * @see AttributeContainerListener
 * @see AttributeContainerEvent
 *
 */
public final class Listeners {

    /**
     * Creates a ListenerSet for AttributeContainerListener.
     *
     * @return New AttributeContainerListener ListenerSet.
     */
    public static <T> ListenerSet<AttributeContainerListener<T>> newAttributeContainerListenerSet() {
	return new ListenerSet<>(AttributeContainerListener.class);
    }

    /**
     * Creates an AttributeContainerListener supplier that will supply an AttributeContainerListener
     * to all newly created direct Entity descendants.
     *
     * @param namedType
     *            Named attribute name.
     *
     * @param supplier
     *            AttributeContainerListener supplier.
     * @return EntityContainerListener that adds supplied AttributeContainerListeners to
     *         descendants.
     */
    public static <T> EntityContainerListener newAttributeContainerListenerSupplier(
	    final NamedAttributeType<T> namedType, final Supplier<AttributeContainerListener<T>> supplier) {
	return newAttributeContainerListenerSupplier(namedType.getName(), namedType.getType(), supplier);
    }

    /**
     * Creates an AttributeContainerListener supplier that will supply an AttributeContainerListener
     * to all newly created direct Entity descendants.
     *
     * @param name
     *            Attribute name.
     *
     * @param type
     *            Attribute type.
     *
     * @param supplier
     *            AttributeContainerListener supplier.
     * @return EntityContainerListener that adds supplied AttributeContainerListeners to
     *         descendants.
     */
    public static <T> EntityContainerListener newAttributeContainerListenerSupplier(final String name,
	    final AttributeType<T> type, final Supplier<AttributeContainerListener<T>> supplier) {
	return new AttributeContainerListenerSupplier<>(name, type, supplier, false);
    }

    /**
     * Creates a ListenerSet for EntityContainerListener.
     *
     * @return New EntityContainerListener ListenerSet.
     */
    public static ListenerSet<EntityContainerListener> newEntityContainerListenerSet() {
	return new ListenerSet<>(EntityContainerListener.class);
    }

    /**
     * Creates an EntityContainerListener supplier that will supply an EntityContainerListener to
     * all newly created direct Entity descendants.
     *
     * @param supplier
     *            EntityContainerListener supplier.
     * @return EntityContainerListener that adds supplied EntityContainerListener to descendants.
     */
    public static EntityContainerListener newEntityContainerListenerSupplier(
	    final Supplier<EntityContainerListener> supplier) {
	return new EntityContainerListenerSupplier(supplier, false);
    }

    /**
     * Creates a ListenerSet for EntityListener.
     *
     * @return New EntityListener ListenerSet.
     */
    public static ListenerSet<EntityListener> newEntityListenerSet() {
	return new ListenerSet<>(EntityListener.class);
    }

    /**
     * Creates an EntityListener supplier that will supply an EntityListener to all newly created
     * direct Entity descendants.
     *
     * @param supplier
     *            EntityListener supplier.
     * @return EntityContainerListener that adds supplied EntityListener to descendants.
     */
    public static EntityContainerListener newEntityListenerSupplier(final Supplier<EntityListener> supplier) {
	return new EntityListenerSupplier(supplier, false);
    }

    /**
     * Creates an AttributeContainerListener supplier that will supply an AttributeContainerListener
     * to all newly created Entities and their descendants recursively.
     *
     * @param namedType
     *            Named attribute type.
     *
     * @param supplier
     *            AttributeContainerListener supplier.
     * @return EntityContainerListener that adds supplied AttributeContainerListeners to
     *         descendants.
     */
    public static <T> EntityContainerListener newRecursiveAttributeContainerListenerSupplier(
	    final NamedAttributeType<T> namedType, final Supplier<AttributeContainerListener<T>> supplier) {
	return newRecursiveAttributeContainerListenerSupplier(namedType.getName(), namedType.getType(), supplier);
    }

    /**
     * Creates an AttributeContainerListener supplier that will supply an AttributeContainerListener
     * to all newly created Entities and their descendants recursively.
     *
     * @param name
     *            Attribute name.
     *
     * @param type
     *            Attribute type.
     *
     * @param supplier
     *            AttributeContainerListener supplier.
     * @return EntityContainerListener that adds supplied AttributeContainerListeners to
     *         descendants.
     */
    public static <T> EntityContainerListener newRecursiveAttributeContainerListenerSupplier(final String name,
	    final AttributeType<T> type, final Supplier<AttributeContainerListener<T>> supplier) {
	return new AttributeContainerListenerSupplier<>(name, type, supplier, true);
    }

    /**
     * Creates an EntityContainerListener supplier that will supply an EntityContainerListener to
     * all newly created Entities and their descendants recursively.
     *
     * @param supplier
     *            EntityContainerListener supplier.
     * @return EntityContainerListener that adds supplied EntityContainerListeners to descendants.
     */
    public static EntityContainerListener newRecursiveEntityContainerListenerSupplier(
	    final Supplier<EntityContainerListener> supplier) {
	return new EntityContainerListenerSupplier(supplier, true);
    }

    /**
     * Creates an EntityListener supplier that will supply an EntityListener to all newly created
     * Entities and their descendants recursively.
     *
     * @param supplier
     *            EntityListener supplier.
     * @return EntityContainerListener that adds supplied EntityListeners to descendants.
     */
    public static EntityContainerListener newRecursiveEntityListenerSupplier(final Supplier<EntityListener> supplier) {
	return new EntityListenerSupplier(supplier, true);
    }

    private Listeners() {
	throw new UnsupportedOperationException();
    }
}
