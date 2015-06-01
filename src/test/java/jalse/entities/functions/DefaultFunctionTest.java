package jalse.entities.functions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jalse.entities.Entity;
import jalse.entities.methods.DefaultMethod;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Test;

public class DefaultFunctionTest {

    public interface TestInvalidEntity extends Entity {

	void testMethod();
    }

    public interface TestValidEntity extends Entity {

	default void testMethod() {}
    }

    DefaultFunction function = null;

    @After
    public void after() {
	function = null;
    }

    @Test
    public void testInvalid() {
	function = new DefaultFunction();

	final Method m = testMethod(TestInvalidEntity.class);

	final DefaultMethod dm = function.apply(m);
	assertNull(dm);
    }

    public Method testMethod(final Class<?> clazz) {
	Method m = null;
	try {
	    m = clazz.getDeclaredMethod("testMethod");
	} catch (NoSuchMethodException | SecurityException e) {
	    fail("Could not get method reference");
	}
	return m;
    }

    @Test
    public void testValid() {
	function = new DefaultFunction();

	final Method m = testMethod(TestValidEntity.class);

	final DefaultMethod dm = function.apply(m);
	assertNotNull(dm);

	assertNotNull(dm.getHandle());
	assertTrue(dm.getArgCount() == 0);
    }
}
