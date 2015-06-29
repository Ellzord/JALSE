package jalse.attributes;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
 * @see DefaultAttributeContainer
 * @see Attributes#emptyAttributeContainer()
 * @see Attributes#unmodifiableAttributeContainer(AttributeContainer)
 *
 */
public interface AttributeContainer {

    /**
     * Adds all attributes and listeners from the source container.
     *
     * @param sourceContainer
     *            Source container to copy from.
     */
    default void addAll(final AttributeContainer sourceContainer) {
	addAllAttributes(sourceContainer);
	addAllAttributeListeners(sourceContainer);
    }

    /**
     * Adds all attribute listeners from the source container.
     *
     * @param sourceContainer
     *            Source attribute container.
     */
    @SuppressWarnings("unchecked")
    default void addAllAttributeListeners(final AttributeContainer sourceContainer) {
	for (final NamedAttributeType<?> namedType : sourceContainer.getAttributeListenerTypes()) {
	    for (final AttributeListener<?> listener : sourceContainer.getAttributeListeners(namedType)) {
		addAttributeListener((NamedAttributeType<Object>) namedType, (AttributeListener<Object>) listener);
	    }
	}
    }

    /**
     * Adds all attributes from the source container.
     *
     * @param sourceContainer
     *            Source attribute container.
     */
    @SuppressWarnings("unchecked")
    default void addAllAttributes(final AttributeContainer sourceContainer) {
	for (final NamedAttributeType<?> namedType : sourceContainer.getAttributeTypes()) {
	    final Object attr = sourceContainer.getAttribute(namedType);
	    if (attr != null) {
		setAttribute((NamedAttributeType<Object>) namedType, attr);
	    }
	}
    }

    /**
     * Adds an attribute listener for the supplied named attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    <T> boolean addAttributeListener(final NamedAttributeType<T> namedType, final AttributeListener<T> listener);

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
     *
     * @see #addAttributeListener(NamedAttributeType, AttributeListener)
     */
    default <T> boolean addAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return addAttributeListener(new NamedAttributeType<>(name, type), listener);
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param namedType
     *            Named attribute type name.
     */
    <T> void fireAttributeChanged(final NamedAttributeType<T> namedType);

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to fire for.
     *
     * @see #fireAttributeChanged(NamedAttributeType)
     */
    default <T> void fireAttributeChanged(final String name, final AttributeType<T> type) {
	fireAttributeChanged(new NamedAttributeType<>(name, type));
    }

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param namedType
     *            Named attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    <T> T getAttribute(final NamedAttributeType<T> namedType);

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     *
     * @see #getAttribute(NamedAttributeType)
     */
    default <T> T getAttribute(final String name, final AttributeType<T> type) {
	return getAttribute(new NamedAttributeType<>(name, type));
    }

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
     *
     * @see #getAttributeListenerTypes()
     */
    default Set<String> getAttributeListenerNames() {
	return getAttributeListenerTypes().stream().map(NamedAttributeType::getName).collect(Collectors.toSet());
    }

    /**
     * Gets all attribute listeners associated to the supplied named attribute type.
     *
     * @param namedType
     *            Named attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    <T> Set<? extends AttributeListener<T>> getAttributeListeners(final NamedAttributeType<T> namedType);

    /**
     * Gets all attribute listeners associated to the supplied named attribute type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     *
     * @see #getAttributeListeners(NamedAttributeType)
     */
    default <T> Set<? extends AttributeListener<T>> getAttributeListeners(final String name,
	    final AttributeType<T> type) {
	return getAttributeListeners(new NamedAttributeType<>(name, type));
    }

    /**
     * Gets all of the named attribute types with listeners bound.
     *
     * @return All named types with listeners.
     */
    Set<NamedAttributeType<?>> getAttributeListenerTypes();

    /**
     * Gets all the attribute listener types.
     *
     * @param name
     *            Attribute type name.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     * @see #getAttributeListenerTypes
     */
    default Set<AttributeType<?>> getAttributeListenerTypes(final String name) {
	return getAttributeListenerTypes().stream().filter(nt -> name.equals(nt.getName()))
		.map(NamedAttributeType::getType).collect(Collectors.toSet());
    }

    /**
     * Gets all the attribute type names assigned to attributes.
     *
     * @return Attribute type names with values.
     *
     * @see #getAttributeTypes()
     */
    default Set<String> getAttributeNames() {
	return getAttributeTypes().stream().map(NamedAttributeType::getName).collect(Collectors.toSet());
    }

    /**
     * Gets all of the attributes within the container.
     *
     * @return All of the attributes or an empty set if none were found.
     *
     * @see #streamAttributes()
     */
    default Set<?> getAttributes() {
	return streamAttributes().collect(Collectors.toSet());
    }

    /**
     * Gets all of the named attribute types with attributes.
     *
     * @return Named attribute types for the attributes.
     */
    Set<NamedAttributeType<?>> getAttributeTypes();

    /**
     * Gets all of the attribute types within the container.
     *
     * @param name
     *            Attribute type name.
     *
     * @return All of the types of the attributes or an empty set if none were found.
     *
     * @see #getAttributeTypes()
     */
    default Set<AttributeType<?>> getAttributeTypes(final String name) {
	return getAttributeTypes().stream().filter(nt -> name.equals(nt.getName())).map(NamedAttributeType::getType)
		.collect(Collectors.toSet());
    }

    /**
     * This is a convenience method for getting an attribute (optional).
     *
     *
     * @param namedType
     *            Named attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     *
     * @see #getAttribute(NamedAttributeType)
     */
    default <T> Optional<T> getOptAttribute(final NamedAttributeType<T> namedType) {
	return Optional.ofNullable(getAttribute(namedType));
    }

    /**
     * This is a convenience method for getting an attribute (optional).
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     *
     * @see #getOptAttribute(NamedAttributeType)
     */
    default <T> Optional<T> getOptAttribute(final String name, final AttributeType<T> type) {
	return getOptAttribute(new NamedAttributeType<>(name, type));
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @return Whether the attribute was found.
     *
     * @see #getAttribute(NamedAttributeType)
     */
    default <T> boolean hasAttribute(final NamedAttributeType<T> namedType) {
	return getAttribute(namedType) != null;
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     * @return Whether the attribute was found.
     *
     * @see #hasAttribute(NamedAttributeType)
     */
    default <T> boolean hasAttribute(final String name, final AttributeType<T> type) {
	return hasAttribute(new NamedAttributeType<>(name, type));
    }

    /**
     * Checks whether the container contains a particular listener for a given attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @param listener
     *            Listener to check presence of.
     * @return Whether the attribute has any listeners.
     */
    default <T> boolean hasAttributeListener(final NamedAttributeType<T> namedType,
	    final AttributeListener<T> listener) {
	return getAttributeListeners(namedType).contains(listener);
    }

    /**
     * Checks whether the container contains a particular listener for a given attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     * @param listener
     *            Listener to check presence of.
     * @return Whether the attribute has any listeners.
     *
     * @see #hasAttributeListener(NamedAttributeType, AttributeListener)
     */
    default <T> boolean hasAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return hasAttributeListener(new NamedAttributeType<>(name, type), listener);
    }

    /**
     * Checks whether the container contains any listeners for a given attribute type.
     *
     * @param namedType
     *            Named attribute type.
     * @return Whether the attribute has any listeners.
     */
    default <T> boolean hasAttributeListeners(final NamedAttributeType<T> namedType) {
	return !getAttributeListeners(namedType).isEmpty();
    }

    /**
     * Checks whether the container contains any listeners for a given attribute type.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     * @return Whether the attribute has any listeners.
     *
     * @see #hasAttributeListeners(NamedAttributeType)
     */
    default <T> boolean hasAttributeListeners(final String name, final AttributeType<T> type) {
	return hasAttributeListeners(new NamedAttributeType<>(name, type));
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
     * Removes the attribute matching the supplied type.
     *
     * @param namedType
     *            Named attribute type to remove.
     *
     * @return The removed attribute or null if none was removed.
     */
    <T> T removeAttribute(final NamedAttributeType<T> namedType);

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param name
     *            Attribute type name.
     *
     * @param type
     *            Attribute type to remove.
     * @return The removed attribute or null if none was removed.
     *
     * @see #removeAttribute(NamedAttributeType)
     */
    default <T> T removeAttribute(final String name, final AttributeType<T> type) {
	return removeAttribute(new NamedAttributeType<>(name, type));
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
    <T> boolean removeAttributeListener(final NamedAttributeType<T> namedType, final AttributeListener<T> listener);

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
    default <T> boolean removeAttributeListener(final String name, final AttributeType<T> type,
	    final AttributeListener<T> listener) {
	return removeAttributeListener(new NamedAttributeType<>(name, type), listener);
    }

    /**
     * Removes all listeners.
     */
    void removeAttributeListeners();

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param namedType
     *            Named attribute type.
     */
    <T> void removeAttributeListeners(final NamedAttributeType<T> namedType);

    /**
     * Removes all listeners for the supplied attribute types.
     *
     * @param name
     *            Attribute type name.
     * @param type
     *            Attribute type.
     *
     * @see #removeAttributeListeners(NamedAttributeType)
     */
    default <T> void removeAttributeListeners(final String name, final AttributeType<T> type) {
	removeAttributeListeners(new NamedAttributeType<>(name, type));
    }

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
    default <T> Optional<T> removeOptAttribute(final NamedAttributeType<T> namedType) {
	return Optional.ofNullable(removeAttribute(namedType));
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
     *
     * @see #removeOptAttribute(NamedAttributeType)
     */
    default <T> Optional<T> removeOptAttribute(final String name, final AttributeType<T> type) {
	return removeOptAttribute(new NamedAttributeType<>(name, type));
    }

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param namedType
     *            Named attribute type.
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    <T> T setAttribute(final NamedAttributeType<T> namedType, final T attr);

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
     *
     * @see #setAttribute(NamedAttributeType, Object)
     */
    default <T> T setAttribute(final String name, final AttributeType<T> type, final T attr) {
	return setAttribute(new NamedAttributeType<>(name, type), attr);
    }

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
    default <T> Optional<T> setOptAttribute(final NamedAttributeType<T> namedType, final T attr) {
	return Optional.ofNullable(setAttribute(namedType, attr));
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
     *
     * @see #setOptAttribute(NamedAttributeType, Object)
     */
    default <T> Optional<T> setOptAttribute(final String name, final AttributeType<T> type, final T attr) {
	return setOptAttribute(new NamedAttributeType<>(name, type), attr);
    }

    /**
     * Streams all of the attributes within the container.
     *
     * @return Stream of all attributes.
     */
    Stream<?> streamAttributes();
}
