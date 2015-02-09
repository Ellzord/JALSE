package jalse.tags;

import jalse.misc.Identifiable;

import java.util.Objects;
import java.util.UUID;

/**
 * An immutable {@link Tag} used to identify a direct parent.
 *
 * @author Elliot Ford
 *
 */
public class Parent implements Tag, Identifiable {

    private final UUID id;

    /**
     * @param id
     */
    public Parent(final UUID id) {

	this.id = Objects.requireNonNull(id);
    }

    @Override
    public UUID getID() {

	return id;
    }

    @Override
    public String toString() {

	return toString(this);
    }
}
