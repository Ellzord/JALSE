package jalse.misc;

import java.util.UUID;

public interface Identifiable {

    UUID getID();

    default String toString(Identifiable obj) {

	return obj.getClass().getSimpleName() + " [id=" + obj.getID() + "]";
    }
}
