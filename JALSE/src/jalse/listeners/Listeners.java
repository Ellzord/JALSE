package jalse.listeners;

import static jalse.attributes.Attributes.newType;
import jalse.attributes.AttributeType;
import jalse.attributes.Attributes;

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
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created direct Entity descendants.
     *
     * @param type
     *            Attribute type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     */
    public static <T> EntityListener createAttributeListenerSupplier(final AttributeType<T> type,
	    final Supplier<AttributeListener<T>> supplier) {
	return new AttributeListenerSupplier<>(type, supplier, false);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created direct Entity descendants.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     *
     * @see Attributes#newType(String, Class)
     */
    public static <T> EntityListener createAttributeListenerSupplier(final String name, final Class<T> type,
	    final Supplier<AttributeListener<T>> supplier) {
	return createAttributeListenerSupplier(newType(name, type), supplier);
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
     * Creates an EntityListener supplier that will supply an EntityListener to all newly created
     * direct Entity descendants.
     *
     * @param supplier
     *            EntityListener supplier.
     * @return EntityLitener that adds supplied EntityLitenes to descendants.
     */
    public static EntityListener createEntityListenerSupplier(final Supplier<EntityListener> supplier) {
	return new EntityListenerSupplier(supplier, false);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created Entities and their descendants recursively.
     *
     * @param type
     *            Attribute type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     */
    public static <T> EntityListener createRecursiveAttributeListenerSupplier(final AttributeType<T> type,
	    final Supplier<AttributeListener<T>> supplier) {
	return new AttributeListenerSupplier<>(type, supplier, true);
    }

    /**
     * Creates an AttributeListener supplier that will supply an AttributeListener to all newly
     * created Entities and their descendants recursively.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     *
     * @param supplier
     *            AttributeListener supplier.
     * @return AttributeListener that adds supplied AttributeListeners to descendants.
     *
     * @see Attributes#newType(String, Class)
     */
    public static <T> EntityListener createRecursiveAttributeListenerSupplier(final String name, final Class<T> type,
	    final Supplier<AttributeListener<T>> supplier) {
	return createRecursiveAttributeListenerSupplier(newType(name, type), supplier);
    }

    /**
     * Creates an EntityListener supplier that will supply an EntityListener to all newly created
     * Entities and their descendants recursively.
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
