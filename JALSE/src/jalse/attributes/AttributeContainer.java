package jalse.attributes;

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
     * Adds an attribute listener for the supplied named attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    default <T> boolean addAttributeListener(final NamedAttributeType<T> namedType, final AttributeListener<T> listener) {
	return addAttributeListener(namedType.getName(), namedType.getType(), listener);
    }

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    <T> boolean addAttributeListener(String name, AttributeType<T> type, AttributeListener<T> listener);

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param namedType
     *            Named attribute type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    default <T> T addAttributeOfType(final NamedAttributeType<T> namedType, final T attr) {
	return addAttributeOfType(namedType.getName(), namedType.getType(), attr);
    }

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    <T> T addAttributeOfType(String name, final AttributeType<T> type, T attr);

    /**
     * This is a convenience method for adding an attribute (optional).
     *
     * @param namedType
     *            Named attribute type.
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty optional if none
     *         found
     *
     */
    default <T> Optional<T> addOptAttributeOfType(final NamedAttributeType<T> namedType, final T attr) {
	return addOptAttributeOfType(namedType.getName(), namedType.getType(), attr);
    }

    /**
     * This is a convenience method for adding an attribute (optional).
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type.
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty optional if none
     *         found
     */
    default <T> Optional<T> addOptAttributeOfType(final String name, final AttributeType<T> type, final T attr) {
	return Optional.ofNullable(addAttributeOfType(name, type, attr));
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param namedType
     *            Named attribute type name.
     */
    default <T> void fireAttributeChanged(final NamedAttributeType<T> namedType) {
	fireAttributeChanged(namedType.getName(), namedType.getType());
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to fire for.
     */
    <T> void fireAttributeChanged(String name, AttributeType<T> type);

    /**
     * Gets the number of total attributes within the container.
     *
     * @return Attribute count.
     */
    int getAttributeCount();

    /**
     * Gets the attribute type names with listeners associated.
     *
     * @return Associated attribute type names to listeners.
     */
    Set<String> getAttributeListenerNames();

    /**
     * Gets all attribute listeners associated to the supplied named attribute type.
     *
     * @param namedType
     *            Named attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    default <T> Set<? extends AttributeListener<T>> getAttributeListeners(final NamedAttributeType<T> namedType) {
	return getAttributeListeners(namedType.getName(), namedType.getType());
    }

    /**
     * Gets all attribute listeners associated to the supplied named attribute type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    <T> Set<? extends AttributeListener<T>> getAttributeListeners(String name, AttributeType<T> type);

    /**
     * Gets all the attribute listener types.
     *
     * @param name
     *            Attribute type name.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    Set<AttributeType<?>> getAttributeListenerTypes(String name);

    /**
     * Gets all the attribute type names assigned to attributes.
     *
     * @return Attribute type names with values.
     */
    Set<String> getAttributeNames();

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param namedType
     *            Named attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    default <T> T getAttributeOfType(final NamedAttributeType<T> namedType) {
	return getAttributeOfType(namedType.getName(), namedType.getType());
    }

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    <T> T getAttributeOfType(String name, final AttributeType<T> type);

    /**
     * Gets all of the attributes within the container.
     *
     * @return All of the attributes or an empty set if none were found.
     */
    Set<?> getAttributes();

    /**
     * Gets all of the attribute types within the container.
     *
     * @param name
     *            Attribute type name.
     *
     * @return All of the types of the attributes or an empty set if none were found.
     */
    Set<AttributeType<?>> getAttributeTypes(String name);

    /**
     * This is a convenience method for getting an attribute (optional).
     *
     *
     * @param namedType
     *            Named attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     */
    default <T> Optional<T> getOptAttributeOfType(final NamedAttributeType<T> namedType) {
	return getOptAttributeOfType(namedType.getName(), namedType.getType());
    }

    /**
     * This is a convenience method for getting an attribute (optional).
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     */
    default <T> Optional<T> getOptAttributeOfType(final String name, final AttributeType<T> type) {
	return Optional.ofNullable(getAttributeOfType(name, type));
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @return Whether the attribute was found.
     */
    default <T> boolean hasAttributeOfType(final NamedAttributeType<T> namedType) {
	return hasAttributeOfType(namedType.getName(), namedType.getType());
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     * @return Whether the attribute was found.
     */
    default <T> boolean hasAttributeOfType(final String name, final AttributeType<T> type) {
	return getAttributeOfType(name, type) != null;
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
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    default <T> boolean removeAttributeListener(final NamedAttributeType<T> namedType,
	    final AttributeListener<T> listener) {
	return removeAttributeListener(namedType.getName(), namedType.getType(), listener);
    }

    /**
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    <T> boolean removeAttributeListener(String name, AttributeType<T> type, AttributeListener<T> listener);

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param namedType
     *            Named attribute type.
     */
    default <T> void removeAttributeListeners(final NamedAttributeType<T> namedType) {
	removeAttributeListeners(namedType.getName(), namedType.getType());
    }

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     */
    <T> void removeAttributeListeners(String name, AttributeType<T> type);

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param namedType
     *            Named attribute type to remove.
     *
     * @return The removed attribute or null if none was removed.
     */
    default <T> T removeAttributeOfType(final NamedAttributeType<T> namedType) {
	return removeAttributeOfType(namedType.getName(), namedType.getType());
    }

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to remove.
     * @return The removed attribute or null if none was removed.
     */
    <T> T removeAttributeOfType(String name, final AttributeType<T> type);

    /**
     * Removes all attributes within the container (firing removal events).
     */
    void removeAttributes();

    /**
     * This is a convenience method for removing an attribute (no optional).
     *
     * @param namedType
     *            Named attribute type to remove.
     * @return Optional containing the removed attribute or else empty optional if none found
     */
    default <T> Optional<T> removeOptAttributeOfType(final NamedAttributeType<T> namedType) {
	return removeOptAttributeOfType(namedType.getName(), namedType.getType());
    }

    /**
     * This is a convenience method for removing an attribute (no optional).
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to remove.
     * @return Optional containing the removed attribute or else empty optional if none found
     */
    default <T> Optional<T> removeOptAttributeOfType(final String name, final AttributeType<T> type) {
	return Optional.ofNullable(removeAttributeOfType(name, type));
    }

    /**
     * Streams all of the attributes within the container.
     *
     * @return Stream of all attributes.
     */
    Stream<?> streamAttributes();
}
