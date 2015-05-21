package jalse.entities.functions;

import jalse.entities.methods.EntityMethod;

import java.lang.reflect.Method;
import java.util.function.Function;

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
