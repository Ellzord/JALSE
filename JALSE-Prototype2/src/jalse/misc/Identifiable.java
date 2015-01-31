package jalse.misc;

import java.util.UUID;

public interface Identifiable {

    UUID getID();

    default String toString(final Identifiable obj) {

	return obj.getClass().getSimpleName() + " [id=" + obj.getID() + "]";
    }
}
