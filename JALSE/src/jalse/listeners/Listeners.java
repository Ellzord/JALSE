package jalse.listeners;

import jalse.attributes.AttributeType;
import jalse.attributes.NamedAttributeType;

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
    public static <T> EntityListener newAttributeListenerSupplier(final NamedAttributeType<T> namedType,
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
    public static <T> EntityListener newAttributeListenerSupplier(final String name, final AttributeType<T> type,
	    final Supplier<AttributeListener<T>> supplier) {
	return new AttributeListenerSupplier<>(name, type, supplier, false);
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
     * @return EntityLitener that adds supplied EntityLitenes to descendants.
     */
    public static EntityListener newEntityListenerSupplier(final Supplier<EntityListener> supplier) {
	return new EntityListenerSupplier(supplier, false);
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
    public static <T> EntityListener newRecursiveAttributeListenerSupplier(final NamedAttributeType<T> namedType,
	    final Supplier<AttributeListener<T>> supplier) {
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
    public static <T> EntityListener newRecursiveAttributeListenerSupplier(final String name,
	    final AttributeType<T> type, final Supplier<AttributeListener<T>> supplier) {
	return new AttributeListenerSupplier<>(name, type, supplier, true);
    }

    /**
     * Creates an EntityListener supplier that will supply an EntityListener to all newly created
     * Entities and their descendants recursively.
     *
     * @param supplier
     *            EntityListener supplier.
     * @return EntityLitener that adds supplied EntityLiteners to descendants.
     */
    public static EntityListener newRecursiveEntityListenerSupplier(final Supplier<EntityListener> supplier) {
	return new EntityListenerSupplier(supplier, true);
    }

    private Listeners() {
	throw new UnsupportedOperationException();
    }
}
