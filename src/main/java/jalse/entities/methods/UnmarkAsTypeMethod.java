package jalse.entities.methods;

import jalse.entities.Entity;
import jalse.entities.annotations.UnmarkAsType;
import jalse.entities.functions.UnmarkAsTypeFunction;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * This is used for mapping calls to {@link Entity#unmarkAsType(Class)}.
 *
 * @author Elliot Ford
 *
 * @see UnmarkAsType
 * @see UnmarkAsTypeFunction
 *
 */
public class UnmarkAsTypeMethod implements EntityMethod {

    private final Class<? extends Entity> type;

    /**
     * Creates a unmark as type method.
     *
     * @param type
     *            Entity type to unmark as.
     */
    public UnmarkAsTypeMethod(final Class<? extends Entity> type) {
	this.type = Objects.requireNonNull(type);
    }

    @Override
    public Set<Class<? extends Entity>> getDependencies() {
	return Collections.singleton(type);
    }

    /**
     * Gets entity type to unmark as.
     *
     * @return Unmark as type.
     */
    public Class<? extends Entity> getType() {
	return type;
    }

    @Override
    public Object invoke(final Object proxy, final Entity entity, final Object[] args) throws Throwable {
	return entity.unmarkAsType(type);
    }
}
