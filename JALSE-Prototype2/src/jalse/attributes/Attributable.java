package jalse.attributes;

import jalse.listeners.AttributeListener;

import java.util.Optional;
import java.util.Set;

/**
 * This is an {@link Attribute} collection. This attribute collections works
 * more like a set but using the attribute type to determine uniqueness (only
 * one of each attribute type can be added). {@link AttributeListener} can be
 * added for an attribute type, trigger code will fire upon add, update or
 * removal of attributes of that type. Each collection manipulation method
 * returns {@code Optional} of the attribute (may be empty if none matching are
 * found).
 *
 * @author Elliot Ford
 *
 * @see Optional
 *
 */
public interface Attributable {

    /**
     * Adds an attribute listener for the supplied attribute type.
     *
     * @param attr
     *            Attribute type to assign to.
     * @param listener
     *            Listener to add.
     * @return Whether the listener was not already assigned.
     */
    <T extends Attribute> boolean addListener(Class<T> attr, AttributeListener<T> listener);

    /**
     * Adds the specified attribute to the collection.
     *
     * @param attr
     *            Attribute to add.
     * @return Optional containing the replaced attribute if set or else empty
     *         optional if none found
     */
    <T extends Attribute> Optional<T> associate(T attr);

    /**
     * Removes the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to remove.
     * @return Optional containing the removed attribute or else empty optional
     *         if none found
     */
    <T extends Attribute> Optional<T> disassociate(Class<T> attr);

    /**
     * Manually fires an attribute change for the supplied attribute type. This
     * is used for mutable attributes that can change their internal state.
     *
     * @param attr
     *            Attribute type to fire for.
     * @return Whether the collection contains an attribute matching the
     *         supplied type.
     */
    <T extends Attribute> boolean fireAttributeChanged(Class<T> attr);

    /**
     * Gets the attribute matching the supplied type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return Optional containing the attribute or else empty optional if none
     *         found.
     */
    <T extends Attribute> Optional<T> getOfType(Class<T> attr);

    /**
     * Gets all attribute listeners associated to the supplied attribute type.
     *
     * @param attr
     *            Attribute type to check for.
     * @return Set of attribute listeners or an empty set if none were found.
     */
    <T extends Attribute> Set<AttributeListener<T>> getListeners(Class<T> attr);

    /**
     * Removes an attribute listener assigned to the supplied attribute type.
     *
     * @param attr
     *            Attribute type to remove association with.
     * @param listener
     *            Listener to remove.
     * @return Whether the listener was assigned.
     */
    <T extends Attribute> boolean removeListener(Class<T> attr, AttributeListener<T> listener);
}
