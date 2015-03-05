package jalse.misc;

import java.util.Objects;
import java.util.UUID;

/**
 * This is an abstract implementation of {@link Identifiable}. This class simply
 * takes in a {@link UUID} as an ID and provides equality and string
 * representation methods.
 *
 * @author Elliot Ford
 *
 * @see Identifiable#equals(Identifiable, Object)
 * @see Identifiable#hashCode(Identifiable)
 * @see Identifiable#toString(Identifiable)
 *
 */
public abstract class AbstractIdentifiable implements Identifiable {

    /**
     * Unique ID.
     */
    protected final UUID id;

    /**
     * Creates a new instance of AbstractIdentifiable with the supplied ID.
     *
     * @param id
     *            Unique ID of the Identifiable.
     */
    protected AbstractIdentifiable(final UUID id) {

	this.id = Objects.requireNonNull(id);
    }

    @Override
    public boolean equals(final Object obj) {

	return Identifiable.equals(this, obj);
    }

    @Override
    public UUID getID() {

	return id;
    }

    @Override
    public int hashCode() {

	return Identifiable.hashCode(this);
    }

    @Override
    public String toString() {

	return Identifiable.toString(this);
    }
}
