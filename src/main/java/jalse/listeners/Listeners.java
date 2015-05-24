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
    public static <T> ListenerSet<AttributeListener<T>> newAttributeListenerSet() {
	return new ListenerSet<>(AttributeListener.class);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created direct Entity descendants.
     *
     * @param namedType
     *            Named attribute name.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     */
    public static <T> EntityContainerListener newAttributeListenerSupplier(final NamedAttributeType<T> namedType,
	    final Supplier<AttributeListener<T>> supplier) {
	return newAttributeListenerSupplier(namedType.getName(), namedType.getType(), supplier);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created direct Entity descendants.
     *
     * @param name
     *            Attribute name.
     *
     * @param type
     *            Attribute type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     */
    public static <T> EntityContainerListener newAttributeListenerSupplier(final String name,
	    final AttributeType<T> type, final Supplier<AttributeListener<T>> supplier) {
	return new AttributeListenerSupplier<>(name, type, supplier, false);
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
     * @return EntityLitener that adds supplied EntityContainerListener to descendants.
     */
    public static EntityContainerListener newEntityContainerListenerSupplier(
	    final Supplier<EntityContainerListener> supplier) {
	return new EntityContainerListenerSupplier(supplier, false);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created Entities and their descendants recursively.
     *
     * @param namedType
     *            Named attribute type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     */
    public static <T> EntityContainerListener newRecursiveAttributeListenerSupplier(
	    final NamedAttributeType<T> namedType, final Supplier<AttributeListener<T>> supplier) {
	return newRecursiveAttributeListenerSupplier(namedType.getName(), namedType.getType(), supplier);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created Entities and their descendants recursively.
     *
     * @param name
     *            Attribute name.
     *
     * @param type
     *            Attribute type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     */
    public static <T> EntityContainerListener newRecursiveAttributeListenerSupplier(final String name,
	    final AttributeType<T> type, final Supplier<AttributeListener<T>> supplier) {
	return new AttributeListenerSupplier<>(name, type, supplier, true);
    }

    /**
     * Creates an EntityContainerListener supplier that will supply an EntityContainerListener to
     * all newly created Entities and their descendants recursively.
     *
     * @param supplier
     *            EntityContainerListener supplier.
     * @return EntityLitener that adds supplied EntityContainerListeners to descendants.
     */
    public static EntityContainerListener newRecursiveEntityContainerListenerSupplier(
	    final Supplier<EntityContainerListener> supplier) {
	return new EntityContainerListenerSupplier(supplier, true);
    }

    private Listeners() {
	throw new UnsupportedOperationException();
    }
}
