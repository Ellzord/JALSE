package jalse.actions;

import jalse.attributes.Attribute;
import jalse.attributes.AttributeContainer;
import jalse.entities.Entity;
import jalse.misc.Identifiable;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Action is the JALSE equivalent of {@link Runnable}. Actions are performed
 * using a given actor and can be scheduled to be run once now, in the future or
 * periodically at an interval. {@link TickInfo} will be supplied on every
 * execution of an action, this will be current and contain the delta between
 * the last tick. Actions are generally scheduled by {@link Scheduler} for the
 * actor type suitable for the desired result.
 *
 * @author Elliot Ford
 *
 * @param <T>
 *            Type of actor to be supplied.
 *
 * @see Scheduler#scheduleAction(Action, long, long,
 *      java.util.concurrent.TimeUnit)
 * @see TickInfo#getDelta()
 */
@FunctionalInterface
public interface Action<T> {

    /**
     * Predicate to check if the ID is equal to that supplied.
     *
     * @param id
     *            ID to check for.
     * @return Predicate of {@code true} if the ID is equal or {@code false} if
     *         it is not.
     *
     * @see Identifiable#getID()
     */
    default Predicate<Identifiable> isID(final UUID id) {

	return i -> i.getID().equals(id);
    }

    /**
     * Checks to see if the entity has been tagged with the type.
     *
     * @param type
     *            Entity type to check for.
     * @return Predicate of {@code true} if the entity is of the type or
     *         {@code false} if it is not.
     */
    default Predicate<Entity> isMarkedAsType(final Class<? extends Entity> type) {

	return i -> i.isMarkedAsType(type);
    }

    /**
     * Predicate to check attribute is present.
     *
     * @param attr
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is present and
     *         {@code false} if it is not.
     */
    default Predicate<AttributeContainer> isPresent(final Class<? extends Attribute> attr) {

	return a -> a.getAttributeOfType(attr).isPresent();
    }

    /**
     * Predicate to check if the ID is not equal to that supplied.
     *
     * @param id
     *            ID to check for.
     * @return Predicate of {@code false} if the ID is equal or {@code true} if
     *         it is.
     *
     * @see Identifiable#getID()
     */
    default Predicate<Identifiable> notID(final UUID id) {

	return isID(id).negate();
    }

    /**
     * Checks to see if the entity has not been tagged with the type.
     *
     * @param type
     *            Entity type to check for.
     * @return Predicate of {@code true} if the entity is not of the type or
     *         {@code false} if it is.
     */
    default Predicate<Entity> notMarkedAsType(final Class<? extends Entity> type) {

	return isMarkedAsType(type).negate();
    }

    /**
     * Predicate to check attribute is not present.
     *
     * @param attr
     *            Attribute type.
     * @return Predicate of {@code true} if the attribute is not present and
     *         {@code false} if it is.
     */
    default <S extends Attribute> Predicate<AttributeContainer> notPresent(final Class<S> attr) {

	return isPresent(attr).negate();
    }

    /**
     * Performs the actions using the supplied actor and given tick information.
     *
     * @param actor
     *            Actor to use.
     * @param tick
     *            Current tick information
     *
     * @see Runnable#run()
     */
    void perform(T actor, TickInfo tick);
}
