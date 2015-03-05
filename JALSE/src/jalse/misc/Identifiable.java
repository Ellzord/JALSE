package jalse.misc;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * This is for anything that should be uniquely identifiable within JALSE. This
 * interface allows for easy equality and comparison.
 *
 * @author Elliot Ford
 *
 */
public interface Identifiable extends Comparable<Identifiable> {

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
