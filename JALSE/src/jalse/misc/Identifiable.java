package jalse.misc;

import java.util.Comparator;
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
    final Comparator<Identifiable> COMPARATOR = (a, b) -> a.compareTo(b);

    /**
     * Gets the unique identifier.
     *
     * @return This objects identifier.
     */
    UUID getID();

    /**
     * Creates a simple to string for the identifiable. This is structured like
     * {@code <SIMPLE_CLASS_NAME> [id=X]}.
     *
     * @param obj
     *            Identifiable to create a string representation for.
     * @return String representation of the identifiable.
     */
    default String toString(final Identifiable obj) {

	return obj.getClass().getSimpleName() + " [id=" + obj.getID() + "]";
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
    public static boolean equals(final Identifiable a, final Identifiable b) {

	return a == b || a != null && b != null && a.getID().equals(b.getID());
    }

    /**
     * Convenience method for creating a comparator of the correct type.
     *
     * @return Identifiable comparator.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Identifiable> Comparator<T> comparator() {

	return (Comparator<T>) COMPARATOR;
    }

    @Override
    default int compareTo(final Identifiable o) {

	return getID().compareTo(o.getID());
    }
}
