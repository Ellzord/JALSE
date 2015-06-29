package jalse.entities.functions;

import java.lang.reflect.Method;
import java.util.function.Function;

import jalse.entities.methods.EntityMethod;

/**
 * A function for resolving {@link Method} to {@link EntityMethod}.
 *
 * @author Elliot Ford
 *
 * @see EntityFunctionResolver
 * @see EntityFunction
 *
 */
@FunctionalInterface
public interface EntityMethodFunction extends Function<Method, EntityMethod> {}
