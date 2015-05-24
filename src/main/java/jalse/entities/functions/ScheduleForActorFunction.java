package jalse.entities.functions;

import static jalse.entities.functions.Functions.checkNoParams;
import static jalse.entities.functions.Functions.checkNotDefault;
import static jalse.entities.functions.Functions.firstGenericTypeArg;
import static jalse.entities.functions.Functions.hasReturnType;
import static jalse.entities.functions.Functions.returnTypeIs;
import static jalse.entities.functions.Functions.toClass;
import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.entities.DefaultEntityProxyFactory;
import jalse.entities.Entity;
import jalse.entities.annotations.ScheduleForActor;
import jalse.entities.methods.ScheduleForActorMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * This is a method function for {@link ScheduleForActor} annotation. It will resolve an
 * {@link ScheduleForActorMethod} to be used by the entity typing system. This will resolve the zero
 * argument constructor for the {@link Action}. <br>
 * <br>
 *
 * <pre>
 * <code>
 * {@code @ScheduleForAction(action = Haunt.class)}
 * {@code MutableActionContext<Entity>} scheduleHaunting();
 * 
 * {@code @ScheduleForAction(action = Haunt.class)}
 * void scheduleHaunting();
 * 
 * {@code @ScheduleForAction(action = Haunt.class, initialDelay = 50, unit = TimeUnit.MILLISECONDS)}
 * {@code MutableActionContext<Entity>} scheduleHaunting();
 * 
 * {@code @ScheduleForAction(action = Haunt.class, initialDelay = 50, unit = TimeUnit.MILLISECONDS)}
 * void scheduleHaunting();
 * 
 * 
 * {@code @ScheduleForAction(action = Haunt.class, initialDelay = 50, period = 200, unit = TimeUnit.MILLISECONDS)}
 * {@code MutableActionContext<Entity>} scheduleHaunting();
 * 
 * {@code @ScheduleForAction(action = Haunt.class, initialDelay = 50, period = 200, unit = TimeUnit.MILLISECONDS)}
 * void scheduleHaunting();
 * </code>
 * </pre>
 *
 *
 * NOTE: This function will throw exceptions if {@link ScheduleForActor} is present but the method
 * signature is invalid.
 *
 * @author Elliot Ford
 *
 * @see DefaultEntityProxyFactory
 *
 */
public class ScheduleForActorFunction implements EntityMethodFunction {

    @Override
    public ScheduleForActorMethod apply(final Method m) {
	// Check for annotation
	final ScheduleForActor annonation = m.getAnnotation(ScheduleForActor.class);
	if (annonation == null) {
	    return null;
	}

	// Annotation info
	final long initialDelay = annonation.initialDelay();
	final long period = annonation.period();
	final TimeUnit unit = annonation.unit();

	// Check annotation info
	if (initialDelay < 0) {
	    throw new IllegalArgumentException("Initial delay cannot be negative");
	} else if (period < 0) {
	    throw new IllegalArgumentException("Period cannot be negative");
	}

	// Basic check method signature
	checkNoParams(m);
	checkNotDefault(m);

	// Get constructor for action
	Constructor<?> constructor = null;
	for (final Constructor<?> con : annonation.action().getConstructors()) {
	    // Check for zero arg
	    if (con.getParameterCount() == 0) {
		constructor = con;
		break;
	    }
	}

	// Check constructor
	if (constructor == null) {
	    throw new IllegalArgumentException(
		    String.format("Action type %s has no public statically accessible zero argument constructor",
			    annonation.action()));
	}

	// Check return type
	if (hasReturnType(m)) {
	    // Check context
	    if (!returnTypeIs(m, ActionContext.class)) {
		throw new IllegalArgumentException("Return type must be void or context");
	    }
	    // Check context type
	    final Type contextType = firstGenericTypeArg(m.getGenericReturnType());
	    if (!Entity.class.equals(toClass(contextType))) {
		throw new IllegalArgumentException("Context type must be entity");
	    }
	}

	// Create new schedule action method
	return new ScheduleForActorMethod(constructor, initialDelay, period, unit);
    }
}
