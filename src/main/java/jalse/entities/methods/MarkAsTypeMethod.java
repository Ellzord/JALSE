package jalse.entities.methods;

import jalse.entities.Entity;
import jalse.entities.annotations.MarkAsType;
import jalse.entities.functions.MarkAsTypeFunction;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * This is used for mapping calls to {@link Entity#markAsType(Class)}.
 *
 * @author Elliot Ford
 *
 * @see MarkAsType
 * @see MarkAsTypeFunction
 *
 */
public class MarkAsTypeMethod implements EntityMethod {

    private final Class<? extends Entity> type;

    /**
     * Creates a mark as type method.
     *
     * @param type
     *            Entity type to mark as.
     */
    public MarkAsTypeMethod(final Class<? extends Entity> type) {
	this.type = Objects.requireNonNull(type);
    }

    @Override
    public Set<Class<? extends Entity>> getDependencies() {
	return Collections.singleton(type);
    }

    /**
     * Gets entity type to mark as.
     *
     * @return Mark as type.
     */
    public Class<? extends Entity> getType() {
	return type;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	return entity.markAsType(type);
    }
}
