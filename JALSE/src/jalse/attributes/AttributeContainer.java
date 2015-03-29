package jalse.attributes;

import static jalse.attributes.Attributes.newType;
import jalse.listeners.AttributeListener;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is an attribute collection. This attribute collections works more like a set but using the
 * {@link AttributeType} to determine uniqueness (only one of each attribute type can be added).
 * {@link AttributeListener} can be added for an attribute type, trigger code will fire upon add,
 * update or removal of attributes of that type. Each collection manipulation method returns
 * {@code Optional} of the attribute (may be empty if none matching are found).
 *
 * @author Elliot Ford
 *
 * @see Optional
 * @see AttributeSet
 * @see Attributes#emptyAttributeContainer()
 * @see Attributes#unmodifiableAttributeContainer(AttributeContainer)
 *
 */
public interface AttributeContainer {

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    <T> boolean addAttributeListener(AttributeType<T> type, AttributeListener<T> listener);

    /**
     * This is a convenience method for adding an attribute (optional).
     *
     * @param type
     *            Attribute type.
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty optional if none
     *         found
     */
    default <T> Optional<T> addAttributeOfType(final AttributeType<T> type, final T attr) {
	return Optional.ofNullable(addOrNullAttributeOfType(type, attr));
    }

    /**
     * This is a convenience method for adding an attribute (optional).
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty optional if none
     *         found
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> Optional<T> addAttributeOfType(final String name, final Class<T> type, final T attr) {
	return addAttributeOfType(newType(name, type), attr);
    }

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param type
     *            Attribute type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    <T> T addOrNullAttributeOfType(final AttributeType<T> type, T attr);

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> T addOrNullAttributeOfType(final String name, final Class<T> type, final T attr) {
	return addOrNullAttributeOfType(newType(name, type), attr);
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param type
     *            Attribute type to fire for.
     */
    <T> void fireAttributeChanged(AttributeType<T> type);

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> void fireAttributeChanged(final String name, final Class<T> type) {
	fireAttributeChanged(newType(name, type));
    }

    /**
     * Gets the number of total attributes within the container.
     *
     * @return Attribute count.
     */
    int getAttributeCount();

    /**
     * Gets all the attribute listeners.
     *
     * @return Set of attribute listeners or an empty set if none were found.
     */
    Set<? extends AttributeListener<?>> getAttributeListeners();

    /**
     * Gets all attribute listeners associated to the supplied attribute type.
     *
     * @param type
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    <T> Set<? extends AttributeListener<T>> getAttributeListeners(AttributeType<T> type);

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    Set<AttributeType<?>> getAttributeListenerTypes();

    /**
     * This is a convenience method for getting an attribute (optional).
     *
     *
     * @param type
     *            Attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     */
    default <T> Optional<T> getAttributeOfType(final AttributeType<T> type) {
	return Optional.ofNullable(getOrNullAttributeOfType(type));
    }

    /**
     * This is a convenience method for getting an attribute (optional).
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @return Optional containing the attribute or else empty optional if none found.
     * @see Attributes#newType(String, Class)
     */
    default <T> Optional<T> getAttributeOfType(final String name, final Class<T> type) {
	return getAttributeOfType(newType(name, type));
    }

    /**
     * Gets all of the attributes within the container.
     *
     * @return All of the attributes or an empty set if none were found.
     */
    Set<?> getAttributes();

    /**
     * Gets all of the attribute types within the container.
     *
     * @return All of the types of the attributes or an empty set if none were found.
     */
    Set<AttributeType<?>> getAttributeTypes();

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param type
     *            Attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    <T> T getOrNullAttributeOfType(final AttributeType<T> type);

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @return The attribute matching the supplied type or null if none found.
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> T getOrNullAttributeOfType(final String name, final Class<T> type) {
	return getOrNullAttributeOfType(newType(name, type));
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param type
     *            Attribute type.
     * @return Whether the attribute was found.
     */
    default <T> boolean hasAttributeOfType(final AttributeType<T> type) {
	return getOrNullAttributeOfType(type) != null;
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @return Whether the attribute was found.
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> boolean hasAttributeOfType(final String name, final Class<T> type) {
	return getOrNullAttributeOfType(name, type) != null;
    }

    /**
     * Checks whether the container has any attributes.
     *
     * @return Is the container is not empty.
     */
    default boolean hasAttributes() {
	return getAttributeCount() > 0;
    }

    /**
     * Removes all listeners for all attribute types.
     */
    void removeAllAttributeListeners();

    /**
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    <T> boolean removeAttributeListener(AttributeType<T> type, AttributeListener<T> listener);

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param attr
     *            Attribute type.
     */
    <T> void removeAttributeListeners(AttributeType<T> attr);

    /**
     * This is a convenience method for removing an attribute (no optional).
     *
     * @param type
     *            Attribute type to remove.
     * @return Optional containing the removed attribute or else empty optional if none found
     */
    default <T> Optional<T> removeAttributeOfType(final AttributeType<T> type) {
	return Optional.ofNullable(removeOrNullAttributeOfType(type));
    }

    /**
     * This is a convenience method for removing an attribute (no optional).
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @return Optional containing the removed attribute or else empty optional if none found
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> Optional<T> removeAttributeOfType(final String name, final Class<T> type) {
	return removeAttributeOfType(newType(name, type));
    }

    /**
     * Removes all attributes within the container (firing removal events).
     */
    void removeAttributes();

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param type
     *            Attribute type to remove.
     * @return The removed attribute or null if none was removed.
     */
    <T> T removeOrNullAttributeOfType(final AttributeType<T> type);

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Value type.
     * @return The removed attribute or null if none was removed.
     *
     * @see Attributes#newType(String, Class)
     */
    default <T> T removeOrNullAttributeOfType(final String name, final Class<T> type) {
	return removeOrNullAttributeOfType(newType(name, type));
    }

    /**
     * Streams all of the attributes within the container.
     *
     * @return Stream of all attributes.
     */
    Stream<?> streamAttributes();
}
