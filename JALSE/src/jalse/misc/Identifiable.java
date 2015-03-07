package jalse.misc;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * This is for anything that should be uniquely identifiable within JALSE. This
 * interface allows for easy equality and comparison.
 *
 * @author Elliot Ford
 *
 */
public interface Identifiable extends Comparable<Identifiable> {

    /**
     * A hard-coded dummy ID that can be used to identify an Identifiable that
     * does not need to be unique (not advised). ID =
     * {@code 00000000-0000-0000-0000-000000000000}.
     */
    public static final UUID DUMMY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Identifiable object comparator.
     */
    Comparator<Identifiable> COMPARATOR = (a, b) -> a.compareTo(b);

    /**
     * Convenience method for creating a comparator of the correct type.
     *
     * @return Identifiable comparator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> Comparator<T> comparator() {

	return (Comparator<T>) COMPARATOR;
    }

    /**
     * Checks if the two identifiable objects are equals using their unique
     * identifiers.
     *
     * @param a
     *            First object.
     * @param b
     *            Second object.
     * @return Whether the unique identifiers are equal.
     */
    public static boolean equals(final Identifiable a, final Object b) {

	return a == b || a != null && b instanceof Identifiable
		&& Objects.equals(a.getID(), ((Identifiable) b).getID());
    }

    /**
     * Gets the ID of the object if it is an Identifiable.
     *
     * @param obj
     *            Object to get ID for.
     * @return ID of object if Identifiable instance otherwise return null.
     */
    public static UUID getID(final Object obj) {

	return obj instanceof Identifiable ? ((Identifiable) obj).getID() : null;
    }

    /**
     * Generates a hashcode for an identifiable.
     *
     * @param obj
     *            Identifiable instance.
     * @return Hashcode for the identifiable or {@code 0} if null.
     */
    public static int hashCode(final Identifiable obj) {

	return obj == null ? 0 : Objects.hashCode(obj.getID());
    }

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
    public static Predicate<Identifiable> isID(final UUID id) {

	return i -> i.getID().equals(id);
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
    public static Predicate<Identifiable> notID(final UUID id) {

	return isID(id).negate();
    }

    /**
     * Creates a simple to string for the identifiable. This is structured like
     * {@code <SIMPLE_CLASS_NAME> [id=X]}.
     *
     * @param obj
     *            Identifiable to create a string representation for.
     * @return String representation of the identifiable.
     */
    public static String toString(final Identifiable obj) {

	return obj.getClass().getSimpleName() + " [id=" + obj.getID() + "]";
    }

    @Override
    default int compareTo(final Identifiable o) {

	return Objects.compare(this, o, COMPARATOR);
    }

    /**
     * Gets the unique identifier.
     *
     * @return This objects identifier.
     */
    UUID getID();
}
