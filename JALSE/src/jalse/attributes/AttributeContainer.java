package jalse.attributes;

import static jalse.attributes.Attributes.unwrap;
import jalse.listeners.AttributeListener;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This is an {@link Attribute} collection. This attribute collections works more like a set but
 * using the attribute type to determine uniqueness (only one of each attribute type can be added).
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
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    boolean addAttributeListener(AttributeListener<? extends Attribute> listener);

    /**
     * Adds the supplied attribute to the collection.
     *
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty optional if none
     *         found
     */
    <T extends Attribute> Optional<T> addAttributeOfType(T attr);

    /**
     * This is a convenience method for adding an attribute (no optional).
     *
     * @param attr
     *            Attribute to add.
     * @return The replaced attribute or null if none was replaced.
     */
    default <T extends Attribute> T addOrNullAttributeOfType(final T attr) {
	return addAttributeOfType(attr).orElse(null);
    }

    /**
     * Manually fires an attribute change for the supplied attribute type. This is used for mutable
     * attributes that can change their internal state.
     *
     * @param attr
     *            Attribute type to fire for.
     * @return Whether the collection contains an attribute matching the supplied type.
     */
    <T extends Attribute> boolean fireAttributeChanged(Class<T> attr);

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
    Set<? extends AttributeListener<? extends Attribute>> getAttributeListeners();

    /**
     * Gets all attribute listeners associated to the supplied attribute type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    <T extends Attribute> Set<? extends AttributeListener<T>> getAttributeListeners(Class<T> attr);

    /**
     * Gets all the attribute listener types.
     *
     * @return Set of the types attribute listeners are for or an empty set if none were found.
     */
    Set<Class<? extends Attribute>> getAttributeListenerTypes();

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none found.
     */
    <T extends Attribute> Optional<T> getAttributeOfType(Class<T> attr);

    /**
     * Gets all of the attributes within the container.
     *
     * @return All of the attributes or an empty set if none were found.
     */
    Set<? extends Attribute> getAttributes();

    /**
     * Gets all of the attribute types within the container.
     *
     * @return All of the types of the attributes or an empty set if none were found.
     */
    Set<Class<? extends Attribute>> getAttributeTypes();

    /**
     * This is a convenience method for getting an attribute (no optional).
     *
     * @param attr
     *            Attribute type to check for.
     * @return The attribute matching the supplied type or null if none found.
     */
    default <T extends Attribute> T getOrNullAttributeOfType(final Class<T> attr) {
	return getAttributeOfType(attr).orElse(null);
    }

    /**
     * This is a convenience method for getting an attribute wrapper and unwrapping the result.
     *
     * @param attr
     *            Attribute wrapper type.
     * @return The unwrapped attribute or null if the type had no association.
     *
     * @see #getAttributeOfType(Class)
     * @see NonAttributeWrapper#unwrap()
     */
    default <T, S extends NonAttributeWrapper<T>> T getUnwrapAttributeOfType(final Class<S> attr) {
	return unwrap(getAttributeOfType(attr));
    }

    /**
     * Checks whether the container has a value associated to the supplied attribute type.
     *
     * @param attr
     *            Attribute type.
     * @return Whether the attribute was found.
     */
    default <T extends Attribute> boolean hasAttributeOfType(final Class<T> attr) {
	return getAttributeOfType(attr).isPresent();
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
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    boolean removeAttributeListener(AttributeListener<? extends Attribute> listener);

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to remove.
     * @return Optional containing the removed attribute or else empty optional if none found
     */
    <T extends Attribute> Optional<T> removeAttributeOfType(Class<T> attr);

    /**
     * Removes all attributes within the container (firing removal events).
     */
    void removeAttributes();

    /**
     * This is a convenience method for removing an attribute (no optional).
     *
     * @param attr
     *            Attribute type to remove.
     * @return The removed attribute or null if none was removed.
     */
    default <T extends Attribute> T removeOrNullAttributeOfType(final Class<T> attr) {
	return removeAttributeOfType(attr).orElse(null);
    }

    /**
     * Streams all of the attributes within the container.
     *
     * @return Stream of all attributes.
     */
    Stream<? extends Attribute> streamAttributes();
}
