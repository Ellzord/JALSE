package jalse.tags;

import jalse.misc.Identifiable;

import java.util.Objects;
import java.util.UUID;

public class Parent implements Tag, Identifiable {

    private final UUID id;

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
